package asia.sweethome.redpacket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import asia.sweethome.api.ChatApi;
import asia.sweethome.api.UserApi;
import asia.sweethome.api.entity.dto.UserDTO;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.redpacket.constant.RedisConstant;
import asia.sweethome.redpacket.constant.RedpacketConstant;
import asia.sweethome.redpacket.entity.dto.RedpacketDTO;
import asia.sweethome.redpacket.entity.po.Redpacket;
import asia.sweethome.redpacket.entity.vo.RedpacketVO;
import asia.sweethome.redpacket.mapper.RedpacketMapper;
import asia.sweethome.redpacket.service.IRedpacketService;
import asia.sweethome.redpacket.util.RedpacketUtil;
import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-21
 */
@RequiredArgsConstructor
@Service
public class RedpacketServiceImpl extends ServiceImpl<RedpacketMapper, Redpacket> implements IRedpacketService {

    @DubboReference
    private ChatApi chatApi;

    @DubboReference
    private UserApi userApi;

    private final StringRedisTemplate stringRedisTemplate;

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
                    ErrorCode.NOT_CONVERSATION_MEMBER
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

        redpacket.setStatus(
                RedpacketConstant.REDPACKET_STATUS_ONGOING
        );
        redpacket.setExpiredAt(
                now.plusDays(1)
        );
        redpacket.setCreatedAt(
                now
        );

        // 扣减用户余额
        // todo 分布式事务
        if (!userApi.deductBalance( userId, redpacket.getTotalAmount() )) {
            throw new BusinessException(
                    ErrorCode.INSUFFICIENT_FUND
            );
        }

        save( redpacket );

        // 确保事务提交完成了，再放缓存redis
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        // redis 分配list
                        List<Long> allocationList = RedpacketUtil.splitRedpacket(redpacket.getTotalAmount(), redpacket.getTotalCount());

                        String key = RedisConstant.KEY_LIST_REDPACKET_ALLOCATION + ":" + redpacket.getId();

                        stringRedisTemplate.opsForList()
                                .rightPushAll(
                                        key,
                                        allocationList.stream().map(String::valueOf).toList()
                                );
                        // 设置兜底过期时间
                        stringRedisTemplate.expire(
                                key,
                                RedisConstant.KEY_LIST_REDPACKET_ALLOCATION_TTL
                        );


                        /**
                         * 缓存创建失效怎么办？
                         * 会走定时任务兜底逻辑。一天以后数据库将用户金额退回
                         */
                    }
                }
        );

        return redpacket;

    }

    @Override
    public RedpacketVO getRedpacketDetail(Long userId, Long redpacketId) {

        Redpacket redpacket = getById(redpacketId);

        if (redpacket == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REDPACKET
            );
        }

        if (!chatApi.userExistsInConversation(userId, redpacket.getConversationId()).equals(
                Boolean.TRUE
        )) {
            throw new BusinessException(
                    ErrorCode.NOT_CONVERSATION_MEMBER
            );
        }

        RedpacketVO vo = BeanUtil.copyProperties(redpacket, RedpacketVO.class);

        UserDTO user = userApi.findUserById(vo.getUserId());

        vo.setUserAvatarUrl( user.getAvatarUrl() );
        vo.setUsername( user.getName() );

        return vo;

    }

    @Override
    public List<RedpacketVO> getRedpacketsISent(Long userId) {

        List<Redpacket> redpackets = lambdaQuery().eq(
                Redpacket::getUserId,
                userId
        ).list();

        if (redpackets == null) {
            return Collections.emptyList();
        }

        return BeanUtil.copyToList(
                redpackets, RedpacketVO.class
        );

    }


}
