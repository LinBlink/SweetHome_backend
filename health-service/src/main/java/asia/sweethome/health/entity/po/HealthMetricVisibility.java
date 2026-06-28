package asia.sweethome.health.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 每个成员对每种健康指标是否向家庭公开的设置
 *
 * @author LocrianFifth
 * @since 2026-07-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("health_metric_visibility")
public class HealthMetricVisibility implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String metricType;

    /**
     * 没有对应行时，业务层按「私密」处理，不能只靠这个字段的默认值
     */
    private Boolean visible;

    private LocalDateTime updatedAt;

}
