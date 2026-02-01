package asia.sweethome.family.kinship;

/**
 * 亲属称谓计算结果。两人不在同一家庭关系图连通分量中（无路径）时，两个字段均为 null。
 */
public record RelationResult(String relationCode, String relationLabel) {

    public static final RelationResult NONE = new RelationResult(null, null);
}
