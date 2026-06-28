package asia.sweethome.health.service;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

import asia.sweethome.health.entity.dto.HealthVisibilityDTO;
import asia.sweethome.health.entity.po.HealthMetricVisibility;
import asia.sweethome.health.entity.vo.HealthVisibilityVO;

/**
 * @author LocrianFifth
 * @since 2026-07-19
 */
public interface IHealthVisibilityService extends IService<HealthMetricVisibility> {

    /**
     * 查自己的可见性设置，三种指标类型永远补全返回（没设置过的按 false）
     */
    List<HealthVisibilityVO> queryMyVisibility(Long userId);

    void updateVisibility(Long userId, HealthVisibilityDTO dto);

    /**
     * 供 HealthRecordsService 内部调用：某成员的某项指标是否公开给家庭。
     * 没有配置记录时按「私密」处理。
     */
    boolean isVisible(Long userId, String metricType);

}
