package asia.sweethome.health.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 一条 = 某成员某天某项健康指标的记录
 *
 * @author LocrianFifth
 * @since 2026-07-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("health_records")
public class HealthRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 冗余字段，减少按家庭查询时反查 family-service 的次数
     */
    private Long familyId;

    /**
     * HEIGHT / WEIGHT / BLOOD_PRESSURE，见 HealthMetricConstant
     */
    private String metricType;

    /**
     * 身高(cm)/体重(kg)用；血压时存收缩压
     */
    private BigDecimal value;

    /**
     * 仅血压使用，存舒张压；身高体重时为 null
     */
    private BigDecimal valueSecondary;

    /**
     * 记的是哪一天，不是插入时间
     */
    private LocalDate recordedAt;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

}
