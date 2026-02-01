package asia.sweethome.common.constants;

/**
 * 【家庭内的成员角色】
 * <p>
 * 与数据库 family_members 表的 role 字段取值一致。管理员比普通成员多出一些权限
 * （如踢人、解散家庭），具体校验见 family-service。
 *
 * @author: LOCRIAN_V
 * @date: 6/30/2026 11:02 PM
 */
public class RoleConstants {
    public static final String FAMILY_ADMIN = "admin";    // 家庭管理员（通常是创建者）
    public static final String FAMILY_MEMBER = "member";  // 普通家庭成员
}
