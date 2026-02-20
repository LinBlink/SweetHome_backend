package asia.sweethome.chat.service.impl;

import asia.sweethome.chat.entity.po.Conversation;
import asia.sweethome.chat.entity.po.Message;
import asia.sweethome.chat.mapper.MessagesMapper;
import asia.sweethome.chat.service.IConversationsService;
import asia.sweethome.chat.service.IMessagesService;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 【消息 服务实现类】负责消息的落库、分页查询、未读统计。
 */
@Service
@RequiredArgsConstructor
public class MessagesServiceImpl extends ServiceImpl<MessagesMapper, Message> implements IMessagesService {

    private static final int MAX_CONTENT_LENGTH = 2000;

    private final IConversationsService conversationsService;

    /** 按客户端 id 查消息（用于去重）。clientId 为空视为不去重，返回 null */
    @Override
    public Message findByClientId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return null;
        }
        return lambdaQuery().eq(Message::getClientId, clientId).one();
    }

    /**
     * 「游标分页」查历史消息。聊天记录按 id 从新到旧翻页：
     * before 传上一页最老那条的 id，就能接着往更早翻；before 为 null 表示取最新一页。
     * 相比传统「第几页」，游标分页在数据不断新增时不会错位、重复。
     */
    @Override
    public List<Message> listPage(Long conversationId, Long before, int limit) {
        var query = lambdaQuery()
                .eq(Message::getConversationId, conversationId)
                .isNull(Message::getDeletedAt);
        if (before != null) {
            query = query.lt(Message::getId, before);   // 只要比 before 更早（id 更小）的
        }
        // 按 id 倒序取 limit 条（最新的在前）
        return query.orderByDesc(Message::getId).last("limit " + limit).list();
    }

    /** 统计未读数：conversationId 里 id 大于 afterMessageId（我已读到的位置）的消息条数 */
    @Override
    public long countUnread(Long conversationId, Long afterMessageId) {
        return lambdaQuery()
                .eq(Message::getConversationId, conversationId)
                .isNull(Message::getDeletedAt)
                .gt(Message::getId, afterMessageId == null ? 0 : afterMessageId)
                .count();
    }

    /**
     * 发消息落库。加 @Transactional：因为要「插入消息」+「更新会话的最后消息」两步，
     * 用事务保证要么都成功、要么都回滚，不会出现消息存了但会话预览没更新的半吊子状态。
     * （本方法不跨服务调用，所以可以安全用本地事务。）
     */
    @Override
    @Transactional
    public Message send(Long conversationId, Long senderId, String type, String content, String clientId, Long replyToId) {

        if (content != null && content.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(ErrorCode.MESSAGE_TOO_LONG);
        }

        // 幂等去重：同一 clientId 已存过就直接返回旧记录，避免网络重发导致存两条
        Message existing = findByClientId(clientId);
        if (existing != null) {
            return existing;
        }

        // 1. 插入新消息
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setType(type);
        message.setContent(content);
        message.setClientId(clientId);
        message.setReplyToId(replyToId);
        message.setSentAt(LocalDateTime.now());
        save(message);

        // 2. 顺带更新会话的「最后一条消息」信息，供会话列表展示预览和排序
        Conversation conversation = conversationsService.getById(conversationId);
        if (conversation != null) {
            conversation.setLastMessageId(message.getId());
            conversation.setLastMessageAt(message.getSentAt());
            conversationsService.updateById(conversation);
        }

        return message;
    }
}
