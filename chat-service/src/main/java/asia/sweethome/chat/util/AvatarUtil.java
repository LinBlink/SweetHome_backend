package asia.sweethome.chat.util;

/**
 * 【头像视觉工具】
 * <p>
 * 用户没设头像时，用「名字首字 + 一个背景色」当默认头像（很多 App 都这么做）。
 * 纯展示用途，不含任何业务语义。
 */
public final class AvatarUtil {

    // ARGB 十六进制调色板，挑了一组区分度较高的颜色，避免相邻头像颜色太接近
    private static final String[] PALETTE = {
            "FFBF5E3B", "FFF4A261", "FF2A9D8F", "FF457B9D",
            "FF6A4C93", "FFE76F51", "FF386641", "FFB56576"
    };

    private AvatarUtil() {
    }

    /** 取名字第一个字作为头像文字；名字为空则用「?」 */
    public static String label(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        return name.trim().substring(0, 1);
    }

    /**
     * 由 seed（用户 id 或会话 id）确定性地选一个颜色：同一个人每次都是同一色，界面才稳定。
     * 用「取余」把任意大的 id 映射到调色板下标；Math.abs 防止负数取余出问题。
     */
    public static String color(Long seed) {
        if (seed == null) {
            return PALETTE[0];
        }
        int index = (int) (Math.abs(seed) % PALETTE.length);
        return PALETTE[index];
    }
}
