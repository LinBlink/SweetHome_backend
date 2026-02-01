package asia.sweethome.common.constants;

/**
 * 【家庭成员之间的关系类型】
 * <p>
 * 把关系类型写成常量而不是到处敲字符串，可以避免拼写错误（写错编译期就报错），
 * 也让代码含义更清晰。这些值与数据库 family_relations 表的 relation_type 字段取值一致。
 *
 * @author: LOCRIAN_V
 * @date: 7/2/2026 11:19 上午
 */
public class RelationTypeConstants {
    public static final String CHILD_OF = "CHILD_OF";     // A 是 B 的孩子
    public static final String PARENT_OF = "PARENT_OF";   // A 是 B 的父/母
    public static final String SPOUSE_OF = "SPOUSE_OF";   // A 是 B 的配偶
    public static final String SIBLING_OF = "SIBLING_OF"; // A 是 B 的兄弟姐妹
}
