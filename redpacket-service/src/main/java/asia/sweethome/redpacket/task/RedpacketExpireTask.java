package asia.sweethome.redpacket.task;

import static asia.sweethome.redpacket.constant.RedisConstant.KEY_SWEEP_EXPIRED_REDPACKET_LOCK;
import static asia.sweethome.redpacket.constant.ScheduleConstant.DELAY_SWEEP_EXPIRED_REDPACKETS_MS;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import asia.sweethome.api.UserApi;
import asia.sweethome.redpacket.constant.RedpacketConstant;
import asia.sweethome.redpacket.entity.po.Redpacket;
import asia.sweethome.redpacket.service.IRedpacketService;
import asia.sweethome.redpacket.util.RedisDistributedLockUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @description: 定时检查所有红包状态。如果已经过期，执行 refund。
 * @author: LOCRIAN_V
 * @date: 7/23/2026 5:44 下午
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedpacketExpireTask {

    @DubboReference
    private UserApi userApi;

    private final IRedpacketService redpacketService;
    private final RedisDistributedLockUtil redisDistributedLockUtil;

    @Scheduled(fixedDelay = DELAY_SWEEP_EXPIRED_REDPACKETS_MS)
    public void sweetpExpiredRedpackets() {

        boolean lockSuccess = redisDistributedLockUtil.tryLock(
                KEY_SWEEP_EXPIRED_REDPACKET_LOCK
        );

        // 没有成功拿到锁，直接返回
        if (!lockSuccess) {
            return;
        }

        // 拿到锁先来个大try
        try {
            // 拿到所有的已过期的红包名单
            List<Redpacket> expiredList = redpacketService.lambdaQuery()
                    .eq(
                            Redpacket::getStatus,
                            RedpacketConstant.REDPACKET_STATUS_ONGOING
                    ).lt(
                            Redpacket::getExpiredAt,
                            LocalDateTime.now()
                    ).last(
                            "LIMIT 100"
                    ).list();

            // 对每个红包进行退款+改状态
            for (Redpacket redpacket : expiredList) {

                // 先看退款能否成功
                boolean increased = userApi.increaseBalance(redpacket.getUserId(),
                        redpacket.getRemainingAmount());

                // 退款没有成功，就没有修改红包的必要
                if (!increased) {
                    continue;
                }

                // 退款成功了，再去修改红包状态
                boolean updated = redpacketService.lambdaUpdate()
                        .eq(
                                Redpacket::getId,
                                redpacket.getId()
                        ).eq(
                                Redpacket::getStatus,
                                RedpacketConstant.REDPACKET_STATUS_ONGOING
                        )
                        .set(
                                Redpacket::getStatus,
                                RedpacketConstant.REDPACKET_STATUS_refunded
                        ).update();

                // 红包状态更新失败，退款失败，重新进行扣减
                if (!updated) {
                    boolean deducted = userApi.deductBalance(redpacket.getUserId(), redpacket.getRemainingAmount());
                    if (!deducted) {
                        log.error("红包过期退款失败");
                    }
                }


                // todo 向红包所有者发通知，表示红包已退回
            }
        } finally {
            redisDistributedLockUtil.unLock(KEY_SWEEP_EXPIRED_REDPACKET_LOCK);
        }


    }

}
