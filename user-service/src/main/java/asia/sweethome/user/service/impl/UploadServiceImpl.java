package asia.sweethome.user.service.impl;

import static asia.sweethome.user.constant.FileUploadConstants.AVATAR_SAVE_LOCATION;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

import asia.sweethome.common.context.UserContext;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.user.constant.FileUploadConstants;
import asia.sweethome.user.entity.po.User;
import asia.sweethome.user.entity.vo.UploadVO;
import asia.sweethome.user.service.IUsersService;
import asia.sweethome.user.service.UploadService;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/12/2026 6:54 PM
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UploadServiceImpl implements UploadService {


    @Value("${sh.r2.bucket-name}")
    private String r2BucketName;


    @Value("${sh.r2.public-base-url}")
    private String r2PublicBaseUrl;


    private final S3Client r2Client;
    private final IUsersService usersService;

    /**
     * 上传用户头像
     * @param avatarFile 头像文件
     * @return 返回值
     */
    @Override
    public UploadVO uploadAvatar(MultipartFile avatarFile) {

        // --- 验证环节

        // 用户验证
        Long userId = UserContext.getUserId();

        if (userId==null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 非空验证
        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_FILE );
        }

        // 类型验证
        String contentType = avatarFile.getContentType();
        if (StrUtil.isBlank(contentType) || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.FILE_TYPE_ILLEGAL);
        }

        // 大小验证
        if (avatarFile.getSize() > FileUploadConstants.MAX_AVATAR_SIZE) {
            throw new BusinessException(ErrorCode.FILE_SIZE_ILLEGAL);
        }

        // --- 上传环节

        String fileType =  FilenameUtils.getExtension(
                avatarFile.getOriginalFilename()
        );

        if (StrUtil.isBlank( fileType )) {
            throw new BusinessException( ErrorCode.FILE_TYPE_ILLEGAL );
        }

        fileType = fileType.toLowerCase();

        // 文件名就是用户id，很清楚，很大胆。
        String key = AVATAR_SAVE_LOCATION + "/" + userId + "/" + UUID.randomUUID() + "." + fileType;

        try {
            r2Client.putObject(
                    PutObjectRequest.builder()
                            .bucket( r2BucketName )
                            .key( key )
                            .contentType(avatarFile.getContentType() )
                            .build(),
                    RequestBody.fromInputStream(
                            avatarFile.getInputStream(),
                            avatarFile.getSize()
                    )
            );
        } catch (IOException e) {
            log.error("🚧文件上传出现异常", e);
            throw new BusinessException( ErrorCode.FILE_UPLOAD_ERROR );
        }

        String publicBaseUrl = r2PublicBaseUrl + "/" + key;

        UploadVO vo = new UploadVO();
        vo.setAddressReturn( publicBaseUrl );

        // 落库
        usersService.lambdaUpdate()
                .eq(User::getId, userId)
                .set(User::getAvatarUrl, publicBaseUrl)
                .update();


        return vo;
    }


}
