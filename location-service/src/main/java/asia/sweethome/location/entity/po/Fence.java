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
@TableName("fence")
public class Fence implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private Long setterUserId;

    private Long familyId;

    private Long targetUserId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Double fenceLng;

    private Double fenceLat;

    private Double fenceRange;

    private LocalDateTime deletedAt;


}
