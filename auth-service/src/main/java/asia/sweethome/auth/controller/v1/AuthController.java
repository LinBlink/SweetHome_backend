package asia.sweethome.auth.controller.v1;

import asia.sweethome.api.entity.dto.UserRegisterDTO;
import asia.sweethome.auth.entity.dto.LoginRequestDTO;
import asia.sweethome.auth.entity.dto.TokenRefreshRequestDTO;
import asia.sweethome.auth.entity.vo.TokenRefreshResponseVO;
import asia.sweethome.auth.entity.vo.loginResponse.LoginResponseVO;
import asia.sweethome.auth.entity.vo.registerResponse.RegisterResponseVO;
import asia.sweethome.auth.service.AuthService;
import asia.sweethome.common.entity.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 6/29/2026 10:44 PM
 */
@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<RegisterResponseVO> register(
            @RequestBody UserRegisterDTO userRegisterDTO,
            @RequestHeader(value = "User-Agent", required = false) String userAgent
    ) {
        log.info("👮 新增用户注册请求 {}", userRegisterDTO);
        return Result.success(authService.register(userRegisterDTO, userAgent));
    }

    @PostMapping("/login")
    public Result<LoginResponseVO> login(
            @RequestBody LoginRequestDTO loginRequest,
            @RequestHeader(value = "User-Agent", required = false) String userAgent
    ) {
        return Result.success(authService.login(loginRequest, userAgent));
    }

    /**
     * 通过 refreshToken 得到新的 accessToken
     */
    @PostMapping("/refresh")
    public Result<TokenRefreshResponseVO> refreshToken(@RequestBody TokenRefreshRequestDTO request) {
        return Result.success(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody TokenRefreshRequestDTO request) {
        authService.logout(request.getRefreshToken());
        return Result.success();
    }

}
