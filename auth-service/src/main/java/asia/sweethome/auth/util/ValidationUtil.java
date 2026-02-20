package asia.sweethome.auth.util;

import asia.sweethome.api.entity.dto.UserRegisterDTO;
import asia.sweethome.auth.entity.dto.LoginRequestDTO;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;

import java.util.regex.Pattern;

/**
 * 【参数校验工具】
 * <p>
 * 把「手机号/密码/昵称/邀请码是否合法」这类判断集中在一处，供注册、登录复用。
 * 所有方法都是 static（静态），不依赖对象状态，直接 {@code ValidationUtil.xxx(...)} 调用。
 * <p>
 * 命名小知识：Pattern（正则表达式）编译一次比较耗时，所以做成 static final 常量只编译一次、反复使用。
 *
 * @author: LOCRIAN_V
 * @date: 7/1/2026 11:15 上午
 */
public class ValidationUtil {

    // 手机号需以国家码开头（如 +8613800138000），与前端 PhoneInputField 拼接格式对齐，兼容 users.phone VARCHAR(20)
    // 正则含义：^\+ 以加号开头；[1-9] 国家码首位非 0；\d{6,18} 再跟 6~18 位数字；$ 结尾
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{6,18}$");

    // 邀请码：8 位大写字母 + 数字，见 family-service 生成规则
    private static final Pattern INVITE_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{8}$");

    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final int PASSWORD_MAX_LENGTH = 20;

    private static final int NAME_MIN_LENGTH = 1;
    private static final int NAME_MAX_LENGTH = 50;

    private static final int FAMILY_NAME_MIN_LENGTH = 1;
    private static final int FAMILY_NAME_MAX_LENGTH = 100;

    /** 手机号是否符合「+国家码+号码」格式 */
    public static Boolean validatePhoneNumber(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /** 密码长度是否在 6~20 之间 */
    public static Boolean validatePassword(String password) {
        return password != null
                && password.length() >= PASSWORD_MIN_LENGTH
                && password.length() <= PASSWORD_MAX_LENGTH;
    }

    /** 昵称去掉首尾空格后长度是否在 1~50 之间 */
    public static Boolean validateName(String name) {
        if (name == null) {
            return false;
        }
        int length = name.trim().length();   // trim() 去掉首尾空白，防止用户只输入空格
        return length >= NAME_MIN_LENGTH && length <= NAME_MAX_LENGTH;
    }

    /** 家庭名去掉首尾空格后长度是否在 1~100 之间 */
    public static Boolean validateFamilyName(String familyName) {
        if (familyName == null) {
            return false;
        }
        int length = familyName.trim().length();
        return length >= FAMILY_NAME_MIN_LENGTH && length <= FAMILY_NAME_MAX_LENGTH;
    }

    /** 邀请码是否为 8 位大写字母/数字 */
    public static Boolean validateInviteCode(String inviteCode) {
        return inviteCode != null && INVITE_CODE_PATTERN.matcher(inviteCode).matches();
    }

    /** 性别是否为 male / female */
    public static Boolean validateGender(String gender) {
        return "male".equals(gender) || "female".equals(gender);
    }

    /**
     * 校验整个注册请求是否合法（返回 true/false，由调用方决定如何处理）。
     * 除了各字段格式，还要保证「新建家庭」和「加入家庭」二者恰好选其一。
     */
    public static Boolean validateUserRegisterDTO(UserRegisterDTO userRegisterDTO) {

        if (userRegisterDTO == null) {
            return false;
        }

        String name = userRegisterDTO.getName();
        String phone = userRegisterDTO.getPhone();
        String password = userRegisterDTO.getPassword();
        String familyName = userRegisterDTO.getFamilyName();
        String inviteCode = userRegisterDTO.getInviteCode();

        if (!validateName(name) || !validatePhoneNumber(phone) || !validatePassword(password)
                || !validateGender(userRegisterDTO.getGender())) {
            return false;
        }

        boolean hasFamilyName = familyName != null && !familyName.trim().isEmpty();
        boolean hasInviteCode = inviteCode != null && !inviteCode.trim().isEmpty();

        // familyName 与 inviteCode 二选一：创建家庭 或 加入家庭，不可同时提供或都不提供。
        // 小技巧：两个 boolean 相等（同为 true 或同为 false）就说明「都填了」或「都没填」，均非法。
        if (hasFamilyName == hasInviteCode) {
            return false;
        }

        // 走「新建家庭」分支：只需家庭名合法
        if (hasFamilyName) {
            return validateFamilyName(familyName);
        }

        // 走「加入家庭」分支：邀请码合法，且必须说明「和谁、什么关系」
        return validateInviteCode(inviteCode)
                && userRegisterDTO.getRelationToMemberId() != null
                && userRegisterDTO.getRelationType() != null
                && !userRegisterDTO.getRelationType().isBlank();
    }

    /**
     * 校验登录请求。与注册不同，这里校验不通过会「直接抛出对应的业务异常」，
     * 好让前端能精准提示是手机号还是密码格式有问题（返回值恒为 true，仅表示通过）。
     */
    public static Boolean validateLoginRequestDTO(LoginRequestDTO loginRequestDTO) {

        if (loginRequestDTO == null) {
            return false;
        }

        String phone = loginRequestDTO.getPhone();
        String password = loginRequestDTO.getPassword();

        Boolean phoneCorrect = validatePhoneNumber(phone);
        Boolean passwordCorrect = validatePassword(password);

        if (!phoneCorrect) {
            throw new BusinessException(ErrorCode.PHONE_FORMAT_NOT_VALID);
        }

        if (!passwordCorrect) {
            throw new BusinessException(ErrorCode.PASSWORD_FORMAT_NOT_VALID);
        }

        return true;
    }

}
