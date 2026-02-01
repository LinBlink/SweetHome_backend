package asia.sweethome.chat.service.impl;

import asia.sweethome.chat.entity.po.Conversation;
import asia.sweethome.chat.mapper.ConversationsMapper;
import asia.sweethome.chat.service.IConversationsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ConversationsServiceImpl extends ServiceImpl<ConversationsMapper, Conversation> implements IConversationsService {
}
