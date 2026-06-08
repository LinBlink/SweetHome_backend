package asia.sweethome.api;

import java.util.List;

import asia.sweethome.api.entity.dto.UserDTO;
import asia.sweethome.api.entity.dto.UserRegisterDTO;
import asia.sweethome.api.entity.vo.UserInfoVO;

/**
 * 【user-service 对外暴露的 Dubbo 接口】
 * <p>
 * 主要给 auth-service 在「注册 / 登录」时调用，用来查用户、建用户。
 * （关于 Dubbo 接口的原理，见 {@link FamilyApi} 的说明。）
 *
 * @author: LOCRIAN_V
 * @date: 6/30/2026 3:52 PM
 */
public interface UserApi {

    // 按手机号查用户（连带其家庭信息），登录时用来核对账号；查不到返回 null
    UserDTO findUserAndFamilyByPhone(String phone);

    // 注册时创建新用户，返回用户基本信息
    UserInfoVO createUser(UserRegisterDTO userRegisterDTO);

    // 按用户 id 查单个用户
    UserDTO findUserById(Long userId);

    // 按一批用户 id 批量查用户（如群成员列表），避免逐个查询的 N 次网络往返
    List<UserDTO> findUsersByIds(List<Long> userIds);

}
