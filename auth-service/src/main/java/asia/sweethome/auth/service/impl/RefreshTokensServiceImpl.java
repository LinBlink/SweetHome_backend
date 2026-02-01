package asia.sweethome.auth.service.impl;

import asia.sweethome.auth.entity.po.RefreshToken;
import asia.sweethome.auth.mapper.RefreshTokensMapper;
import asia.sweethome.auth.service.IRefreshTokensService;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RefreshTokensServiceImpl extends ServiceImpl<RefreshTokensMapper, RefreshToken> implements IRefreshTokensService {


    @Override
    /**
     * 签发 refresh token
     */
    public RefreshToken issue(Long userId, String rawRefreshToken, String deviceInfo, LocalDateTime expiresAt) {
        RefreshToken entity = new RefreshToken();
        entity.setUserId(userId);
        entity.setTokenHash(hash(rawRefreshToken));
        entity.setDeviceInfo(deviceInfo);
        entity.setExpiresAt(expiresAt);
        entity.setCreatedAt(LocalDateTime.now());
        save(entity);
        return entity;
    }


    @Override
    /**
     * 得到有效的 refreshToken
     */
    public RefreshToken validate(String rawRefreshToken) {
        RefreshToken entity = lambdaQuery()
                .eq(RefreshToken::getTokenHash, hash(rawRefreshToken))
                .one();

        if (entity == null) {
            return null;
        }

        // 如果被吊销了，无效
        if (entity.getRevokedAt() != null) {
            return null;
        }

        // 没有过期 或者 过期时间在现在之前
        if (entity.getExpiresAt() == null || entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            return null;
        }
        return entity;
    }


    @Override
    /**
     * 废除一个 refreshToken
     */
    public void revoke(String rawRefreshToken) {
        RefreshToken entity = lambdaQuery()
                .eq(RefreshToken::getTokenHash, hash(rawRefreshToken))
                .one();
        if (entity != null && entity.getRevokedAt() == null) {
            entity.setRevokedAt(LocalDateTime.now());
            updateById(entity);
        }
    }

    private String hash(String raw) {
        return DigestUtil.sha256Hex(raw);
    }
}
