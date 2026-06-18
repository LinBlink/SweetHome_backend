package asia.sweethome.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

import asia.sweethome.user.entity.dto.PushTokenDTO;
import asia.sweethome.user.entity.po.PushToken;
import asia.sweethome.user.mapper.PushTokensMapper;
import asia.sweethome.user.service.IPushTokensService;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-14
 */
@Service
public class PushTokensServiceImpl extends ServiceImpl<PushTokensMapper, PushToken> implements IPushTokensService {

    @Override
    public void pushToken(Long userId, PushTokenDTO dto) {

        // 查看该设备有没有被注册过

        PushToken one = lambdaQuery().eq(
                PushToken::getRegistrationId,
                dto.getRegistrationId()
        ).one();

        if (one != null) {

            // 如果被注册过了，就更新。
            // 注册过了不能直接返回，因为可能不是同一个用户

            one.setUserId(userId);
            one.setPlatform(dto.getPlatform());

            updateById( one );

            return;
        }

        // 如果没被注册过，就创建

        PushToken pushToken = new PushToken();
        pushToken.setUserId(userId);
        pushToken.setRegistrationId(dto.getRegistrationId());
        pushToken.setPlatform(dto.getPlatform());

        save(pushToken);

    }

    @Override
    public void deleteToken(Long userId, PushTokenDTO dto) {

        PushToken one = lambdaQuery().eq(
                PushToken::getUserId,
                userId
        ).eq(
                PushToken::getRegistrationId,
                dto.getRegistrationId()
        ).one();

        if (one == null) {
            return;
        }

        removeById(one.getId());

    }
}
