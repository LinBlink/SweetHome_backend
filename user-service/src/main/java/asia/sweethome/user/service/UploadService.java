package asia.sweethome.user.service;

import org.springframework.web.multipart.MultipartFile;

import asia.sweethome.user.entity.vo.UploadVO;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/12/2026 6:54 PM
 */
public interface UploadService {
    UploadVO uploadAvatar(MultipartFile avatarFile);

    UploadVO uploadImage(MultipartFile imageFile);

    UploadVO uploadVideo(MultipartFile videoFile);

    UploadVO uploadAudio(MultipartFile audioFile);
}
