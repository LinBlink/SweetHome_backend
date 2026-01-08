package asia.sweethome.user.controller.v1;

import asia.sweethome.common.entity.vo.Result;
import asia.sweethome.user.entity.vo.UserInfoVO;
import asia.sweethome.user.service.IUsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 6/30/2026 2:14 PM
 */
@RestController
@RequestMapping("/v1/users")
@Tag(name = "用户控制器")
@RequiredArgsConstructor
public class UserController {

    private final IUsersService usersService;

    @GetMapping("/me")
    @Operation(summary = "获取当前登录用户个人信息")
    public Result<UserInfoVO> getMyInfo(){
        return null;
    }

}
