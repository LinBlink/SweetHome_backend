package asia.sweethome.auth.service.impl;

import asia.sweethome.auth.entity.po.RefreshToken;
import asia.sweethome.auth.mapper.RefreshTokensMapper;
import asia.sweethome.auth.service.IRefreshTokensService;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 【refresh token 的数据库管理】
 * <p>
 * 继承 MyBatis-Plus 的 {@link ServiceImpl}，白得 save/updateById/lambdaQuery 等常用增删改查方法，
 * 不用自己写 SQL。这里负责 refresh token 的签发落库、有效性校验、吊销。
 * <p>
 * 安全要点：数据库里存的是 token 的 SHA-256 哈希，而不是原始 token。即便库被脱，
 * 攻击者也拿不到能直接使用的 token（哈希不可逆）。
 */
@Service
public class RefreshTokensServiceImpl extends ServiceImpl<RefreshTokensMapper, RefreshToken> implements IRefreshTokensService {

    /**
     * 签发：新登录/注册时，把这个 refresh token 的哈希连同用户、设备、过期时间存一条记录。
     * （Javadoc 必须写在 @Override 之上，否则不会被识别为文档注释。）
     */
    @Override
    public RefreshToken issue(Long userId, String rawRefreshToken, String deviceInfo, LocalDateTime expiresAt) {
        RefreshToken entity = new RefreshToken();
        entity.setUserId(userId);
        entity.setTokenHash(hash(rawRefreshToken));   // 只存哈希，不存原文
        entity.setDeviceInfo(deviceInfo);
        entity.setExpiresAt(expiresAt);
        entity.setCreatedAt(LocalDateTime.now());
        save(entity);                                  // MyBatis-Plus：插入一条记录
        return entity;
    }

    /**
     * 校验：token 存在、未被吊销、未过期，三者都满足才算有效，返回记录；否则返回 null。
     */
    @Override
    public RefreshToken validate(String rawRefreshToken) {
        // 按哈希查这条记录（lambdaQuery 用方法引用写查询条件，避免手写列名拼错）
        RefreshToken entity = lambdaQuery()
                .eq(RefreshToken::getTokenHash, hash(rawRefreshToken))
                .one();

        if (entity == null) {
            return null;   // 查无此 token
        }

        // 已登出吊销 → 无效
        if (entity.getRevokedAt() != null) {
            return null;
        }

        // 过期时间缺失、或已早于当前时间 → 无效
        if (entity.getExpiresAt() == null || entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            return null;
        }
        return entity;
    }

    /**
     * 吊销：登出时把 revokedAt 置为当前时间。做了幂等判断——已吊销的就不再重复更新。
     */
    @Override
    public void revoke(String rawRefreshToken) {
        RefreshToken entity = lambdaQuery()
                .eq(RefreshToken::getTokenHash, hash(rawRefreshToken))
                .one();
        if (entity != null && entity.getRevokedAt() == null) {
            entity.setRevokedAt(LocalDateTime.now());
            updateById(entity);
        }
    }

    /** 把原始 token 算成 SHA-256 十六进制字符串，用作数据库存储与查询的键 */
    private String hash(String raw) {
        return DigestUtil.sha256Hex(raw);
    }
}
