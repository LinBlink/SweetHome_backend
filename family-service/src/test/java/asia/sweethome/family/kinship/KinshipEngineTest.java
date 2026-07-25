package asia.sweethome.family.kinship;

import asia.sweethome.common.constants.RelationTypeConstants;
import asia.sweethome.common.constants.UserConstants;
import asia.sweethome.family.entity.po.FamilyMember;
import asia.sweethome.family.entity.po.FamilyRelation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

// 用 Assertions 而不是 AssertionsForClassTypes：后者没有 Map/集合的断言重载，
// 而批量接口的测试要断言 map 的键集。
import static org.assertj.core.api.Assertions.assertThat;

class KinshipEngineTest {

    private final KinshipEngine engine = new KinshipEngine();

    // ────── 工具 ──────

    private static FamilyMember member(Long id, String gender, Integer birthOrder) {
        FamilyMember m = new FamilyMember();
        m.setId(id);
        m.setGender(gender);
        m.setBirthOrder(birthOrder);
        return m;
    }

    private static FamilyMember member(Long id, String gender) {
        return member(id, gender, null);
    }

    private static FamilyRelation parentOf(Long parentId, Long childId) {
        FamilyRelation r = new FamilyRelation();
        r.setRelationType(RelationTypeConstants.PARENT_OF);
        r.setSubjectMemberId(parentId);
        r.setObjectMemberId(childId);
        return r;
    }

    private static FamilyRelation spouseOf(Long a, Long b) {
        FamilyRelation r = new FamilyRelation();
        r.setRelationType(RelationTypeConstants.SPOUSE_OF);
        r.setSubjectMemberId(a);
        r.setObjectMemberId(b);
        return r;
    }

    // ────── 基础称谓 ──────

    @Test
    void self() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE)
        );
        RelationResult result = engine.computeRelation(List.of(), members, 1L, 1L);
        assertThat(result.relationCode()).isEqualTo("SELF");
    }

    @Test
    void childCallFather() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.FEMALE)
        );
        List<FamilyRelation> relations = List.of(parentOf(1L, 2L));
        RelationResult result = engine.computeRelation(relations, members, 2L, 1L);
        assertThat(result.relationCode()).isEqualTo("F");
    }

    @Test
    void childCallMother() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.FEMALE),
                2L, member(2L, UserConstants.MALE)
        );
        List<FamilyRelation> relations = List.of(parentOf(1L, 2L));
        RelationResult result = engine.computeRelation(relations, members, 2L, 1L);
        assertThat(result.relationCode()).isEqualTo("M");
    }

    @Test
    void fatherCallSon() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.MALE)
        );
        List<FamilyRelation> relations = List.of(parentOf(1L, 2L));
        RelationResult result = engine.computeRelation(relations, members, 1L, 2L);
        assertThat(result.relationCode()).isEqualTo("Son");
    }

    @Test
    void fatherCallDaughter() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.FEMALE)
        );
        List<FamilyRelation> relations = List.of(parentOf(1L, 2L));
        RelationResult result = engine.computeRelation(relations, members, 1L, 2L);
        assertThat(result.relationCode()).isEqualTo("Dau");
    }

    @Test
    void motherCallSon() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.FEMALE),
                2L, member(2L, UserConstants.MALE)
        );
        List<FamilyRelation> relations = List.of(parentOf(1L, 2L));
        RelationResult result = engine.computeRelation(relations, members, 1L, 2L);
        assertThat(result.relationCode()).isEqualTo("Son");
    }

    // ────── 多代 ──────

    @Test
    void paternalGrandfather() {
        // 爷爷(1) → 爸爸(2) → 我(3) → 我叫我爷爷 = F.F
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.MALE),
                3L, member(3L, UserConstants.MALE)
        );
        List<FamilyRelation> relations = List.of(
                parentOf(1L, 2L),
                parentOf(2L, 3L)
        );
        RelationResult result = engine.computeRelation(relations, members, 3L, 1L);
        assertThat(result.relationCode()).isEqualTo("F.F");
    }

    @Test
    void paternalGrandmother() {
        // 奶奶(1) → 爸爸(2) → 我(3) → 我叫我奶奶 = F.M
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.FEMALE),
                2L, member(2L, UserConstants.MALE),
                3L, member(3L, UserConstants.MALE)
        );
        List<FamilyRelation> relations = List.of(
                parentOf(1L, 2L),
                parentOf(2L, 3L)
        );
        RelationResult result = engine.computeRelation(relations, members, 3L, 1L);
        assertThat(result.relationCode()).isEqualTo("F.M");
    }

    @Test
    void maternalGrandfather() {
        // 外公(1) → 妈妈(2) → 我(3) → 我叫我外公 = M.F
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.FEMALE),
                3L, member(3L, UserConstants.MALE)
        );
        List<FamilyRelation> relations = List.of(
                parentOf(1L, 2L),
                parentOf(2L, 3L)
        );
        RelationResult result = engine.computeRelation(relations, members, 3L, 1L);
        assertThat(result.relationCode()).isEqualTo("M.F");
    }

    @Test
    void greatGrandfather() {
        // 曾祖父(1) → 爷爷(2) → 爸爸(3) → 我(4) → 我叫我曾祖父 = F.F.F
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.MALE),
                3L, member(3L, UserConstants.MALE),
                4L, member(4L, UserConstants.MALE)
        );
        List<FamilyRelation> relations = List.of(
                parentOf(1L, 2L),
                parentOf(2L, 3L),
                parentOf(3L, 4L)
        );
        RelationResult result = engine.computeRelation(relations, members, 4L, 1L);
        assertThat(result.relationCode()).isEqualTo("F.F.F");
    }

    // ────── 无路经 ──────

    @Test
    void noRelation() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.FEMALE)
        );
        RelationResult result = engine.computeRelation(List.of(), members, 1L, 2L);
        assertThat(result.relationCode()).isNull();
    }

    @Test
    void nullParams() {
        RelationResult result = engine.computeRelation(List.of(), Map.of(), null, 1L);
        assertThat(result.relationCode()).isNull();
    }

    // ────── 配偶 ──────

    @Test
    void husbandCallWife() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.FEMALE)
        );
        List<FamilyRelation> relations = List.of(spouseOf(1L, 2L));
        RelationResult result = engine.computeRelation(relations, members, 1L, 2L);
        assertThat(result.relationCode()).isEqualTo("Wi");
    }

    @Test
    void wifeCallHusband() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.FEMALE),
                2L, member(2L, UserConstants.MALE)
        );
        List<FamilyRelation> relations = List.of(spouseOf(1L, 2L));
        RelationResult result = engine.computeRelation(relations, members, 1L, 2L);
        assertThat(result.relationCode()).isEqualTo("Hu");
    }

    /**
     * 这两个方向以前算出来是同一个编码 "S"——「配偶的性别」在编码里彻底丢失了，
     * 前端只能靠额外传 gender 参数外加「婚姻是异性的」假设去反推。现在编码自己说清楚。
     */
    @Test
    void spouseTokenDistinguishesHusbandFromWife() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.FEMALE)
        );
        List<FamilyRelation> relations = List.of(spouseOf(1L, 2L));
        assertThat(engine.computeRelation(relations, members, 1L, 2L).relationCode())
                .isNotEqualTo(engine.computeRelation(relations, members, 2L, 1L).relationCode());
    }

    // ────── 同辈折叠（兄弟姐妹） ──────

    @Test
    void olderBrother() {
        // 爸爸(1) → 哥哥(2, birthOrder=1, male) → 我(3, birthOrder=2, male)
        // 我 → 哥哥 = F.Son = eB (哥哥年长)
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.MALE, 1),
                3L, member(3L, UserConstants.MALE, 2)
        );
        List<FamilyRelation> relations = List.of(
                parentOf(1L, 2L),
                parentOf(1L, 3L)
        );
        RelationResult result = engine.computeRelation(relations, members, 3L, 2L);
        assertThat(result.relationCode()).isEqualTo("eB");
    }

    @Test
    void youngerBrother() {
        // 爸爸(1) → 我(2, birthOrder=1, male) → 弟弟(3, birthOrder=2, male)
        // 我 → 弟弟 = F.Son = yB (弟弟年幼)
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.MALE, 1),
                3L, member(3L, UserConstants.MALE, 2)
        );
        List<FamilyRelation> relations = List.of(
                parentOf(1L, 2L),
                parentOf(1L, 3L)
        );
        RelationResult result = engine.computeRelation(relations, members, 2L, 3L);
        assertThat(result.relationCode()).isEqualTo("yB");
    }

    @Test
    void olderSister() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.FEMALE, 1),
                3L, member(3L, UserConstants.MALE, 2)
        );
        List<FamilyRelation> relations = List.of(
                parentOf(1L, 2L),
                parentOf(1L, 3L)
        );
        RelationResult result = engine.computeRelation(relations, members, 3L, 2L);
        assertThat(result.relationCode()).isEqualTo("eZ");
    }

    @Test
    void youngerSister() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.MALE, 1),
                3L, member(3L, UserConstants.FEMALE, 2)
        );
        List<FamilyRelation> relations = List.of(
                parentOf(1L, 2L),
                parentOf(1L, 3L)
        );
        RelationResult result = engine.computeRelation(relations, members, 2L, 3L);
        assertThat(result.relationCode()).isEqualTo("yZ");
    }

    @Test
    void siblingViaMother() {
        // 妈妈(1) → 姐姐(2, female, birthOrder=1) → 我(3, male, birthOrder=2)
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.FEMALE),
                2L, member(2L, UserConstants.FEMALE, 1),
                3L, member(3L, UserConstants.MALE, 2)
        );
        List<FamilyRelation> relations = List.of(
                parentOf(1L, 2L),
                parentOf(1L, 3L)
        );
        RelationResult result = engine.computeRelation(relations, members, 3L, 2L);
        assertThat(result.relationCode()).isEqualTo("eZ");
    }

    @Test
    void siblingWithoutBirthOrderDefaultsToElder() {
        // 未设 birthOrder 时默认 elder
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.MALE),
                3L, member(3L, UserConstants.MALE)
        );
        List<FamilyRelation> relations = List.of(
                parentOf(1L, 2L),
                parentOf(1L, 3L)
        );
        RelationResult result = engine.computeRelation(relations, members, 3L, 2L);
        assertThat(result.relationCode()).isEqualTo("eB");
    }

    // ────── 复杂场景 ──────

    @Test
    void uncleFathersBrother() {
        // 爷爷(1) → 大伯(2, male, birthOrder=1) + 爸爸(3, male, birthOrder=2) → 我(4)
        // 我 → 大伯 = F.Son = eB (大伯比爸爸年长)
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.MALE, 1),
                3L, member(3L, UserConstants.MALE, 2),
                4L, member(4L, UserConstants.MALE, 3)
        );
        List<FamilyRelation> relations = List.of(
                parentOf(1L, 2L),
                parentOf(1L, 3L),
                parentOf(3L, 4L)
        );
        RelationResult result = engine.computeRelation(relations, members, 4L, 2L);
        assertThat(result.relationCode()).isEqualTo("F.eB");
    }

    @Test
    void childViaSpouse() {
        // 我(1, male) → 妻子(2, female) → 继子(3, male)
        // 我 → 继子: 先 Wi 到妻子, 再 Son 到孩子 = Wi.Son
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.FEMALE),
                3L, member(3L, UserConstants.MALE)
        );
        List<FamilyRelation> relations = List.of(
                spouseOf(1L, 2L),
                parentOf(2L, 3L)
        );
        RelationResult result = engine.computeRelation(relations, members, 1L, 3L);
        assertThat(result.relationCode()).isEqualTo("Wi.Son");
    }

    @Test
    void mothersHusbandIsStepFather() {
        // 妈妈(1, female) + 继父(2, male) = 配偶, 妈妈(1) → 我(3)
        // 我 → 继父 = M.Hu = 母亲的丈夫
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.FEMALE),
                2L, member(2L, UserConstants.MALE),
                3L, member(3L, UserConstants.FEMALE)
        );
        List<FamilyRelation> relations = List.of(
                spouseOf(1L, 2L),
                parentOf(1L, 3L)
        );
        RelationResult result = engine.computeRelation(relations, members, 3L, 2L);
        assertThat(result.relationCode()).isEqualTo("M.Hu");
    }

    @Test
    void grandparentInLaw() {
        // 我(1,male) + 妻子(2,female) = 配偶, 妻子(2) → 岳父(3,male)
        // 我 → 岳父 = Wi.F。以前这里是 S.F，而 S.F 同时也是女方叫公公用的编码，
        // 靠前端额外传「我」的性别才能分开；现在 Wi.F=岳父 / Hu.F=公公，编码本身就不歧义了。
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.FEMALE),
                3L, member(3L, UserConstants.MALE)
        );
        List<FamilyRelation> relations = List.of(
                spouseOf(1L, 2L),
                parentOf(3L, 2L)
        );
        RelationResult result = engine.computeRelation(relations, members, 1L, 3L);
        assertThat(result.relationCode()).isEqualTo("Wi.F");
    }

    /** 同一张图，换成女方视角就是公公 Hu.F——两个称谓从此是两个编码 */
    @Test
    void parentInLawDiffersByMarriageSide() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),      // 丈夫
                2L, member(2L, UserConstants.FEMALE),    // 妻子
                3L, member(3L, UserConstants.MALE),      // 丈夫的爸爸 → 妻子叫公公
                4L, member(4L, UserConstants.MALE)       // 妻子的爸爸 → 丈夫叫岳父
        );
        List<FamilyRelation> relations = List.of(
                spouseOf(1L, 2L),
                parentOf(3L, 1L),
                parentOf(4L, 2L)
        );
        assertThat(engine.computeRelation(relations, members, 2L, 3L).relationCode()).isEqualTo("Hu.F");
        assertThat(engine.computeRelation(relations, members, 1L, 4L).relationCode()).isEqualTo("Wi.F");
    }

    // ────── 大型家庭图 ──────

    @Test
    void largeFamily() {
        // 构建一个三代家庭：
        // 爷爷(1,male) + 奶奶(2,female) = 配偶
        //    └─ 爸爸(3,male) + 妈妈(4,female) = 配偶
        //         ├─ 我(5,male, birthOrder=2)
        //         └─ 姐姐(6,female, birthOrder=1)
        //    └─ 姑姑(7,female) + 姑父(8,male) = 配偶
        //         └─ 表弟(9,male, birthOrder=1)
        // 我(5) → 爷爷 = F.F
        // 我(5) → 姐姐 = F.Dau = eZ
        // 我(5) → 表弟 = 走血亲（经姑姑）而不是姻亲（经姑父），见下方断言

        Map<Long, FamilyMember> members = Map.ofEntries(
                Map.entry(1L, member(1L, UserConstants.MALE)),
                Map.entry(2L, member(2L, UserConstants.FEMALE)),
                Map.entry(3L, member(3L, UserConstants.MALE, 1)),
                Map.entry(4L, member(4L, UserConstants.FEMALE)),
                Map.entry(5L, member(5L, UserConstants.MALE, 2)),
                Map.entry(6L, member(6L, UserConstants.FEMALE, 1)),
                Map.entry(7L, member(7L, UserConstants.FEMALE, 2)),
                Map.entry(8L, member(8L, UserConstants.MALE)),
                Map.entry(9L, member(9L, UserConstants.MALE, 1))
        );
        List<FamilyRelation> relations = List.of(
                spouseOf(1L, 2L),
                parentOf(1L, 3L),
                parentOf(1L, 7L),
                parentOf(2L, 3L),
                parentOf(2L, 7L),
                spouseOf(3L, 4L),
                parentOf(3L, 5L),
                parentOf(3L, 6L),
                parentOf(4L, 5L),
                parentOf(4L, 6L),
                spouseOf(7L, 8L),
                parentOf(7L, 9L),
                parentOf(8L, 9L)
        );

        assertThat(engine.computeRelation(relations, members, 5L, 1L).relationCode()).isEqualTo("F.F");
        assertThat(engine.computeRelation(relations, members, 5L, 6L).relationCode()).isEqualTo("eZ");
        // F.F.Dau.Son → F + (F.Dau→yZ) + Son → F.yZ.Son = 爸爸的妹妹的兒子 = 表弟
        assertThat(engine.computeRelation(relations, members, 5L, 9L).relationCode()).isEqualTo("F.yZ.Son");
        assertThat(engine.computeRelation(relations, members, 5L, 5L).relationCode()).isEqualTo("SELF");
    }

    // ────── 确定性：结果不能依赖 relations 的顺序 ──────

    /**
     * 这是本引擎最重要的一条测试。数据库查 relations 时没有 ORDER BY，MySQL 不保证返回顺序
     * （换执行计划、加索引、主从切换都可能变）。旧实现的 BFS 是「谁先摸到门把手谁进」，
     * 选出哪条路径直接取决于这个顺序——同一个家庭今天显示「表哥」明天显示「姐夫」，且线上无法复现。
     * <p>
     * 所以：把同一份关系数据用<b>所有</b>可能的顺序喂进去，每个成员的称谓都必须完全一致。
     */
    @Test
    void resultIsIndependentOfRelationOrder() {
        // 造一个带「偶环」的重组家庭，让血亲路径和姻亲路径等长，从而真正触发选路分歧：
        // 爷爷(1) → 爸爸(2) → 我(4)；爷爷(1) → 姑姑(3)；姑姑(3) + 姑父(5) 是配偶；姑父(5) → 表妹(6)
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.MALE, 1),
                3L, member(3L, UserConstants.FEMALE, 2),
                4L, member(4L, UserConstants.MALE),
                5L, member(5L, UserConstants.MALE),
                6L, member(6L, UserConstants.FEMALE)
        );
        List<FamilyRelation> relations = new ArrayList<>(List.of(
                parentOf(1L, 2L),
                parentOf(1L, 3L),
                parentOf(2L, 4L),
                spouseOf(3L, 5L),
                parentOf(5L, 6L),
                parentOf(3L, 6L)
        ));

        Map<Long, RelationResult> expected = engine.computeRelations(relations, members, 4L);
        // 先确认这张图确实算出了东西，否则下面的「顺序无关」是空断言
        assertThat(expected).hasSize(members.size());

        List<FamilyRelation> shuffled = new ArrayList<>(relations);
        for (int round = 0; round < 200; round++) {
            Collections.shuffle(shuffled, new Random(round));
            assertThat(engine.computeRelations(shuffled, members, 4L))
                    .as("第 %d 种顺序算出的称谓必须与基准一致", round)
                    .isEqualTo(expected);
        }
    }

    /** 同等长度时血亲路径优先于姻亲路径（API.md 11.3），且这一点不受输入顺序影响 */
    @Test
    void bloodPathWinsOverAffinalPathOfSameLength() {
        // 我(1) 的孩子(3)，同时也是我配偶(2) 的孩子。
        // 走血亲：Son（1 跳，0 条姻亲边）；走姻亲：S.Son（2 跳）。必须选血亲。
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.FEMALE),
                3L, member(3L, UserConstants.MALE)
        );
        List<FamilyRelation> relations = List.of(
                spouseOf(1L, 2L),
                parentOf(2L, 3L),
                parentOf(1L, 3L)
        );
        assertThat(engine.computeRelation(relations, members, 1L, 3L).relationCode()).isEqualTo("Son");
    }

    // ────── 折叠：「下一辈再上一辈」→ 配偶 ──────

    /**
     * 「我儿子的妈妈」其实就是「我配偶」。夫妻关系没登记 SPOUSE_OF（未婚生育、离异未清理、
     * 用户懒得填）但两个孩子的 PARENT_OF 都在时就会走到这条路径，旧实现只折叠了
     * 「上一辈再下一辈」，于是界面上会出现「我儿子的妈妈」这种四不像。
     */
    @Test
    void childThenParentCollapsesToSpouse() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),      // 我
                2L, member(2L, UserConstants.FEMALE),    // 孩子的妈，与我没有 SPOUSE_OF 记录
                3L, member(3L, UserConstants.MALE)       // 我们的儿子
        );
        List<FamilyRelation> relations = List.of(
                parentOf(1L, 3L),
                parentOf(2L, 3L)
        );
        // 折出来的配偶是女性 → Wi
        assertThat(engine.computeRelation(relations, members, 1L, 2L).relationCode()).isEqualTo("Wi");
    }

    /** 折叠可以级联：孩子的妈的爸 → Wi.F（岳父），而不是 Son.M.F */
    @Test
    void collapseCascadesThroughSpouse() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.FEMALE),
                3L, member(3L, UserConstants.MALE),
                4L, member(4L, UserConstants.MALE)       // 配偶的爸爸
        );
        List<FamilyRelation> relations = List.of(
                parentOf(1L, 3L),
                parentOf(2L, 3L),
                parentOf(4L, 2L)
        );
        assertThat(engine.computeRelation(relations, members, 1L, 4L).relationCode()).isEqualTo("Wi.F");
    }

    // ────── 性别缺失 → 中性 token，而不是猜成女性 ──────

    /**
     * 旧实现用 {@code MALE.equals(gender)} 判断，false 就一律当女性，于是 gender 为 null
     * （老数据、第三方登录没拿到）时会静默产出「妈妈」「女儿」「妻子」这类确定性的错误称谓。
     * 现在「不知道」有自己的 token，前端会显示中性文案。
     */
    @Test
    void unknownGenderProducesNeutralTokens() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, null),                    // 家长，性别未录
                2L, member(2L, UserConstants.MALE),      // 我
                3L, member(3L, null),                    // 孩子，性别未录
                4L, member(4L, null)                     // 配偶，性别未录
        );
        List<FamilyRelation> relations = List.of(
                parentOf(1L, 2L),
                parentOf(2L, 3L),
                spouseOf(2L, 4L)
        );

        assertThat(engine.computeRelation(relations, members, 2L, 1L).relationCode()).isEqualTo("P");
        assertThat(engine.computeRelation(relations, members, 2L, 3L).relationCode()).isEqualTo("C");
        assertThat(engine.computeRelation(relations, members, 2L, 4L).relationCode()).isEqualTo("S");
    }

    /** 性别未知的同辈用 eX/yX，不再一律算成姐妹 */
    @Test
    void unknownGenderSiblingUsesNeutralToken() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, null, 1),   // 兄或姐，性别未录
                3L, member(3L, UserConstants.MALE, 2)
        );
        List<FamilyRelation> relations = List.of(
                parentOf(1L, 2L),
                parentOf(1L, 3L)
        );
        assertThat(engine.computeRelation(relations, members, 3L, 2L).relationCode()).isEqualTo("eX");
        assertThat(engine.computeRelation(relations, members, 2L, 3L).relationCode()).isEqualTo("yB");
    }

    /** 性别取值非法（不是 male/female）与缺失同等对待 */
    @Test
    void invalidGenderIsTreatedAsUnknown() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, "MALE"),   // 大写，与数据库约定的小写不符
                2L, member(2L, UserConstants.MALE)
        );
        assertThat(engine.computeRelation(List.of(parentOf(1L, 2L)), members, 2L, 1L)
                .relationCode()).isEqualTo("P");
    }

    // ────── 脏数据防御 ──────

    /**
     * 幽灵节点：成员已退出家庭（membersById 里没有他）但关系行还在。
     * 旧实现的姻亲分支不做存在性校验，于是这个人还能被当成中转站走过去，
     * 最后 siblingToken 拿到 null、静默落进「默认年长」分支，产出一个错误称谓且不报错。
     */
    @Test
    void spouseEdgeToMissingMemberIsIgnored() {
        // 2 号已退出家庭，但 spouseOf(1,2) 和 parentOf(2,3) 都还在
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                3L, member(3L, UserConstants.MALE)
        );
        List<FamilyRelation> relations = List.of(
                spouseOf(1L, 2L),
                parentOf(2L, 3L)
        );
        // 不能借道幽灵成员 2 走到 3
        assertThat(engine.computeRelation(relations, members, 1L, 3L).relationCode()).isNull();
        // 也不能凭空冒出一个对 2 的称谓
        assertThat(engine.computeRelations(relations, members, 1L)).containsOnlyKeys(1L);
    }

    /** 自环（A 是 A 的父母）是脏数据，不能让它影响结果 */
    @Test
    void selfLoopRelationIsIgnored() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.MALE)
        );
        List<FamilyRelation> relations = List.of(
                parentOf(1L, 1L),
                parentOf(1L, 2L)
        );
        assertThat(engine.computeRelation(relations, members, 2L, 1L).relationCode()).isEqualTo("F");
    }

    /** 重复的关系行（同一条边录了两次）不应改变结果 */
    @Test
    void duplicateRelationRowsDoNotChangeResult() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.MALE)
        );
        assertThat(engine.computeRelation(
                List.of(parentOf(1L, 2L), parentOf(1L, 2L)), members, 2L, 1L
        ).relationCode()).isEqualTo("F");
    }

    // ────── 长幼判定：生日优先，排行兜底 ──────

    private static FamilyMember memberBorn(Long id, String gender, String birthDate, Integer birthOrder) {
        FamilyMember m = new FamilyMember();
        m.setId(id);
        m.setGender(gender);
        m.setBirthOrder(birthOrder);
        m.setBirthDate(birthDate == null ? null : LocalDate.parse(birthDate));
        return m;
    }

    /** 两边都有生日时用生日定长幼，不看 birthOrder */
    @Test
    void birthDateDecidesSeniority() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, memberBorn(2L, UserConstants.MALE, "1990-03-01", null),   // 年长
                3L, memberBorn(3L, UserConstants.MALE, "1995-08-20", null)
        );
        List<FamilyRelation> relations = List.of(parentOf(1L, 2L), parentOf(1L, 3L));

        assertThat(engine.computeRelation(relations, members, 3L, 2L).relationCode()).isEqualTo("eB");
        assertThat(engine.computeRelation(relations, members, 2L, 3L).relationCode()).isEqualTo("yB");
    }

    /**
     * 生日和 birthOrder 冲突时以生日为准——birthOrder 要用户理解「排行」的语义再手填，
     * 填错的概率比生日高得多。
     */
    @Test
    void birthDateWinsOverBirthOrder() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                // 生日说 2 号年长，birthOrder 说 3 号年长，故意让两者矛盾
                2L, memberBorn(2L, UserConstants.MALE, "1990-03-01", 2),
                3L, memberBorn(3L, UserConstants.MALE, "1995-08-20", 1)
        );
        List<FamilyRelation> relations = List.of(parentOf(1L, 2L), parentOf(1L, 3L));

        assertThat(engine.computeRelation(relations, members, 3L, 2L).relationCode()).isEqualTo("eB");
    }

    /** 只有一方填了生日 → 定不了长幼，退回 birthOrder */
    @Test
    void fallsBackToBirthOrderWhenBirthDateIncomplete() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, memberBorn(2L, UserConstants.MALE, "1990-03-01", 2),
                3L, memberBorn(3L, UserConstants.MALE, null, 1)   // 生日缺失
        );
        List<FamilyRelation> relations = List.of(parentOf(1L, 2L), parentOf(1L, 3L));

        // 生日不可比 → 用 birthOrder：3 号排行 1 更年长
        assertThat(engine.computeRelation(relations, members, 2L, 3L).relationCode()).isEqualTo("eB");
    }

    /** 生日相同（双胞胎且未填排行）→ 无法定长幼，回退默认「年长」 */
    @Test
    void identicalBirthDateFallsBackToElder() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, memberBorn(2L, UserConstants.MALE, "1990-03-01", null),
                3L, memberBorn(3L, UserConstants.MALE, "1990-03-01", null)
        );
        List<FamilyRelation> relations = List.of(parentOf(1L, 2L), parentOf(1L, 3L));

        assertThat(engine.computeRelation(relations, members, 3L, 2L).relationCode()).isEqualTo("eB");
        assertThat(engine.computeRelation(relations, members, 2L, 3L).relationCode()).isEqualTo("eB");
    }

    /** 生日相同但排行不同（双胞胎且填了排行）→ 用排行 */
    @Test
    void identicalBirthDateUsesBirthOrderWhenPresent() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, memberBorn(2L, UserConstants.MALE, "1990-03-01", 1),
                3L, memberBorn(3L, UserConstants.MALE, "1990-03-01", 2)
        );
        List<FamilyRelation> relations = List.of(parentOf(1L, 2L), parentOf(1L, 3L));

        assertThat(engine.computeRelation(relations, members, 3L, 2L).relationCode()).isEqualTo("eB");
        assertThat(engine.computeRelation(relations, members, 2L, 3L).relationCode()).isEqualTo("yB");
    }

    // ────── 批量接口 ──────

    /** computeRelations 必须和逐个调 computeRelation 得到完全一样的结果 */
    @Test
    void batchMatchesSingleQueries() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.FEMALE),
                3L, member(3L, UserConstants.MALE, 1),
                4L, member(4L, UserConstants.FEMALE, 2)
        );
        List<FamilyRelation> relations = List.of(
                spouseOf(1L, 2L),
                parentOf(1L, 3L),
                parentOf(2L, 3L),
                parentOf(1L, 4L),
                parentOf(2L, 4L)
        );

        Map<Long, RelationResult> batch = engine.computeRelations(relations, members, 3L);
        for (Long memberId : members.keySet()) {
            assertThat(batch.getOrDefault(memberId, RelationResult.NONE))
                    .as("成员 %d", memberId)
                    .isEqualTo(engine.computeRelation(relations, members, 3L, memberId));
        }
    }

    /** 不可达的成员不出现在批量结果里，viewer 自己是 SELF */
    @Test
    void batchOmitsUnreachableMembers() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.MALE),
                3L, member(3L, UserConstants.MALE)   // 孤立成员
        );
        Map<Long, RelationResult> batch = engine.computeRelations(List.of(parentOf(1L, 2L)), members, 2L);
        assertThat(batch).containsOnlyKeys(1L, 2L);
        assertThat(batch.get(2L).relationCode()).isEqualTo("SELF");
        assertThat(batch.get(1L).relationCode()).isEqualTo("F");
    }
}
