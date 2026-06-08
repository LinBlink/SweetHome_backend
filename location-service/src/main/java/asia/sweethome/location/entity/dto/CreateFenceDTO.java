package asia.sweethome.location.entity.dto;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/14/2026 3:00 PM
 */

import java.io.Serializable;

import lombok.Data;

@Data
public class CreateFenceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private Long targetUserId;

    private Double fenceLng;

    private Double fenceLat;

    private Double fenceRange;

}
