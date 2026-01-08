package asia.sweethome.user.service.dubbo;

import asia.sweethome.api.UserApi;
import asia.sweethome.api.entity.dto.UserDTO;
import asia.sweethome.user.service.IUsersService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 6/30/2026 3:59 PM
 */
@DubboService
@RequiredArgsConstructor
public class UserApiImpl implements UserApi {

    private final IUsersService usersService;

    @Override
    public UserDTO findUserById(Long id) {
        return null;
    }
}
