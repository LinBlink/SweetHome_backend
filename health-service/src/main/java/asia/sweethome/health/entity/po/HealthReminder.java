package asia.sweethome.health.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 成员每日健康记录提醒设置
 *
 * @author LocrianFifth
 * @since 2026-07-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("health_reminders")
public class HealthReminder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 每日提醒时间点，如 20:00:00
     */
    private LocalTime remindTime;

    private Boolean enabled;

    /**
     * 今天有没有已经提醒过，避免调度任务在同一分钟内重复扫描到同一条时重复发送
     */
    private LocalDate lastRemindedDate;

    private LocalDateTime updatedAt;

}
