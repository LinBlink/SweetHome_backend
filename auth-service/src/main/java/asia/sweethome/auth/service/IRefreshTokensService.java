package asia.sweethome.auth.service;

import asia.sweethome.auth.entity.po.RefreshToken;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IRefreshTokensService extends IService<RefreshToken> {

    RefreshToken issue(Long userId, String rawRefreshToken, String deviceInfo, java.time.LocalDateTime expiresAt);

    /**
     * 校验 raw refresh token 是否有效（存在、未吊销、未过期），有效则返回对应记录，否则返回 null
     */
    RefreshToken validate(String rawRefreshToken);

    void revoke(String rawRefreshToken);
}
