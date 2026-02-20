package asia.sweethome.chat.controller.v1;

import asia.sweethome.chat.entity.dto.CreateConversationDTO;
import asia.sweethome.chat.entity.dto.MarkReadDTO;
import asia.sweethome.chat.entity.dto.SendMessageDTO;
import asia.sweethome.chat.entity.po.Conversation;
import asia.sweethome.chat.entity.po.ConversationMember;
import asia.sweethome.chat.entity.po.Message;
import asia.sweethome.chat.entity.vo.ConversationVO;
import asia.sweethome.chat.entity.vo.MessageHistoryVO;
import asia.sweethome.chat.entity.vo.MessageVO;
import asia.sweethome.chat.service.ChatAssembler;
import asia.sweethome.chat.service.IConversationMembersService;
import asia.sweethome.chat.service.IConversationsService;
import asia.sweethome.chat.service.IMessagesService;
import asia.sweethome.chat.ws.registry.RedisMessageRelay;
import asia.sweethome.common.constants.ConversationTypeConstants;
import asia.sweethome.common.context.UserContext;
import asia.sweethome.common.entity.vo.Result;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 【会话 REST 控制器】
 * <p>
 * 提供聊天的「非实时」HTTP 接口：拉会话列表、开单聊、查历史消息、发消息（兜底）、标记已读。
 * 实时收发走 WebSocket（见 ws 包），这里的发消息接口是 WebSocket 不可用时的备用通道
 * （sendMessageFallback）。所有接口都从 {@link UserContext} 取「当前登录者」，并校验其是会话成员。
 */
@RestController
@RequestMapping("/v1/conversations")
@RequiredArgsConstructor
public class ConversationsController {

    private static final int DEFAULT_PAGE_SIZE = 50;   // 默认每页消息数
    private static final int MAX_PAGE_SIZE = 100;      // 每页上限，防止前端要一大页拖垮服务

    private final IConversationsService conversationsService;
    private final IConversationMembersService conversationMembersService;
    private final IMessagesService messagesService;
    private final ChatAssembler chatAssembler;
    private final RedisMessageRelay redisMessageRelay;

    /**
     * 会话列表：当前用户参与的所有会话，按「最后消息时间」倒序（最近聊的排最前）。
     */
    @GetMapping
    public Result<List<ConversationVO>> listConversations(
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
    ) {
        Long viewerId = UserContext.getUserId();

        // 得到该用户参与的所有会话 id
        List<Long> conversationIds = conversationMembersService.listActiveConversationIds(viewerId);
        if (conversationIds.isEmpty()) {
            return Result.success(List.of());   // 一个会话都没有，返回空列表
        }

        List<Conversation> conversations = conversationsService.listByIds(conversationIds).stream()
                // 过滤掉已删除的会话
                .filter(c -> c.getDeletedAt() == null)
                // 按最后消息时间倒序排。比较器返回值含义：正数=a排b后，负数=a排b前，0=不变
                .sorted((a, b) -> {
                    LocalDateTime ta = a.getLastMessageAt();
                    LocalDateTime tb = b.getLastMessageAt();
                    if (ta == null && tb == null) return 0;
                    if (ta == null) return 1;   // a 没有最后消息 → 排最后
                    if (tb == null) return -1;  // b 没有最后消息 → a 排前面
                    return tb.compareTo(ta);    // 都有则时间晚的在前（倒序）
                })
                .toList();

        // 把每个会话组装成前端可展示的 VO（含未读数、对方称谓等）
        List<ConversationVO> result = conversations.stream()
                .map(c -> chatAssembler.toConversationVO(c, viewerId, acceptLanguage))
                .toList();

        return Result.success(result);
    }

    /**
     * 创建单聊。幂等：如果我和对方之间已经有单聊会话，直接返回那个，不重复创建。
     */
    @PostMapping
    public Result<ConversationVO> createDirectConversation(
            @RequestBody CreateConversationDTO dto,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
    ) {
        Long viewerId = UserContext.getUserId();
        Long targetId = dto.getTargetUserId();

        // 在我的所有单聊里找「对方也在其中」的那个会话
        List<Long> viewerConversationIds = conversationMembersService.listActiveConversationIds(viewerId);
        Conversation existing = conversationsService.listByIds(viewerConversationIds).stream()
                .filter(c -> ConversationTypeConstants.DIRECT.equals(c.getType()))
                .filter(c -> conversationMembersService.isActiveMember(c.getId(), targetId))
                .findFirst()
                .orElse(null);

        Conversation conversation = existing;
        if (conversation == null) {
            // 没有则新建一个单聊，并把双方都加进成员表
            conversation = new Conversation();
            conversation.setType(ConversationTypeConstants.DIRECT);
            conversation.setCreatedAt(LocalDateTime.now());
            conversation.setUpdatedAt(LocalDateTime.now());
            conversationsService.save(conversation);

            conversationMembersService.addOrReactivate(conversation.getId(), viewerId);
            conversationMembersService.addOrReactivate(conversation.getId(), targetId);
        }

        return Result.success(chatAssembler.toConversationVO(conversation, viewerId, acceptLanguage));
    }

    /**
     * 获取消息历史（游标分页，从新往旧翻）。
     * 分页小技巧：故意多查一条（pageSize + 1）。如果真的多查到了，就说明「还有更早的消息」（hasMore=true），
     * 再把多出来的那条去掉只返回 pageSize 条。这样一次查询就同时得到了「本页数据」和「是否还有下一页」。
     *
     * @param conversationId 会话 id
     * @param before 游标：只取 id 比它更小（更早）的消息；不传表示取最新一页
     * @param limit  每页条数，不传用默认值，超过上限则截到上限
     */
    @GetMapping("/{conversationId}/messages")
    public Result<MessageHistoryVO> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(required = false) Long before,
            @RequestParam(required = false) Integer limit,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
    ) {
        Long viewerId = UserContext.getUserId();
        requireActiveMember(conversationId, viewerId);   // 必须是会话成员才能看历史

        int pageSize = limit == null ? DEFAULT_PAGE_SIZE : Math.min(limit, MAX_PAGE_SIZE);

        List<Message> page = messagesService.listPage(conversationId, before, pageSize + 1); // 多查 1 条探测
        boolean hasMore = page.size() > pageSize;
        List<Message> pageToReturn = hasMore ? page.subList(0, pageSize) : page;  // 有多余就砍掉

        List<MessageVO> messageVOs = pageToReturn.stream()
                .map(m -> chatAssembler.toMessageVO(m, viewerId, acceptLanguage))
                .toList();

        // 下一页游标 = 本页最后（最早）一条消息的 id
        Long nextCursor = pageToReturn.isEmpty() ? null : pageToReturn.get(pageToReturn.size() - 1).getId();

        return Result.success(new MessageHistoryVO(messageVOs, hasMore, nextCursor));
    }

    /**
     * 发送消息的「兜底」REST 接口：正常走 WebSocket，WebSocket 连不上时前端可改用这个 HTTP 接口。
     * 逻辑和 WebSocket 的发消息一致：落库 + Redis 广播，保证两条通道行为统一。
     */
    @PostMapping("/{conversationId}/messages")
    public Result<MessageVO> sendMessageFallback(
            @PathVariable Long conversationId,
            @RequestBody SendMessageDTO dto,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
    ) {
        Long viewerId = UserContext.getUserId();
        requireActiveMember(conversationId, viewerId);

        Message message = messagesService.send(
                conversationId, viewerId, dto.getType(), dto.getContent(), dto.getClientId(), null
        );

        redisMessageRelay.publishNewMessage(conversationId, message.getId());

        return Result.success(chatAssembler.toMessageVO(message, viewerId, acceptLanguage));
    }

    /** 标记已读：把我在此会话的已读进度更新到 lastReadMessageId */
    @PutMapping("/{conversationId}/read")
    public Result<Void> markRead(
            @PathVariable Long conversationId,
            @RequestBody MarkReadDTO dto
    ) {
        Long viewerId = UserContext.getUserId();
        requireActiveMember(conversationId, viewerId);

        conversationMembersService.markRead(conversationId, viewerId, dto.getLastReadMessageId());

        return Result.success();
    }

    /**
     * 权限小工具：确认 userId 是该会话的活跃成员，是则返回成员记录，否则抛 NOT_CONVERSATION_MEMBER。
     * 避免非成员偷看/发消息到别人的会话。
     */
    private ConversationMember requireActiveMember(Long conversationId, Long userId) {
        ConversationMember member = conversationMembersService.getActiveMember(conversationId, userId);
        if (member == null) {
            throw new BusinessException(ErrorCode.NOT_CONVERSATION_MEMBER);
        }
        return member;
    }
}
