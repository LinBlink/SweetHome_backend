package asia.sweethome.auth.service;

import asia.sweethome.auth.entity.dto.UserRegisterDTO;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 6/29/2026 10:45 PM
 */
public interface AuthService {
    void register(UserRegisterDTO userRegisterDTO);
}
