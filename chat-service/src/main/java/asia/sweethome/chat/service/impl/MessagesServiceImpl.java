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

@Service
@RequiredArgsConstructor
public class MessagesServiceImpl extends ServiceImpl<MessagesMapper, Message> implements IMessagesService {

    private static final int MAX_CONTENT_LENGTH = 2000;

    private final IConversationsService conversationsService;

    @Override
    public Message findByClientId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return null;
        }
        return lambdaQuery().eq(Message::getClientId, clientId).one();
    }

    @Override
    public List<Message> listPage(Long conversationId, Long before, int limit) {
        var query = lambdaQuery()
                .eq(Message::getConversationId, conversationId)
                .isNull(Message::getDeletedAt);
        if (before != null) {
            query = query.lt(Message::getId, before);
        }
        return query.orderByDesc(Message::getId).last("limit " + limit).list();
    }

    @Override
    public long countUnread(Long conversationId, Long afterMessageId) {
        return lambdaQuery()
                .eq(Message::getConversationId, conversationId)
                .isNull(Message::getDeletedAt)
                .gt(Message::getId, afterMessageId == null ? 0 : afterMessageId)
                .count();
    }

    @Override
    @Transactional
    public Message send(Long conversationId, Long senderId, String type, String content, String clientId, Long replyToId) {

        if (content != null && content.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(ErrorCode.MESSAGE_TOO_LONG);
        }

        // clientId 去重，幂等返回已存在的消息
        Message existing = findByClientId(clientId);
        if (existing != null) {
            return existing;
        }

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setType(type);
        message.setContent(content);
        message.setClientId(clientId);
        message.setReplyToId(replyToId);
        message.setSentAt(LocalDateTime.now());
        save(message);

        Conversation conversation = conversationsService.getById(conversationId);
        if (conversation != null) {
            conversation.setLastMessageId(message.getId());
            conversation.setLastMessageAt(message.getSentAt());
            conversationsService.updateById(conversation);
        }

        return message;
    }
}
