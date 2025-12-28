package asia.sweethome.auth.controller.v1;

import asia.sweethome.auth.entity.dto.UserRegisterDTO;
import asia.sweethome.auth.service.AuthService;
import asia.sweethome.common.entity.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 6/29/2026 10:44 PM
 */
@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<Void> register(
            @RequestBody UserRegisterDTO userRegisterDTO
    ){
        log.info("👮‍ 新增用户注册请求 {}", userRegisterDTO);
        authService.register( userRegisterDTO );
        return Result.success();
    }

}
