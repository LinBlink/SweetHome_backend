package asia.sweethome.location.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

import asia.sweethome.location.entity.po.OutboxMessage;
import asia.sweethome.location.mapper.OutboxMessagesMapper;
import asia.sweethome.location.service.IOutboxMessagesService;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-16
 */
@Service
public class OutboxMessagesServiceImpl extends ServiceImpl<OutboxMessagesMapper, OutboxMessage> implements IOutboxMessagesService {

}
