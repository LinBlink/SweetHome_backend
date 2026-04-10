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

    @Test
    void SonCallFatherTest(){

        FamilyMember father = new FamilyMember();
        FamilyMember son = new FamilyMember();

        father.setId(1L);
        father.setGender(
                UserConstants.MALE
        );

        son.setId(2L);
        son.setGender(
          UserConstants.FEMALE
        );

        Map<Long, FamilyMember> membersById = Map.of(1L, father, 2L, son);

        FamilyRelation rel = new FamilyRelation();

        rel.setRelationType(RelationTypeConstants.PARENT_OF);
        rel.setSubjectMemberId(1L);
        rel.setObjectMemberId(2L);
        List<FamilyRelation> relations = List.of(rel);

        KinshipEngine engine = new KinshipEngine();
        RelationResult result = engine.computeRelation(
                relations,
                membersById,
                2L,
                1L,
                "en"
        );

        assertThat(
                result.relationCode()
        ).isEqualTo("F");

    }

}