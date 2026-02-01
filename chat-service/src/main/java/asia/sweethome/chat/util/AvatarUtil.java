package asia.sweethome.chat.util;

/**
 * 头像视觉标识（首字 + 确定性颜色），纯展示用途，不含任何称谓语义。
 */
public final class AvatarUtil {

    // ARGB 十六进制调色板，避免相邻会话颜色过于接近
    private static final String[] PALETTE = {
            "FFBF5E3B", "FFF4A261", "FF2A9D8F", "FF457B9D",
            "FF6A4C93", "FFE76F51", "FF386641", "FFB56576"
    };

    private AvatarUtil() {
    }

    public static String label(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        return name.trim().substring(0, 1);
    }

    public static String color(Long seed) {
        if (seed == null) {
            return PALETTE[0];
        }
        int index = (int) (Math.abs(seed) % PALETTE.length);
        return PALETTE[index];
    }
}
