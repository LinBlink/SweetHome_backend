package asia.sweethome.chat.service;

import asia.sweethome.chat.entity.po.Message;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IMessagesService extends IService<Message> {

    Message findByClientId(String clientId);

    // 游标分页：before 为 null 表示取最新一页
    List<Message> listPage(Long conversationId, Long before, int limit);

    long countUnread(Long conversationId, Long afterMessageId);

    Message send(Long conversationId, Long senderId, String type, String content, String clientId, Long replyToId);
}
