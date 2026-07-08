package asia.sweethome.redpacket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import asia.sweethome.api.ChatApi;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.redpacket.controller.constant.RedpacketConstant;
import asia.sweethome.redpacket.entity.po.Redpacket;
import asia.sweethome.redpacket.entity.po.RedpacketGrab;
import asia.sweethome.redpacket.mapper.RedpacketGrabsMapper;
import asia.sweethome.redpacket.service.IRedpacketGrabsService;
import asia.sweethome.redpacket.service.IRedpacketService;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 记录每个红包每个人抢的情况，红包-用户：1-N 服务实现类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-21
 */
@RequiredArgsConstructor
@Service
public class RedpacketGrabsServiceImpl extends ServiceImpl<RedpacketGrabsMapper, RedpacketGrab> implements IRedpacketGrabsService {

    @DubboReference
    private ChatApi chatApi;

    private final IRedpacketService redpacketService;



    // Transactional 在 Spring 默认是 REQUIRED 事务传播行为，表示可以加入其他事务。如果没有其他事务，就自己新开事务
    /*
    其他几个事务传播行为：
        REQUIRES_NEW
        NEVER
        SUPPORTS
        NOT_SUPPORTED
        MANDATORY
        NESTED
     */
    @Transactional
    public RedpacketGrab grabRedpacket(Long userId, Long redpacketId){

        // 检查 redpacketId 是否有效，id存在且在ongoing状态
        Redpacket redpacket = redpacketService.lambdaQuery()
                // 得到红包id
                .eq(
                        Redpacket::getId,
                        redpacketId
                        // 得到红包状态
                ).eq(
                        Redpacket::getStatus,
                        RedpacketConstant.REDPACKET_STATUS_ONGOING

                ).one();


        if (redpacket == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REDPACKET
            );
        }

        if (redpacket.getExpiredAt().isBefore( LocalDateTime.now() )) {
            // 红包如果过期了，更新数据库记录，throw Exception
            redpacketService.markRedpacketAsExpired( redpacketId );
            throw new BusinessException(
                    ErrorCode.REDPACKET_EXPIRED
            );
        }



        // 检查用户是否在 该红包的 conversationId 里面
        if (!chatApi.userExistsInConversation(userId, redpacket.getConversationId()).equals(
                Boolean.TRUE
        )) {
            throw new BusinessException(
                    ErrorCode.NOT_CONVERSATION_MEMBER
            );
        }

        // 下面如何瓜分一部分红包给该用户
        // 如果 count = 1,全部给用户
        // 如果 count > 1,产生随机性
        long grabAmount = IRedpacketGrabsService.calculateGrabAmount(
                redpacket.getRemainingAmount(),
                redpacket.getRemainingCount()
        );


        RedpacketGrab redpacketGrab = new RedpacketGrab();
        redpacketGrab.setRedpacketId( redpacketId );
        redpacketGrab.setUserId( userId );
        redpacketGrab.setGrabAmount( grabAmount );
        redpacketGrab.setCreatedAt( LocalDateTime.now() );

        // 抢红包记录的保存具有原子性。如果已经抢到了并插入了记录，无法重复插入。重复插入则抛异常
        try {
            save( redpacketGrab );
        } catch (DuplicateKeyException e) {
            throw new BusinessException(
                    ErrorCode.REDPACKET_GRABBED_ALREADY
            );
        }

        // 乐观锁
        boolean updateSuccess = redpacketService.lambdaUpdate().eq(
                        Redpacket::getId,
                        redpacketId
                ).gt(
                        Redpacket::getRemainingCount,
                        0
                )
                .setSql(
                        "remaining_count = remaining_count -1"
                ).setSql(
                        "remaining_amount = remaining_amount -" + grabAmount
                ).set(
                        // 如果 grabAmount 和 红包剩余金额相等，要将红包状态改为 FINISHED
                        grabAmount == redpacket.getRemainingAmount(),
                        Redpacket::getStatus,
                        RedpacketConstant.REDPACKET_STATUS_FINISHED
                ).update();

        // 判断记录更新是否成功，不成功则抛出非受检异常，事务回滚
        if (!updateSuccess) {
            throw new BusinessException(
                    ErrorCode.REDPACKET_EMPTY
            );
        }

        // 抢红包完毕

        return redpacketGrab;

    }


}
