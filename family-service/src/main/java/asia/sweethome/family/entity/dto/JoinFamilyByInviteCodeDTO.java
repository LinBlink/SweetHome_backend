package asia.sweethome.family.entity.dto;


import java.time.LocalDate;
import lombok.Data;

/**
 * 【加入家庭请求体】
 * <p>
 * POST /v1/families/join 的请求体。「我是谁」由登录态（UserContext）决定，不放在这里，
 * 所以这里只需要：用哪个邀请码、我的性别、和家里谁建立什么关系。
 *
 * @author: LOCRIAN_V
 * @date: 7/2/2026 10:49 上午
 */
@Data
public class JoinFamilyByInviteCodeDTO {
    private String inviteCode;        // 家庭邀请码
    private String gender;            // 我的性别 male/female
    private LocalDate birthDate;      // 我的出生日期，选填；用于判定同辈长幼（哥/弟、姐/妹）
    private Long relationToMemberId;  // 和家里哪位成员建立关系（对方的成员 id）
    private String relationType;      // 关系类型，见 RelationTypeConstants
}
