package asia.sweethome.user.controller.v1;

import asia.sweethome.common.entity.vo.Result;
import asia.sweethome.user.entity.vo.UploadVO;
import asia.sweethome.user.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/12/2026 6:25 PM
 */
@RestController
@RequestMapping("/v1/users/upload")
@Tag(name="文件上传控制器")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @Operation( description = "上传文件接口")
    @PostMapping
    public Result<UploadVO> uploadFile(
            @RequestParam("file")MultipartFile
file            ){
        // TODO
        return null;
    }


    @Operation( summary = "上传用户头像接口",
    description = "用户头像上传之前，前端要保证发来的头像文件是极致压缩后的格式webp")
    @PostMapping
    public Result<UploadVO> uploadAvatar(
            @RequestParam("file")MultipartFile avatarFile
    ){
        return Result.success(
                uploadService.uploadAvatar( avatarFile )
        );
    }



}
