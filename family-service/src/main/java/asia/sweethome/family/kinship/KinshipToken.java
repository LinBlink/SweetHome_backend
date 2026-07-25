package asia.sweethome.family.kinship;

/**
 * 【关系路径上的一步】见 doc/API.md 「十一、亲属称谓计算算法」11.2 / 11.4。
 * <p>
 * 一条亲属关系路径 = 若干个 token 拼起来，例如 F.F = 爸爸的爸爸 = 爷爷。
 * 之前这些 token 是散落各处的裸字符串（"F"、"Son"、"eB"），拼错了编译器也不会报错；
 * 收成枚举之后，写错名字编译期就挂，而 {@link #getCode()} 是唯一对外输出的字面量——
 * 前端（sh_flutter 的 RelToken）必须与这里的 code 逐字节一致，改动前务必两端同步。
 *
 * <h3>为什么配偶要分 Hu / Wi</h3>
 * 原来只有一个中性的 {@code S}，于是「配偶的性别」这个信息在编码里彻底丢失了：
 * {@code S} 说不清是丈夫还是妻子，{@code S.F} 说不清是岳父（妻之父）还是公公（夫之父）。
 * 前端只好绕一圈，靠额外传入「我」的性别、再假定婚姻是异性的，反推出配偶性别——
 * 一旦路径中间出现配偶（如 {@code S.eB.Son}）这个补救就失效了，同性婚姻下更是直接算错。
 * 现在把性别直接编进 token：{@code Wi.F}=岳父、{@code Hu.F}=公公，编码自己就说清楚了。
 *
 * <h3>为什么还留着中性 token（P / C / S / eX / yX）</h3>
 * 性别可能压根没录（老数据、第三方登录没拿到）。旧实现在这种情况下用
 * {@code MALE.equals(gender)} 判断，false 就一律当女性——把「不知道」和「女性」混为一谈，
 * 静默产出错误称谓。现在「不知道」有自己的 token，前端会显示中性文案（如「配偶」「家长」），
 * 宁可含糊也不猜错。
 */
public enum KinshipToken {

    // ── 向上一辈 ──
    /** 父 */
    FATHER("F", false),
    /** 母 */
    MOTHER("M", false),
    /** 家长（性别未知） */
    PARENT("P", false),

    // ── 向下一辈 ──
    /** 儿子 */
    SON("Son", false),
    /** 女儿 */
    DAUGHTER("Dau", false),
    /** 孩子（性别未知） */
    CHILD("C", false),

    // ── 横向（姻亲边） ──
    /** 丈夫 */
    HUSBAND("Hu", true),
    /** 妻子 */
    WIFE("Wi", true),
    /** 配偶（性别未知） */
    SPOUSE("S", true),

    // ── 同辈：不是「走一步」得到的，而是 reduce 折叠「上一辈再下一辈」的产物 ──
    /** 哥哥 */
    ELDER_BROTHER("eB", false),
    /** 弟弟 */
    YOUNGER_BROTHER("yB", false),
    /** 姐姐 */
    ELDER_SISTER("eZ", false),
    /** 妹妹 */
    YOUNGER_SISTER("yZ", false),
    /** 年长的同辈（性别未知） */
    ELDER_SIBLING("eX", false),
    /** 年幼的同辈（性别未知） */
    YOUNGER_SIBLING("yX", false);

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

    /** 是否「向上一辈」的一步（父/母/家长） */
    public boolean isUpStep() {
        return this == FATHER || this == MOTHER || this == PARENT;
    }

    /** 是否「向下一辈」的一步（儿/女/孩子） */
    public boolean isDownStep() {
        return this == SON || this == DAUGHTER || this == CHILD;
    }
}
