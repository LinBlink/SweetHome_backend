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
import asia.sweethome.chat.ws.RedisMessageRelay;
import asia.sweethome.common.constants.ConversationTypeConstants;
import asia.sweethome.common.context.UserContext;
import asia.sweethome.common.entity.vo.Result;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/v1/conversations")
@RequiredArgsConstructor
public class ConversationsController {

    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 100;

    private final IConversationsService conversationsService;
    private final IConversationMembersService conversationMembersService;
    private final IMessagesService messagesService;
    private final ChatAssembler chatAssembler;
    private final RedisMessageRelay redisMessageRelay;

    @GetMapping
    /**
     * 得到当前用户的 会话列表
     */
    public Result<List<ConversationVO>> listConversations(
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
    ) {
        Long viewerId = UserContext.getUserId();

        // 得到一个用户参与的所有会话
        List<Long> conversationIds = conversationMembersService.listActiveConversationIds(viewerId);
        if (conversationIds.isEmpty()) {
            return Result.success(List.of());
        }

        // 遍历该用户参与的所有会话
        List<Conversation> conversations = conversationsService.listByIds(conversationIds).stream()
                // 过滤所有被删除的会话
                .filter(c -> c.getDeletedAt() == null)
                // 对所有会话进行排序
                .sorted((a, b) -> {
                    LocalDateTime ta = a.getLastMessageAt();
                    LocalDateTime tb = b.getLastMessageAt();
                    if (ta == null && tb == null) return 0;
                    if (ta == null) return 1; // 正数：a排在b后面。如果 a的最后一则消息没有时间，排在最后
                    if (tb == null) return -1; // 负数：a排在b前面，如果 b的最后一则消息没有时间，排在最前
                    return tb.compareTo(ta);
                })
                .toList();

        // 将会话中的信息进行整理
        List<ConversationVO> result = conversations.stream()
                .map(
                        c -> chatAssembler.toConversationVO(c, viewerId, acceptLanguage)
                )
                .toList();

        return Result.success(result);
    }

    @PostMapping
    public Result<ConversationVO> createDirectConversation(
            @RequestBody CreateConversationDTO dto,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
    ) {
        Long viewerId = UserContext.getUserId();
        Long targetId = dto.getTargetUserId();

        // 幂等：两人之间的私聊会话已存在则直接返回
        List<Long> viewerConversationIds = conversationMembersService.listActiveConversationIds(viewerId);
        Conversation existing = conversationsService.listByIds(viewerConversationIds).stream()
                .filter(c -> ConversationTypeConstants.DIRECT.equals(c.getType()))
                .filter(c -> conversationMembersService.isActiveMember(c.getId(), targetId))
                .findFirst()
                .orElse(null);

        Conversation conversation = existing;
        if (conversation == null) {
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
     * 获取消息历史
     * @param conversationId
     * @param before
     * @param limit
     * @param acceptLanguage
     * @return
     */
    @GetMapping("/{conversationId}/messages")
    public Result<MessageHistoryVO> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(required = false) Long before,
            @RequestParam(required = false) Integer limit,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
    ) {
        Long viewerId = UserContext.getUserId();
        requireActiveMember(conversationId, viewerId);

        int pageSize = limit == null ? DEFAULT_PAGE_SIZE : Math.min(limit, MAX_PAGE_SIZE);

        List<Message> page = messagesService.listPage(conversationId, before, pageSize + 1);
        boolean hasMore = page.size() > pageSize;
        List<Message> pageToReturn = hasMore ? page.subList(0, pageSize) : page;

        List<MessageVO> messageVOs = pageToReturn.stream()
                .map(m -> chatAssembler.toMessageVO(m, viewerId, acceptLanguage))
                .toList();

        Long nextCursor = pageToReturn.isEmpty() ? null : pageToReturn.get(pageToReturn.size() - 1).getId();

        return Result.success(new MessageHistoryVO(messageVOs, hasMore, nextCursor));
    }

    /**
     * 发送消息
     * @param conversationId
     * @param dto
     * @param acceptLanguage
     * @return
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

    /**
     * 标记消息已经阅读
     * @param conversationId
     * @param dto
     * @return
     */
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
     * 得到当前活动的用户
     * @param conversationId
     * @param userId
     * @return
     */
    private ConversationMember requireActiveMember(Long conversationId, Long userId) {
        ConversationMember member = conversationMembersService.getActiveMember(conversationId, userId);
        if (member == null) {
            throw new BusinessException(ErrorCode.NOT_CONVERSATION_MEMBER);
        }
        return member;
    }
}
