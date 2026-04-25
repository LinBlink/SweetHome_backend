package asia.sweethome.user.service;

import asia.sweethome.user.entity.vo.UploadVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/12/2026 6:54 PM
 */
public interface UploadService {
    UploadVO uploadAvatar(MultipartFile avatarFile);
}
