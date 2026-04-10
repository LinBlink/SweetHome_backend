package asia.sweethome.user.service.impl;

import asia.sweethome.user.entity.po.OutboxMessage;
import asia.sweethome.user.mapper.OutboxMessagesMapper;
import asia.sweethome.user.service.IOutboxMessagesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-10
 */
@Service
public class OutboxMessagesServiceImpl extends ServiceImpl<OutboxMessagesMapper, OutboxMessage> implements IOutboxMessagesService {

}
