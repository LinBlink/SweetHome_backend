package asia.sweethome.auth.util;

import asia.sweethome.api.entity.dto.UserRegisterDTO;
import asia.sweethome.auth.entity.dto.LoginRequestDTO;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;

import java.util.regex.Pattern;

/**
 * @description: 有效性验证工具
 * @author: LOCRIAN_V
 * @date: 7/1/2026 11:15 上午
 */
public class ValidationUtil {

    // 手机号需以国家码开头（如 +8613800138000），与前端 PhoneInputField 拼接格式对齐，兼容 users.phone VARCHAR(20)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{6,18}$");

    // 邀请码：8 位大写字母 + 数字，见 family-service 生成规则
    private static final Pattern INVITE_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{8}$");

    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final int PASSWORD_MAX_LENGTH = 20;

    private static final int NAME_MIN_LENGTH = 1;
    private static final int NAME_MAX_LENGTH = 50;

    private static final int FAMILY_NAME_MIN_LENGTH = 1;
    private static final int FAMILY_NAME_MAX_LENGTH = 100;

    public static Boolean validatePhoneNumber(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static Boolean validatePassword(String password) {
        return password != null
                && password.length() >= PASSWORD_MIN_LENGTH
                && password.length() <= PASSWORD_MAX_LENGTH;
    }

    public static Boolean validateName(String name) {
        if (name == null) {
            return false;
        }
        int length = name.trim().length();
        return length >= NAME_MIN_LENGTH && length <= NAME_MAX_LENGTH;
    }

    public static Boolean validateFamilyName(String familyName) {
        if (familyName == null) {
            return false;
        }
        int length = familyName.trim().length();
        return length >= FAMILY_NAME_MIN_LENGTH && length <= FAMILY_NAME_MAX_LENGTH;
    }

    public static Boolean validateInviteCode(String inviteCode) {
        return inviteCode != null && INVITE_CODE_PATTERN.matcher(inviteCode).matches();
    }

    public static Boolean validateGender(String gender) {
        return "male".equals(gender) || "female".equals(gender);
    }

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

        // familyName 与 inviteCode 二选一：创建家庭 或 加入家庭，不可同时提供或都不提供
        if (hasFamilyName == hasInviteCode) {
            return false;
        }

        if (hasFamilyName) {
            return validateFamilyName(familyName);
        }

        // 加入家庭：relationToMemberId / relationType 必填
        return validateInviteCode(inviteCode)
                && userRegisterDTO.getRelationToMemberId() != null
                && userRegisterDTO.getRelationType() != null
                && !userRegisterDTO.getRelationType().isBlank();
    }

    public static Boolean validateLoginRequestDTO(LoginRequestDTO loginRequestDTO) {

        if (loginRequestDTO == null) {
            return false;
        }

        String phone = loginRequestDTO.getPhone();
        String password = loginRequestDTO.getPassword();

        Boolean phoneCorrect = validatePhoneNumber(phone);
        Boolean passwordCorrect = validatePassword(password);

        if (!phoneCorrect) {
            throw new BusinessException(
                    ErrorCode.PHONE_FORMAT_NOT_VALID
            );
        }

        if (!passwordCorrect) {
            throw new BusinessException(
                    ErrorCode.PASSWORD_FORMAT_NOT_VALID
            );
        }

        return true;
    }


}
