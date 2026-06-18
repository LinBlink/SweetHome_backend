package asia.sweethome.user.service;

import com.baomidou.mybatisplus.extension.service.IService;

import asia.sweethome.user.entity.dto.PushTokenDTO;
import asia.sweethome.user.entity.po.PushToken;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-14
 */
public interface IPushTokensService extends IService<PushToken> {

    void pushToken(Long userId, PushTokenDTO dto);

    void deleteToken(Long userId, PushTokenDTO dto);
}
