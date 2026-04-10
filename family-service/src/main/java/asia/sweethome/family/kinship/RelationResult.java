package asia.sweethome.family.kinship;

/**
 * 亲属称谓计算结果。只产出关系编码 relationCode（如 "F.F"）；可读称谓由前端拿 code 自行本地化。
 * 两人不在同一家庭关系图连通分量中（无路径）时 relationCode 为 null。
 */
public record RelationResult(String relationCode) {

    public static final RelationResult NONE = new RelationResult(null);
}
