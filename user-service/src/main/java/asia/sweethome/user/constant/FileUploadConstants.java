package asia.sweethome.user.constant;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/12/2026 7:00 PM
 */
public class FileUploadConstants {

    public static final int MAX_AVATAR_SIZE = 500 * 1024; // Byte - 500KB
    public static final int MAX_PHOTO_SIZE =  1024 * 1024; // Byte - 1MB
    public static final int MAX_VIDEO_SIZE = 50 * 1024 * 1024; // Byte - 50MB
    public static final int MAX_AUDIO_SIZE = 10 * 1024 * 1024; // Byte - 10MB

    public static final String AVATAR_SAVE_LOCATION = "users/avatars";

    public static final String PHOTO_SAVE_LOCATION = "users/photos";

    public static final String VIDEO_SAVE_LOCATION = "users/videos";

    public static final String AUDIO_SAVE_LOCATION = "users/audios";

    public static final String FILE_SAVE_LOCATION = "users/files";

}
