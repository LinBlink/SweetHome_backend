package asia.sweethome.chat.service.impl;

import asia.sweethome.chat.entity.po.Conversation;
import asia.sweethome.chat.mapper.ConversationsMapper;
import asia.sweethome.chat.service.IConversationsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 【会话 服务实现类】暂无自定义方法，完全复用 MyBatis-Plus 的通用 CRUD。
 */
@Service
public class ConversationsServiceImpl extends ServiceImpl<ConversationsMapper, Conversation> implements IConversationsService {
}
