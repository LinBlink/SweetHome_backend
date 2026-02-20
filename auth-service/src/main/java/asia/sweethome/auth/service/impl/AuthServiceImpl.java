package asia.sweethome.auth.service.impl;

import asia.sweethome.api.UserApi;
import asia.sweethome.api.entity.dto.UserDTO;
import asia.sweethome.api.entity.dto.UserRegisterDTO;
import asia.sweethome.api.entity.vo.UserInfoVO;
import asia.sweethome.auth.entity.dto.LoginRequestDTO;
import asia.sweethome.auth.entity.po.RefreshToken;
import asia.sweethome.auth.entity.vo.TokenRefreshResponseVO;
import asia.sweethome.auth.entity.vo.loginResponse.LoginResponseVO;
import asia.sweethome.auth.entity.vo.registerResponse.RegisterResponseVO;
import asia.sweethome.auth.service.AuthService;
import asia.sweethome.auth.service.IRefreshTokensService;
import asia.sweethome.auth.util.JwtUtil;
import asia.sweethome.auth.util.ValidationUtil;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 【认证业务实现】
 * <p>
 * 认证服务本身不管用户表，用户数据由 user-service 负责，这里通过 Dubbo（{@code @DubboReference}）
 * 远程调用它。本类聚焦三件事：密码加解密、JWT 令牌签发、refresh token 的落库与校验。
 *
 * @author: LOCRIAN_V
 * @date: 6/29/2026 10:45 PM
 */
@Service   // 交给 Spring 管理，并标识这是「业务层」组件
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;          // 密码加密器（BCrypt），见 SecurityConfig
    private final JwtUtil jwtUtil;                           // JWT 令牌的签发与解析工具
    private final IRefreshTokensService refreshTokensService;// refresh token 的数据库操作

    // @DubboReference：远程引用 user-service 暴露的 UserApi，用起来像本地对象
    @DubboReference
    private UserApi userApi;

    /**
     * 注册流程：校验参数 → 加密密码 → 远程建用户 → 签发双令牌 → 记录 refresh token → 返回。
     */
    @Override
    public RegisterResponseVO register(UserRegisterDTO userRegisterDTO, String deviceInfo) {

        // 1. 校验注册信息（手机号/密码/昵称格式、家庭参数二选一等），不合法直接拒绝
        if (!ValidationUtil.validateUserRegisterDTO(userRegisterDTO)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 2. 把明文密码换成 BCrypt 密文再往下传，保证密码永不明文存库
        userRegisterDTO.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));

        // 3. 远程调用 user-service 真正创建用户（同时会处理建家庭/入家庭）
        UserInfoVO userInfoVO = userApi.createUser(userRegisterDTO);

        // 4. 拿到新用户 id，拿不到说明创建异常
        Long userId = userInfoVO.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        // 5. 签发两个令牌：accessToken（短期，日常请求带它）+ refreshToken（长期，用来续期）
        String accessToken = jwtUtil.generateAccessToken(userId, userInfoVO.getPhone());
        String refreshToken = jwtUtil.generateRefreshToken(userId, userInfoVO.getPhone());

        // 6. 把 refreshToken 记录到数据库（存的是哈希值），登出/换令牌时要来这里核对
        refreshTokensService.issue(userId, refreshToken, deviceInfo, jwtUtil.refreshTokenExpiryFromNow());

        // 7. 组装返回给前端的结果
        RegisterResponseVO registerResponseVO = new RegisterResponseVO();
        registerResponseVO.setToken(accessToken);
        registerResponseVO.setRefreshToken(refreshToken);
        registerResponseVO.setUser(userInfoVO);

        log.info("👮 用户注册成功, userId={}", userId);

        return registerResponseVO;
    }

    /**
     * 登录流程：校验格式 → 按手机号查用户 → 比对密码 → 签发双令牌 → 返回。
     */
    @Override
    public LoginResponseVO login(LoginRequestDTO loginRequestDTO, String deviceInfo) {

        // 1. 校验手机号/密码格式（不合格会在工具方法内部抛出对应的业务异常）
        ValidationUtil.validateLoginRequestDTO(loginRequestDTO);

        // 2. 远程按手机号查用户（连带家庭信息）
        UserDTO user = userApi.findUserAndFamilyByPhone(loginRequestDTO.getPhone());

        // 3. 用户不存在，或密码比对不上，都统一报「手机号或密码错误」。
        //    注意：这里故意不区分「用户不存在」和「密码错误」，避免被人拿来探测哪些手机号已注册。
        //    passwordEncoder.matches(明文, 密文) 会用同样的算法算一遍再比较，绝不反解密文。
        if (user == null || !passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 4. 校验通过，签发双令牌并记录 refreshToken
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getPhone());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getPhone());

        refreshTokensService.issue(user.getId(), refreshToken, deviceInfo, jwtUtil.refreshTokenExpiryFromNow());

        // 5. 把用户信息转成「不含密码」的 VO 返回给前端
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setUserId(user.getId());
        userInfoVO.setName(user.getName());
        userInfoVO.setPhone(user.getPhone());
        userInfoVO.setFamilyId(user.getFamilyId());
        userInfoVO.setFamilyName(user.getFamilyName());
        userInfoVO.setRole(user.getRole());

        LoginResponseVO loginResponseVO = new LoginResponseVO();
        loginResponseVO.setToken(accessToken);
        loginResponseVO.setRefreshToken(refreshToken);
        loginResponseVO.setUser(userInfoVO);

        return loginResponseVO;
    }

    /**
     * 续期流程：解析并验签 refreshToken → 确认它确实是 refresh 类型 → 确认数据库里仍有效
     * （未吊销、未过期）→ 签发一个新的 accessToken 返回。
     */
    @Override
    public TokenRefreshResponseVO refresh(String refreshToken) {

        // 1. 用公钥验签并解析出内容；令牌被篡改或已过期会抛异常，统一转成「刷新令牌无效」
        Claims claims;
        try {
            claims = jwtUtil.parseToken(refreshToken);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 2. 防止有人拿 accessToken 冒充 refreshToken 来续期：必须是签发时标记的 refresh 类型
        String type = claims.get("type", String.class);
        if (!"refresh".equals(type)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 3. 令牌本身有效还不够，还要确认它没被登出吊销、数据库记录也没过期
        RefreshToken persisted = refreshTokensService.validate(refreshToken);
        if (persisted == null) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 4. 从令牌里取出用户 id、手机号，签发新的 accessToken（refreshToken 不变，继续用到它过期）
        Long userId = Long.valueOf(claims.getSubject());
        String phone = claims.get("phone", String.class);

        String newAccessToken = jwtUtil.generateAccessToken(userId, phone);

        return new TokenRefreshResponseVO(newAccessToken);
    }

    /**
     * 登出：把这个 refreshToken 在数据库里标记为「已吊销」，之后它就换不出新令牌了。
     * （已经发出去的 accessToken 因为是无状态的，会在最多 15 分钟后自然过期。）
     */
    @Override
    public void logout(String refreshToken) {
        refreshTokensService.revoke(refreshToken);
    }
}
