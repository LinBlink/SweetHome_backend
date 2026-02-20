package asia.sweethome.chat.service;

import asia.sweethome.chat.entity.po.Message;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 【消息 服务接口】继承通用 CRUD，补充发消息、分页、未读统计等业务方法。
 */
public interface IMessagesService extends IService<Message> {

    // 按客户端 id 查消息（去重用）
    Message findByClientId(String clientId);

    // 游标分页：before 为 null 表示取最新一页
    List<Message> listPage(Long conversationId, Long before, int limit);

    // 统计某会话在 afterMessageId 之后的消息数（即未读数）
    long countUnread(Long conversationId, Long afterMessageId);

    // 发送并落库一条消息（内部按 clientId 幂等去重）
    Message send(Long conversationId, Long senderId, String type, String content, String clientId, Long replyToId);
}
