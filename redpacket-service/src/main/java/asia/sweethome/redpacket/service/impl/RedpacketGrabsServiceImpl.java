package asia.sweethome.redpacket.service.impl;

import static asia.sweethome.redpacket.constant.RedisConstant.*;
import static asia.sweethome.redpacket.util.RedpacketUtil.calculateGrabAmount;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import asia.sweethome.api.ChatApi;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.redpacket.config.LuaScriptLoader;
import asia.sweethome.redpacket.constant.RedpacketConstant;
import asia.sweethome.redpacket.entity.po.Redpacket;
import asia.sweethome.redpacket.entity.po.RedpacketGrab;
import asia.sweethome.redpacket.entity.vo.RedpacketGrabVO;
import asia.sweethome.redpacket.mapper.RedpacketGrabsMapper;
import asia.sweethome.redpacket.service.IRedpacketGrabsService;
import asia.sweethome.redpacket.service.IRedpacketService;
import cn.hutool.core.bean.BeanUtil;
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

    private final StringRedisTemplate stringRedisTemplate;

    private final LuaScriptLoader luaScriptLoader;

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
    public RedpacketGrab grabRedpacket_deprecated(Long userId, Long redpacketId) {

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

        // 没查到红包
        if (redpacket == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REDPACKET
            );
        }

        // 检查是否过期
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
        long grabAmount = calculateGrabAmount(
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

    public RedpacketGrab grabRedpacket(Long userId, Long redpacketId) {

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

        // 没查到红包
        if (redpacket == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REDPACKET
            );
        }

        // 检查是否过期
        if (redpacket.getExpiredAt().isBefore(LocalDateTime.now())) {
            // 红包如果过期了，更新数据库记录，throw Exception
            redpacketService.markRedpacketAsExpired(redpacketId);
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

        // --- 校验完成，调脚本完成抢红包

        Long redisExecuteRst = stringRedisTemplate.execute(
                luaScriptLoader.getGrabRedpacketScript(),
                List.of(
                        KEY_LIST_REDPACKET_ALLOCATION + ":" + redpacketId,
                        KEY_HASH_GRABBED_USERS + ":" + redpacketId,
                        KEY_STREAM_REDPACKET_GRAB_OUTBOX
                ),
                String.valueOf(
                        userId
                ),
                String.valueOf(
                        redpacketId
                )
        );

        if (redisExecuteRst == null) {
            log.error("抢红包时发现redis没有正常执行");
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR
            );
        }


        if (USER_ALREADY_GRABBED.equals(redisExecuteRst)) {
            throw new BusinessException(
                    ErrorCode.REDPACKET_GRABBED_ALREADY
            );
        }

        if (REDPACKET_EMPTY.equals(redisExecuteRst)) {
            throw new BusinessException(
                    ErrorCode.REDPACKET_EMPTY
            );
        }

        // redis异步保存数据，否则扛不住高并发。采用redis的stream消息队列，可以保证原子性

        RedpacketGrab redpacketGrab = new RedpacketGrab();

        redpacketGrab.setRedpacketId(redpacketId);
        redpacketGrab.setUserId(userId);
        redpacketGrab.setGrabAmount(redisExecuteRst);
        redpacketGrab.setCreatedAt(LocalDateTime.now());

        return redpacketGrab;
    }

    @Transactional
    public void persistGrab(Long redpacketId, Long userId, Long amount) {

        RedpacketGrab grab = new RedpacketGrab();

        grab.setRedpacketId(redpacketId);
        grab.setUserId(userId);
        grab.setGrabAmount(amount);
        grab.setCreatedAt(LocalDateTime.now());

        save(
                grab
        );

        redpacketService.lambdaUpdate()
                .eq(Redpacket::getId,
                        redpacketId)
                .setSql(
                        "remaining_amount = remaining_amount -" + amount
                ).setSql(
                        "remaining_count = remaining_count - 1"
                ).update();

        redpacketService.lambdaUpdate()
                .eq(
                        Redpacket::getId,
                        redpacketId
                ).eq(
                        Redpacket::getRemainingCount,
                0
                ).set(
                        Redpacket::getStatus,
                RedpacketConstant.REDPACKET_STATUS_FINISHED
                ).update();

    }

    @Override
    public List<RedpacketGrabVO> getRedpacketsIGrabbed(Long userId) {

        List<RedpacketGrab> redpacketGrabList = lambdaQuery().eq(
                RedpacketGrab::getUserId,
                userId
        ).list();

        return BeanUtil.copyToList(
                redpacketGrabList,
                RedpacketGrabVO.class
        );
    }


    @Override
    public List<RedpacketGrabVO> getRedpacketGrabDetail(Long userId, Long redpacketId) {

        Redpacket redpacket = redpacketService.getById(redpacketId);

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

        List<RedpacketGrab> redpacketGrabList = lambdaQuery().eq(
                RedpacketGrab::getRedpacketId,
                redpacketId
        ).list();

        return BeanUtil.copyToList(
                redpacketGrabList,
                RedpacketGrabVO.class
        );

    }


}
