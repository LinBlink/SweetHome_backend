package asia.sweethome.auth.service;

import asia.sweethome.api.entity.dto.UserRegisterDTO;
import asia.sweethome.auth.entity.dto.LoginRequestDTO;
import asia.sweethome.auth.entity.vo.TokenRefreshResponseVO;
import asia.sweethome.auth.entity.vo.loginResponse.LoginResponseVO;
import asia.sweethome.auth.entity.vo.registerResponse.RegisterResponseVO;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 6/29/2026 10:45 PM
 */
public interface AuthService {

    RegisterResponseVO register(UserRegisterDTO userRegisterDTO, String deviceInfo);

    LoginResponseVO login(LoginRequestDTO loginRequestDTO, String deviceInfo);

    TokenRefreshResponseVO refresh(String refreshToken);

    void logout(String refreshToken);
}
