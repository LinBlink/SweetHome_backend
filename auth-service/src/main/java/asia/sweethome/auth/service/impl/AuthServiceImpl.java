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
 * @description:
 * @author: LOCRIAN_V
 * @date: 6/29/2026 10:45 PM
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final IRefreshTokensService refreshTokensService;

    @DubboReference
    private UserApi userApi;

    @Override
    public RegisterResponseVO register(UserRegisterDTO userRegisterDTO, String deviceInfo) {

        // 验证注册信息是否有效
        if (!ValidationUtil.validateUserRegisterDTO(userRegisterDTO)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 将密码变成密文
        userRegisterDTO.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));

        // 创建用户，调用 dubbo
        UserInfoVO userInfoVO = userApi.createUser(userRegisterDTO);

        // 得到用户 id
        Long userId = userInfoVO.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        // 根据用户信息和手机号生成 accessToken, refreshToken
        String accessToken = jwtUtil.generateAccessToken(userId, userInfoVO.getPhone());
        String refreshToken = jwtUtil.generateRefreshToken(userId, userInfoVO.getPhone());

        // 在后端记录 refreshToken
        refreshTokensService.issue(userId, refreshToken, deviceInfo, jwtUtil.refreshTokenExpiryFromNow());

        RegisterResponseVO registerResponseVO = new RegisterResponseVO();
        registerResponseVO.setToken(accessToken);
        registerResponseVO.setRefreshToken(refreshToken);
        registerResponseVO.setUser(userInfoVO);

        log.info("👮 用户注册成功, userId={}", userId);

        return registerResponseVO;
    }

    @Override
    public LoginResponseVO login(LoginRequestDTO loginRequestDTO, String deviceInfo) {

        ValidationUtil.validateLoginRequestDTO(loginRequestDTO);

        UserDTO user = userApi.findUserAndFamilyByPhone(loginRequestDTO.getPhone());

        if (user == null || !passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getPhone());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getPhone());

        refreshTokensService.issue(user.getId(), refreshToken, deviceInfo, jwtUtil.refreshTokenExpiryFromNow());

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

    @Override
    public TokenRefreshResponseVO refresh(String refreshToken) {

        Claims claims;
        try {
            claims = jwtUtil.parseToken(refreshToken);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        String type = claims.get("type", String.class);
        if (!"refresh".equals(type)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        RefreshToken persisted = refreshTokensService.validate(refreshToken);
        if (persisted == null) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        Long userId = Long.valueOf(claims.getSubject());
        String phone = claims.get("phone", String.class);

        String newAccessToken = jwtUtil.generateAccessToken(userId, phone);

        return new TokenRefreshResponseVO(newAccessToken);
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokensService.revoke(refreshToken);
    }
}
