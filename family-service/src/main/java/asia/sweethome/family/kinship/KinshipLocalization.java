package asia.sweethome.family.kinship;

import java.util.HashMap;
import java.util.Map;

/**
 * relationCode -&gt; relationLabel。
 * <p>
 * relationLabel 统一返回精确、可解析的英文短语（不再按 Accept-Language 做服务端本地化）——
 * 前端拿到这个英文短语后自行按目标语言映射为本地化文案。为了让前端能可靠地区分"姐姐/妹妹"、
 * "哥哥/弟弟"这类中文里必须区分长幼的称谓，这里的英文一律带 Older/Younger 限定词，绝不使用
 * 裸的 "Sister"/"Brother"。
 */
public final class KinshipLocalization {

    private KinshipLocalization() {
    }

    // relationCode（深度 <=3 的常见组合，覆盖 doc 7.5 表格）-> 精确英文短语
    private static final Map<String, String> EXACT = new HashMap<>();
    // 兜底：单 token 基础词，用于拼接未在 EXACT 表中列出的（更深/更罕见的）relationCode
    private static final Map<String, String> BASE_WORDS = new HashMap<>();
    private static final String CONNECTOR = "'s ";

    static {
        EXACT.put("SELF", "Self");
        EXACT.put("F", "Father");
        EXACT.put("M", "Mother");
        EXACT.put("S", "Spouse");
        EXACT.put("Son", "Son");
        EXACT.put("Dau", "Daughter");
        EXACT.put("F.F", "Paternal Grandfather");
        EXACT.put("F.M", "Paternal Grandmother");
        EXACT.put("M.F", "Maternal Grandfather");
        EXACT.put("M.M", "Maternal Grandmother");
        EXACT.put("Son.Son", "Grandson (via Son)");
        EXACT.put("Son.Dau", "Granddaughter (via Son)");
        EXACT.put("Dau.Son", "Grandson (via Daughter)");
        EXACT.put("Dau.Dau", "Granddaughter (via Daughter)");
        EXACT.put("eB", "Older Brother");
        EXACT.put("yB", "Younger Brother");
        EXACT.put("eZ", "Older Sister");
        EXACT.put("yZ", "Younger Sister");
        EXACT.put("F.eB", "Uncle (Father's Older Brother)");
        EXACT.put("F.yB", "Uncle (Father's Younger Brother)");
        EXACT.put("F.eZ", "Aunt (Father's Older Sister)");
        EXACT.put("F.yZ", "Aunt (Father's Younger Sister)");
        EXACT.put("M.eB", "Uncle (Mother's Older Brother)");
        EXACT.put("M.yB", "Uncle (Mother's Younger Brother)");
        EXACT.put("M.eZ", "Aunt (Mother's Older Sister)");
        EXACT.put("M.yZ", "Aunt (Mother's Younger Sister)");
        EXACT.put("eB.Son", "Nephew (via Brother)");
        EXACT.put("eB.Dau", "Niece (via Brother)");
        EXACT.put("yB.Son", "Nephew (via Brother)");
        EXACT.put("yB.Dau", "Niece (via Brother)");
        EXACT.put("eZ.Son", "Nephew (via Sister)");
        EXACT.put("eZ.Dau", "Niece (via Sister)");
        EXACT.put("yZ.Son", "Nephew (via Sister)");
        EXACT.put("yZ.Dau", "Niece (via Sister)");
        EXACT.put("S.F", "Father-in-law");
        EXACT.put("S.M", "Mother-in-law");
        EXACT.put("S.eB", "Brother-in-law (Spouse's Older Brother)");
        EXACT.put("S.yB", "Brother-in-law (Spouse's Younger Brother)");
        EXACT.put("S.eZ", "Sister-in-law (Spouse's Older Sister)");
        EXACT.put("S.yZ", "Sister-in-law (Spouse's Younger Sister)");
        EXACT.put("Son.S", "Daughter-in-law");
        EXACT.put("Dau.S", "Son-in-law");
        // 常见深度 3
        EXACT.put("F.F.F", "Great-Grandfather (Paternal)");
        EXACT.put("F.F.M", "Great-Grandmother (Paternal)");
        EXACT.put("M.M.F", "Great-Grandfather (Maternal)");
        EXACT.put("M.M.M", "Great-Grandmother (Maternal)");
        EXACT.put("F.eB.Son", "Male Cousin (Paternal, Older)");
        EXACT.put("F.eB.Dau", "Female Cousin (Paternal, Older)");
        EXACT.put("F.yB.Son", "Male Cousin (Paternal, Younger)");
        EXACT.put("F.yB.Dau", "Female Cousin (Paternal, Younger)");
        EXACT.put("M.eZ.Son", "Male Cousin (Maternal, Older)");
        EXACT.put("M.eZ.Dau", "Female Cousin (Maternal, Older)");
        EXACT.put("M.yZ.Son", "Male Cousin (Maternal, Younger)");
        EXACT.put("M.yZ.Dau", "Female Cousin (Maternal, Younger)");

        BASE_WORDS.put("SELF", "Self");
        BASE_WORDS.put("F", "Father");
        BASE_WORDS.put("M", "Mother");
        BASE_WORDS.put("S", "Spouse");
        BASE_WORDS.put("Son", "Son");
        BASE_WORDS.put("Dau", "Daughter");
        BASE_WORDS.put("eB", "Older Brother");
        BASE_WORDS.put("yB", "Younger Brother");
        BASE_WORDS.put("eZ", "Older Sister");
        BASE_WORDS.put("yZ", "Younger Sister");
    }

    /**
     * acceptLanguage 目前不影响输出（relationLabel 统一为英文，前端自行本地化），
     * 参数仍然保留是为了不改动上游调用链（REST/Dubbo 的 Accept-Language 透传）。
     */
    public static String localize(String relationCode, String acceptLanguage) {
        String exact = EXACT.get(relationCode);
        if (exact != null) {
            return exact;
        }
        return fallback(relationCode);
    }

    /**
     * 通用兜底：按 base word 表 + 连接词逐 token 拼接，保证任意合法路径都有输出。
     */
    private static String fallback(String relationCode) {
        String[] tokens = relationCode.split("\\.");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            String word = BASE_WORDS.getOrDefault(tokens[i], tokens[i]);
            if (i > 0) {
                sb.append(CONNECTOR);
            }
            sb.append(word);
        }
        return sb.toString();
    }
}
