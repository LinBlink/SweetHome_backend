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
 * 【认证控制器】
 * <p>
 * Controller 是 Web 层的入口：负责「接收 HTTP 请求 → 调用 Service 处理 → 把结果包成 Result 返回」，
 * 本身不写业务逻辑。这里管账号相关的 4 个动作：注册、登录、刷新令牌、登出。
 * <ul>
 *   <li>{@code @RestController}：声明这是个返回 JSON 的控制器；</li>
 *   <li>{@code @RequestMapping("/v1/auth")}：这个类下所有接口的公共前缀是 /v1/auth；</li>
 *   <li>{@code @RequiredArgsConstructor}：Lombok 为所有 final 字段生成构造方法，配合 Spring
 *       完成「构造器注入」——即自动把 AuthService 实例塞进来，不用手写 @Autowired。</li>
 * </ul>
 *
 * @author: LOCRIAN_V
 * @date: 6/29/2026 10:44 PM
 */
@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    // 业务逻辑委托给 Service 层；final + @RequiredArgsConstructor 实现构造器注入
    private final AuthService authService;

    /**
     * 注册。
     * @param userRegisterDTO 请求体 JSON 自动映射成的对象（@RequestBody）
     * @param userAgent 浏览器/设备标识，从请求头读出，用于记录 refresh token 属于哪台设备；
     *                  required=false 表示没这个头也不报错
     */
    @PostMapping("/register")
    public Result<RegisterResponseVO> register(
            @RequestBody UserRegisterDTO userRegisterDTO,
            @RequestHeader(value = "User-Agent", required = false) String userAgent
    ) {
        log.info("👮 新增用户注册请求 {}", userRegisterDTO);
        return Result.success(authService.register(userRegisterDTO, userAgent));
    }

    /** 登录：核对手机号 + 密码，成功后下发 accessToken / refreshToken */
    @PostMapping("/login")
    public Result<LoginResponseVO> login(
            @RequestBody LoginRequestDTO loginRequest,
            @RequestHeader(value = "User-Agent", required = false) String userAgent
    ) {
        return Result.success(authService.login(loginRequest, userAgent));
    }

    /**
     * 刷新令牌：accessToken 有效期短（15 分钟），过期后不必重新登录，
     * 拿有效期长（30 天）的 refreshToken 来这里换一个新的 accessToken。
     */
    @PostMapping("/refresh")
    public Result<TokenRefreshResponseVO> refreshToken(@RequestBody TokenRefreshRequestDTO request) {
        return Result.success(authService.refresh(request.getRefreshToken()));
    }

    /** 登出：把这个 refreshToken 标记为已吊销，之后就不能再用它换新令牌 */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody TokenRefreshRequestDTO request) {
        authService.logout(request.getRefreshToken());
        return Result.success();
    }

}
