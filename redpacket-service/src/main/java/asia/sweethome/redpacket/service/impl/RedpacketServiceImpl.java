package asia.sweethome.redpacket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import asia.sweethome.api.ChatApi;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.redpacket.controller.constant.RedpacketConstant;
import asia.sweethome.redpacket.entity.dto.RedpacketDTO;
import asia.sweethome.redpacket.entity.po.Redpacket;
import asia.sweethome.redpacket.mapper.RedpacketMapper;
import asia.sweethome.redpacket.service.IRedpacketService;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-21
 */
@Service
public class RedpacketServiceImpl extends ServiceImpl<RedpacketMapper, Redpacket> implements IRedpacketService {

    @DubboReference
    private ChatApi chatApi;

    @Override
    // REQUIRES_NEW 的事务传播行为：如果外层有事务将外层事务挂起，该任务作为新事务。如果外层没有事务，自己作为新事务
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRedpacketAsExpired( Long redpacketId ){
        lambdaUpdate().eq(
                Redpacket::getId,
                redpacketId
        ).set(
                Redpacket::getStatus,
                RedpacketConstant.REDPACKET_STATUS_EXPIRED
        ).update();
    }

    @Transactional
    @Override
    public Redpacket createRedpacket(Long userId, RedpacketDTO dto) {

        Redpacket redpacket = new Redpacket();

        Long conversationId = dto.getConversationId();

        if (!chatApi.userExistsInConversation( userId, conversationId ).equals( Boolean.TRUE )) {
            throw new BusinessException(
                    ErrorCode.NO_SUCH_CONVERSATION
            );
        }


        Long familyId = chatApi.getConversationFamilyId( conversationId );

        Long conversationMemberCount = chatApi.getConversationMemberCount(conversationId);

        if (dto.getTotalCount() > conversationMemberCount) {
            throw new BusinessException(
                    ErrorCode.REDPACKET_NUMBER_MORE_THAN_CONVERSATION_MEMBERS
            );
        }


        LocalDateTime now = LocalDateTime.now();

        redpacket.setUserId(userId);

        redpacket.setTotalAmount(
                dto.getTotalAmount()
        );
        redpacket.setTotalCount(
                dto.getTotalCount()
        );
        redpacket.setRemainingAmount(

                dto.getTotalAmount()
        );
        redpacket.setRemainingCount(
                dto.getTotalCount()
        );
        redpacket.setFamilyId(
                familyId
        );
        redpacket.setConversationId(
                conversationId
        );
        // 这里的 enum 怎么写
        redpacket.setStatus("ongoing");
        redpacket.setExpiredAt(
                now.plusDays(1)
        );
        redpacket.setCreatedAt(
                now
        );

        save( redpacket );

        // todo 用户余额扣减

        return redpacket;

    }
}
