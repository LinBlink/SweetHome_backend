package asia.sweethome.user.entity.dto;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/17/2026 6:13 下午
 */

import java.io.Serializable;

import lombok.Data;

@Data
public class PushTokenDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String registrationId;

    private String platform;

}