package asia.sweethome.health.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.health.constant.HealthMetricConstant;
import asia.sweethome.health.entity.dto.HealthVisibilityDTO;
import asia.sweethome.health.entity.po.HealthMetricVisibility;
import asia.sweethome.health.entity.vo.HealthVisibilityVO;
import asia.sweethome.health.mapper.HealthMetricVisibilityMapper;
import asia.sweethome.health.service.IHealthVisibilityService;

/**
 * @author LocrianFifth
 * @since 2026-07-19
 */
@Service
public class HealthVisibilityServiceImpl extends ServiceImpl<HealthMetricVisibilityMapper, HealthMetricVisibility> implements IHealthVisibilityService {

    @Override
    public List<HealthVisibilityVO> queryMyVisibility(Long userId) {

        List<HealthMetricVisibility> records = lambdaQuery()
                .eq(HealthMetricVisibility::getUserId, userId)
                .list();

        Map<String, Boolean> metricTypeVisibleMap = new HashMap<>();
        for (HealthMetricVisibility record : records) {
            metricTypeVisibleMap.put(record.getMetricType(), record.getVisible());
        }

        // 三种指标类型永远补全返回，没配置过的按 false（私密），不留隐含空洞让前端猜
        List<HealthVisibilityVO> result = new java.util.ArrayList<>(HealthMetricConstant.TYPE_LIST.size());
        for (String metricType : HealthMetricConstant.TYPE_LIST) {
            HealthVisibilityVO vo = new HealthVisibilityVO();
            vo.setMetricType(metricType);
            vo.setVisible(Boolean.TRUE.equals(metricTypeVisibleMap.get(metricType)));
            result.add(vo);
        }

        return result;
    }

    @Override
    public void updateVisibility(Long userId, HealthVisibilityDTO dto) {

        if (!HealthMetricConstant.TYPE_LIST.contains(dto.getMetricType())) {
            throw new BusinessException(ErrorCode.INVALID_HEALTH_METRIC_TYPE);
        }

        HealthMetricVisibility one = lambdaQuery()
                .eq(HealthMetricVisibility::getUserId, userId)
                .eq(HealthMetricVisibility::getMetricType, dto.getMetricType())
                .one();

        LocalDateTime now = LocalDateTime.now();

        if (one != null) {
            one.setVisible(dto.getVisible());
            one.setUpdatedAt(now);
            updateById(one);
            return;
        }

        HealthMetricVisibility record = new HealthMetricVisibility();
        record.setUserId(userId);
        record.setMetricType(dto.getMetricType());
        record.setVisible(dto.getVisible());
        record.setUpdatedAt(now);
        save(record);
    }

    @Override
    public boolean isVisible(Long userId, String metricType) {

        HealthMetricVisibility one = lambdaQuery()
                .eq(HealthMetricVisibility::getUserId, userId)
                .eq(HealthMetricVisibility::getMetricType, metricType)
                .one();

        // 没有配置记录时按「私密」处理，这是显式的 fail-safe default，不是数据库列默认值能保证的
        if (one == null) {
            return false;
        }

        return Boolean.TRUE.equals(one.getVisible());
    }

}
