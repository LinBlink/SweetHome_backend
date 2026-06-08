package asia.sweethome.location.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("fence_alarm")
public class FenceAlarm implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long fenceId;

    /**
     * 目前只做入栏报警和出栏报警，另外两个以后做
     */
    private String alarmType;

    private LocalDateTime alarmedAt;

    private Long setterUserId;

    private Long targetUserId;

    private Long familyId;

    private LocalDateTime deletedAt;


}
