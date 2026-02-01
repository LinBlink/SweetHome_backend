package asia.sweethome.api;

import asia.sweethome.api.entity.dto.UserDTO;
import asia.sweethome.api.entity.dto.UserRegisterDTO;
import asia.sweethome.api.entity.vo.UserInfoVO;

import java.util.List;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 6/30/2026 3:52 PM
 */
public interface UserApi {
    UserDTO findUserAndFamilyByPhone(String phone);

    UserInfoVO createUser(UserRegisterDTO userRegisterDTO);

    UserDTO findUserById(Long userId);

    List<UserDTO> findUsersByIds(List<Long> userIds);
}
