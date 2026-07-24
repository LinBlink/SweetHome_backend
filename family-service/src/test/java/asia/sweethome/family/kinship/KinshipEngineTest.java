package asia.sweethome.family.kinship;

import asia.sweethome.common.constants.RelationTypeConstants;
import asia.sweethome.common.constants.UserConstants;
import asia.sweethome.family.entity.po.FamilyMember;
import asia.sweethome.family.entity.po.FamilyRelation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
        RelationResult result = engine.computeRelation(List.of(), members, 1L, 1L, "en");
        assertThat(result.relationCode()).isEqualTo("SELF");
    }

    @Test
    void childCallFather() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.FEMALE)
        );
        List<FamilyRelation> relations = List.of(parentOf(1L, 2L));
        RelationResult result = engine.computeRelation(relations, members, 2L, 1L, "en");
        assertThat(result.relationCode()).isEqualTo("F");
    }

    @Test
    void childCallMother() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.FEMALE),
                2L, member(2L, UserConstants.MALE)
        );
        List<FamilyRelation> relations = List.of(parentOf(1L, 2L));
        RelationResult result = engine.computeRelation(relations, members, 2L, 1L, "en");
        assertThat(result.relationCode()).isEqualTo("M");
    }

    @Test
    void fatherCallSon() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.MALE)
        );
        List<FamilyRelation> relations = List.of(parentOf(1L, 2L));
        RelationResult result = engine.computeRelation(relations, members, 1L, 2L, "en");
        assertThat(result.relationCode()).isEqualTo("Son");
    }

    @Test
    void fatherCallDaughter() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.FEMALE)
        );
        List<FamilyRelation> relations = List.of(parentOf(1L, 2L));
        RelationResult result = engine.computeRelation(relations, members, 1L, 2L, "en");
        assertThat(result.relationCode()).isEqualTo("Dau");
    }

    @Test
    void motherCallSon() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.FEMALE),
                2L, member(2L, UserConstants.MALE)
        );
        List<FamilyRelation> relations = List.of(parentOf(1L, 2L));
        RelationResult result = engine.computeRelation(relations, members, 1L, 2L, "en");
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
        RelationResult result = engine.computeRelation(relations, members, 3L, 1L, "en");
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
        RelationResult result = engine.computeRelation(relations, members, 3L, 1L, "en");
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
        RelationResult result = engine.computeRelation(relations, members, 3L, 1L, "en");
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
        RelationResult result = engine.computeRelation(relations, members, 4L, 1L, "en");
        assertThat(result.relationCode()).isEqualTo("F.F.F");
    }

    // ────── 无路经 ──────

    @Test
    void noRelation() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.FEMALE)
        );
        RelationResult result = engine.computeRelation(List.of(), members, 1L, 2L, "en");
        assertThat(result.relationCode()).isNull();
    }

    @Test
    void nullParams() {
        RelationResult result = engine.computeRelation(List.of(), Map.of(), null, 1L, "en");
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
        RelationResult result = engine.computeRelation(relations, members, 1L, 2L, "en");
        assertThat(result.relationCode()).isEqualTo("S");
    }

    @Test
    void wifeCallHusband() {
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.FEMALE),
                2L, member(2L, UserConstants.MALE)
        );
        List<FamilyRelation> relations = List.of(spouseOf(1L, 2L));
        RelationResult result = engine.computeRelation(relations, members, 1L, 2L, "en");
        assertThat(result.relationCode()).isEqualTo("S");
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
        RelationResult result = engine.computeRelation(relations, members, 3L, 2L, "en");
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
        RelationResult result = engine.computeRelation(relations, members, 2L, 3L, "en");
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
        RelationResult result = engine.computeRelation(relations, members, 3L, 2L, "en");
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
        RelationResult result = engine.computeRelation(relations, members, 2L, 3L, "en");
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
        RelationResult result = engine.computeRelation(relations, members, 3L, 2L, "en");
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
        RelationResult result = engine.computeRelation(relations, members, 3L, 2L, "en");
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
        RelationResult result = engine.computeRelation(relations, members, 4L, 2L, "en");
        assertThat(result.relationCode()).isEqualTo("F.eB");
    }

    @Test
    void childViaSpouse() {
        // 我(1, male) → 配偶(2, female) → 继子(3, male)
        // 我 → 继子: 先 S 到配偶, 再 Son 到孩子 = S.Son
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.FEMALE),
                3L, member(3L, UserConstants.MALE)
        );
        List<FamilyRelation> relations = List.of(
                spouseOf(1L, 2L),
                parentOf(2L, 3L)
        );
        RelationResult result = engine.computeRelation(relations, members, 1L, 3L, "en");
        assertThat(result.relationCode()).isEqualTo("S.Son");
    }

    @Test
    void mothersHusbandIsStepFather() {
        // 妈妈(1, female) + 继父(2, male) = 配偶, 妈妈(1) → 我(3)
        // 我 → 继父 = M.S = 母亲的配偶
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.FEMALE),
                2L, member(2L, UserConstants.MALE),
                3L, member(3L, UserConstants.FEMALE)
        );
        List<FamilyRelation> relations = List.of(
                spouseOf(1L, 2L),
                parentOf(1L, 3L)
        );
        RelationResult result = engine.computeRelation(relations, members, 3L, 2L, "en");
        assertThat(result.relationCode()).isEqualTo("M.S");
    }

    @Test
    void grandparentInLaw() {
        // 我(1,male) + 配偶(2,female) = 配偶, 配偶(2) → 岳父(3,male)
        // 我 → 岳父 = S.F
        Map<Long, FamilyMember> members = Map.of(
                1L, member(1L, UserConstants.MALE),
                2L, member(2L, UserConstants.FEMALE),
                3L, member(3L, UserConstants.MALE)
        );
        List<FamilyRelation> relations = List.of(
                spouseOf(1L, 2L),
                parentOf(3L, 2L)
        );
        RelationResult result = engine.computeRelation(relations, members, 1L, 3L, "en");
        assertThat(result.relationCode()).isEqualTo("S.F");
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
        // 我(5) → 表弟 = F.S.F.Son = eB/yB

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

        assertThat(engine.computeRelation(relations, members, 5L, 1L, "en").relationCode()).isEqualTo("F.F");
        assertThat(engine.computeRelation(relations, members, 5L, 6L, "en").relationCode()).isEqualTo("eZ");
        // F.F.Dau.Son → F + (F.Dau→yZ) + Son → F.yZ.Son = 爸爸的妹妹的兒子 = 表弟
        assertThat(engine.computeRelation(relations, members, 5L, 9L, "en").relationCode()).isEqualTo("F.yZ.Son");
        assertThat(engine.computeRelation(relations, members, 5L, 5L, "en").relationCode()).isEqualTo("SELF");
    }
}
