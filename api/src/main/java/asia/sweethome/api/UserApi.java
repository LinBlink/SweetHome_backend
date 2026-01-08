package asia.sweethome.api;

import asia.sweethome.api.entity.dto.UserDTO;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 6/30/2026 3:52 PM
 */
public interface UserApi {
    UserDTO findUserById( Long id );
}
