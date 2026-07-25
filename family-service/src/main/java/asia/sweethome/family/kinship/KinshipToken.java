package asia.sweethome.family.kinship;

/**
 * 【关系路径上的一步】见 doc/API.md 「十一、亲属称谓计算算法」11.2 / 11.4。
 * <p>
 * 一条亲属关系路径 = 若干个 token 拼起来，例如 F.F = 爸爸的爸爸 = 爷爷。
 * 之前这些 token 是散落各处的裸字符串（"F"、"Son"、"eB"），拼错了编译器也不会报错；
 * 收成枚举之后，写错名字编译期就挂，而 {@link #getCode()} 是唯一对外输出的字面量——
 * 前端（sh_flutter 的 RelToken）必须与这里的 code 逐字节一致，改动前务必两端同步。
 */
public enum KinshipToken {

    /** 向上一步，对方是男性 → 父 */
    FATHER("F", false),
    /** 向上一步，对方是女性 → 母 */
    MOTHER("M", false),
    /** 横向一步 → 配偶（姻亲边） */
    SPOUSE("S", true),
    /** 向下一步，对方是男性 → 儿子 */
    SON("Son", false),
    /** 向下一步，对方是女性 → 女儿 */
    DAUGHTER("Dau", false),

    // 以下 4 个不是「走一步」得到的，而是 reduce 折叠「上一辈再下一辈」的产物
    /** 哥哥 */
    ELDER_BROTHER("eB", false),
    /** 弟弟 */
    YOUNGER_BROTHER("yB", false),
    /** 姐姐 */
    ELDER_SISTER("eZ", false),
    /** 妹妹 */
    YOUNGER_SISTER("yZ", false);

    /** 输出到 relationCode 里的字面量，与前端 RelToken.code 一一对应 */
    private final String code;

    /** true=姻亲（走配偶边），false=血亲。选路时血亲优先，见 KinshipEngine 的 PATH_ORDER */
    private final boolean affinal;

    KinshipToken(String code, boolean affinal) {
        this.code = code;
        this.affinal = affinal;
    }

    public String getCode() {
        return code;
    }

    public boolean isAffinal() {
        return affinal;
    }

    /** 是否「向上一辈」的一步（父/母） */
    public boolean isUpStep() {
        return this == FATHER || this == MOTHER;
    }

    /** 是否「向下一辈」的一步（儿/女） */
    public boolean isDownStep() {
        return this == SON || this == DAUGHTER;
    }
}
