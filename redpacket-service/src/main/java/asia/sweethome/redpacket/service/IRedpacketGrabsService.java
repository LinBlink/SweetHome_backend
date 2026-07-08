package asia.sweethome.redpacket.service;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.concurrent.ThreadLocalRandom;

import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.redpacket.entity.po.RedpacketGrab;

/**
 * <p>
 * 记录每个红包每个人抢的情况，红包-用户：1-N 服务类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-21
 */
public interface IRedpacketGrabsService extends IService<RedpacketGrab> {


    RedpacketGrab grabRedpacket( Long userId , Long redpacketId );

    /**
     * 计算一个用户抢到红包的金额
     * @param remainingAmount 红包剩余金额
     * @param remainingCount 红包剩余数量
     * @return 抢到的金额
     */
    static long calculateGrabAmount( long remainingAmount, int remainingCount ){

        if (remainingAmount==0 || remainingCount ==0) {
            throw new BusinessException(
                    ErrorCode.REDPACKET_EMPTY
            );
        }

        if (remainingCount==1) {
            return remainingAmount;
        }

        long maxAmount = remainingAmount/remainingCount * 2 - 1;
        long minAmount = 1; // 最低 0.01 元

        // 右边开区间，变闭区间+1
        long grabAmount = ThreadLocalRandom.current().nextLong(minAmount, maxAmount + 1);

        return grabAmount;

    }

}
