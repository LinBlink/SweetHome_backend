package asia.sweethome.auth.service;

import asia.sweethome.auth.entity.po.RefreshToken;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 【refresh token 服务接口】
 * <p>
 * 继承 MyBatis-Plus 的 {@link IService}，自带一批通用 CRUD 方法；这里再补充三个业务方法。
 * 实现见 {@link asia.sweethome.auth.service.impl.RefreshTokensServiceImpl}。
 */
public interface IRefreshTokensService extends IService<RefreshToken> {

    // 签发并记录一个 refresh token（存哈希）
    RefreshToken issue(Long userId, String rawRefreshToken, String deviceInfo, java.time.LocalDateTime expiresAt);

    /**
     * 校验 raw refresh token 是否有效（存在、未吊销、未过期），有效则返回对应记录，否则返回 null
     */
    RefreshToken validate(String rawRefreshToken);

    // 吊销一个 refresh token（登出时用）
    void revoke(String rawRefreshToken);
}
