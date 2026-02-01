package asia.sweethome.user.controller.v1;

import asia.sweethome.api.FamilyApi;
import asia.sweethome.api.entity.dto.FamilyDTO;
import asia.sweethome.common.context.UserContext;
import asia.sweethome.common.entity.vo.Result;
import asia.sweethome.user.entity.dto.UserUpdateDTO;
import asia.sweethome.user.entity.po.User;
import asia.sweethome.user.entity.vo.UserDetailVO;
import asia.sweethome.user.service.IUsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @DubboReference
    private FamilyApi familyApi;

    @GetMapping("/me")
    @Operation(summary = "获取当前登录用户个人信息")
    public Result<UserDetailVO> getMyInfo() {
        Long userId = UserContext.getUserId();
        return Result.success(buildDetail(userId));
    }

    @PutMapping("/me")
    @Operation(summary = "更新当前登录用户个人信息")
    public Result<UserDetailVO> updateMyInfo(@RequestBody UserUpdateDTO userUpdateDTO) {
        Long userId = UserContext.getUserId();
        usersService.updateProfile(userId, userUpdateDTO.getName(), userUpdateDTO.getAvatarUrl());
        return Result.success(buildDetail(userId));
    }

    private UserDetailVO buildDetail(Long userId) {
        User user = usersService.getById(userId);

        UserDetailVO vo = new UserDetailVO();
        vo.setUserId(user.getId());
        vo.setName(user.getName());
        vo.setPhone(user.getPhone());
        vo.setAvatarUrl(user.getAvatarUrl());

        FamilyDTO family = familyApi.getFamilyByUserId(userId);
        vo.setFamilyId(family.getId());
        vo.setFamilyName(family.getName());
        vo.setRole(familyApi.getFamilyRoleByUserId(userId));
        vo.setGender(familyApi.getGenderByUserId(userId));

        return vo;
    }

}
