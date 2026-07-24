package asia.sweethome.redpacket.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/23/2026 9:34 下午
 */
public class RedpacketUtil {

    /**
     * 计算一个用户抢到红包的金额
     *
     * @param remainingAmount 红包剩余金额
     * @param remainingCount  红包剩余数量
     * @return 抢到的金额
     */
    public static long calculateGrabAmount(long remainingAmount, int remainingCount) {

        if (remainingAmount == 0 || remainingCount == 0) {
            throw new BusinessException(
                    ErrorCode.REDPACKET_EMPTY
            );
        }

        if (remainingCount == 1) {
            return remainingAmount;
        }

        long maxAmount = remainingAmount / remainingCount * 2 - 1;
        long minAmount = 1; // 最低 0.01 元

        // 右边开区间，变闭区间+1
        long grabAmount = ThreadLocalRandom.current().nextLong(minAmount, maxAmount + 1);

        return grabAmount;
    }

    /**
     * 给定红包总金额和拆分数量，给出每份金额
     *
     * @param totalAmount 总金额
     * @param totalCount  数量
     * @return
     */
    public static List<Long> splitRedpacket(
            long totalAmount, int totalCount
    ) {
        // 拆包算法

        ArrayList<Long> rst = new ArrayList<>(totalCount);
        long remainAmount = totalAmount;
        int remainCount = totalCount;

        for (int i = 0; i < totalCount; i++) {

            long onePackAmount = calculateGrabAmount(
                    remainAmount,
                    remainCount
            );

            remainAmount = remainAmount - onePackAmount;
            rst.add(onePackAmount);
            remainCount--;
        }

        return rst;
    }

}
