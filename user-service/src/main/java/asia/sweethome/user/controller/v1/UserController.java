package asia.sweethome.user.controller.v1;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

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

/**
 * 【用户控制器】
 * <p>
 * 面向前端 App 的用户接口，目前提供「查看/修改我自己的资料」。
 * 这里的「我是谁」不来自请求参数，而来自 {@link UserContext}——网关校验 Token 后把用户 id
 * 放进请求头，拦截器再存进 UserContext，避免前端伪造别人的 userId。
 * <p>
 * {@code @Tag / @Operation} 是 Swagger/OpenAPI 注解，用于自动生成接口文档，对运行无影响。
 *
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

    /** 获取当前登录用户的完整资料（含家庭信息） */
    @GetMapping("/me")
    @Operation(summary = "获取当前登录用户个人信息")
    public Result<UserDetailVO> getMyInfo() {
        Long userId = UserContext.getUserId();   // 当前登录者，由网关+拦截器注入
        return Result.success(buildDetail(userId));
    }

    /** 更新当前登录用户资料，更新后返回最新完整资料 */
    @PutMapping("/me")
    @Operation(summary = "更新当前登录用户个人信息")
    public Result<UserDetailVO> updateMyInfo(@RequestBody UserUpdateDTO userUpdateDTO) {
        Long userId = UserContext.getUserId();
        usersService.updateProfile(userId, userUpdateDTO.getName(), userUpdateDTO.getAvatarUrl());
        return Result.success(buildDetail(userId));
    }

    /**
     * 把「用户基本信息 + 家庭信息（家庭名/角色/性别）」拼装成一个完整的 UserDetailVO。
     * 用户自身字段查本地库，家庭相关字段远程调 family-service。
     */
    private UserDetailVO buildDetail(Long userId) {
        User user = usersService.getById(userId);

        UserDetailVO vo = new UserDetailVO();
        vo.setUserId(user.getId());
        vo.setName(user.getName());
        vo.setPhone(user.getPhone());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setBalance(user.getBalance());

        // 家庭相关信息来自 family-service（用户必属于某个家庭，否则这些调用会抛业务异常）
        FamilyDTO family = familyApi.getFamilyByUserId(userId);
        vo.setFamilyId(family.getId());
        vo.setFamilyName(family.getName());
        vo.setRole(familyApi.getFamilyRoleByUserId(userId));
        vo.setGender(familyApi.getGenderByUserId(userId));

        return vo;
    }

}
