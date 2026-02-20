package asia.sweethome.auth.service;

import asia.sweethome.api.entity.dto.UserRegisterDTO;
import asia.sweethome.auth.entity.dto.LoginRequestDTO;
import asia.sweethome.auth.entity.vo.TokenRefreshResponseVO;
import asia.sweethome.auth.entity.vo.loginResponse.LoginResponseVO;
import asia.sweethome.auth.entity.vo.registerResponse.RegisterResponseVO;

/**
 * 【认证业务接口】
 * <p>
 * 只定义「能做哪些事」，具体怎么做见实现类 {@link asia.sweethome.auth.service.impl.AuthServiceImpl}。
 * 面向接口编程的好处：Controller 依赖接口而非具体实现，将来换实现不影响上层。
 *
 * @author: LOCRIAN_V
 * @date: 6/29/2026 10:45 PM
 */
public interface AuthService {

    // 注册：deviceInfo 是设备标识，用于区分同一用户在不同设备上的 refresh token
    RegisterResponseVO register(UserRegisterDTO userRegisterDTO, String deviceInfo);

    // 登录
    LoginResponseVO login(LoginRequestDTO loginRequestDTO, String deviceInfo);

    // 用 refreshToken 换新的 accessToken
    TokenRefreshResponseVO refresh(String refreshToken);

    // 登出：吊销 refreshToken
    void logout(String refreshToken);
}
