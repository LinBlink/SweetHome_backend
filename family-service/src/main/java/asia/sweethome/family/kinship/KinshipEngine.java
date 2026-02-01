package asia.sweethome.family.kinship;

import asia.sweethome.common.constants.RelationTypeConstants;
import asia.sweethome.common.constants.UserConstants;
import asia.sweethome.family.entity.po.FamilyMemeber;
import asia.sweethome.family.entity.po.FamilyRelation;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * 亲属称谓计算算法实现，见 doc/api.md 「七、亲属称谓计算算法」。
 * <p>
 * 输入 family_relations 的原始边（PARENT_OF 有向 + SPOUSE_OF 无向），从 viewer 到 target
 * 做最短路径 BFS（血亲边优先于姻亲边出队），再对路径做同辈折叠化简，最后生成 relationCode
 * 并按语言本地化为 relationLabel。
 */
@Component
public class KinshipEngine {

    private static final String TOKEN_FATHER = "F";
    private static final String TOKEN_MOTHER = "M";
    private static final String TOKEN_SPOUSE = "S";
    private static final String TOKEN_SON = "Son";
    private static final String TOKEN_DAUGHTER = "Dau";

    private record Edge(Long to, String token, boolean blood) {
    }

    public RelationResult computeRelation(
            List<FamilyRelation> relations,
            Map<Long, FamilyMemeber> membersById,
            Long viewerMemberId,
            Long targetMemberId,
            String acceptLanguage
    ) {
        if (viewerMemberId == null || targetMemberId == null) {
            return RelationResult.NONE;
        }

        if (viewerMemberId.equals(targetMemberId)) {
            return new RelationResult("SELF", KinshipLocalization.localize("SELF", acceptLanguage));
        }

        Map<Long, List<Edge>> graph = buildGraph(relations, membersById);

        List<Long> nodes = new ArrayList<>();
        List<String> tokens = new ArrayList<>();
        boolean found = bfs(graph, viewerMemberId, targetMemberId, nodes, tokens);

        if (!found) {
            return RelationResult.NONE;
        }

        reduce(nodes, tokens, membersById);

        String relationCode = String.join(".", tokens);
        String relationLabel = KinshipLocalization.localize(relationCode, acceptLanguage);

        return new RelationResult(relationCode, relationLabel);
    }

    private Map<Long, List<Edge>> buildGraph(List<FamilyRelation> relations, Map<Long, FamilyMemeber> membersById) {
        Map<Long, List<Edge>> graph = new HashMap<>();

        // 先加全部血亲边（PARENT_OF），保证同一节点的邻接表中血亲边排在姻亲边之前
        for (FamilyRelation rel : relations) {
            if (!RelationTypeConstants.PARENT_OF.equals(rel.getRelationType())) {
                continue;
            }
            Long parentId = rel.getSubjectMemberId();
            Long childId = rel.getObjectMemberId();

            FamilyMemeber child = membersById.get(childId);
            FamilyMemeber parent = membersById.get(parentId);
            if (child == null || parent == null) {
                continue;
            }

            String downToken = UserConstants.MALE.equals(child.getGender()) ? TOKEN_SON : TOKEN_DAUGHTER;
            String upToken = UserConstants.MALE.equals(parent.getGender()) ? TOKEN_FATHER : TOKEN_MOTHER;

            graph.computeIfAbsent(parentId, k -> new ArrayList<>()).add(new Edge(childId, downToken, true));
            graph.computeIfAbsent(childId, k -> new ArrayList<>()).add(new Edge(parentId, upToken, true));
        }

        // 再加姻亲边（SPOUSE_OF），排在血亲边之后
        for (FamilyRelation rel : relations) {
            if (!RelationTypeConstants.SPOUSE_OF.equals(rel.getRelationType())) {
                continue;
            }
            Long a = rel.getSubjectMemberId();
            Long b = rel.getObjectMemberId();

            graph.computeIfAbsent(a, k -> new ArrayList<>()).add(new Edge(b, TOKEN_SPOUSE, false));
            graph.computeIfAbsent(b, k -> new ArrayList<>()).add(new Edge(a, TOKEN_SPOUSE, false));
        }

        return graph;
    }

    private boolean bfs(
            Map<Long, List<Edge>> graph,
            Long start,
            Long target,
            List<Long> outNodes,
            List<String> outTokens
    ) {
        Map<Long, Long> predecessor = new HashMap<>();
        Map<Long, String> predecessorToken = new HashMap<>();
        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new ArrayDeque<>();

        visited.add(start);
        queue.add(start);

        boolean found = start.equals(target);

        while (!queue.isEmpty() && !found) {
            Long current = queue.poll();
            for (Edge edge : graph.getOrDefault(current, List.of())) {
                if (visited.contains(edge.to())) {
                    continue;
                }
                visited.add(edge.to());
                predecessor.put(edge.to(), current);
                predecessorToken.put(edge.to(), edge.token());
                if (edge.to().equals(target)) {
                    found = true;
                    break;
                }
                queue.add(edge.to());
            }
        }

        if (!found) {
            return false;
        }

        // 从 target 回溯到 start，再反转
        List<Long> nodePath = new ArrayList<>();
        List<String> tokenPath = new ArrayList<>();
        Long cursor = target;
        nodePath.add(cursor);
        while (!cursor.equals(start)) {
            String token = predecessorToken.get(cursor);
            cursor = predecessor.get(cursor);
            nodePath.add(cursor);
            tokenPath.add(token);
        }

        java.util.Collections.reverse(nodePath);
        java.util.Collections.reverse(tokenPath);

        outNodes.addAll(nodePath);
        outTokens.addAll(tokenPath);
        return true;
    }

    /**
     * 反复折叠相邻的 (F|M)+(Son|Dau) 为同辈 token（eB/yB/eZ/yZ），直至一轮扫描无法再折叠。
     */
    private void reduce(List<Long> nodes, List<String> tokens, Map<Long, FamilyMemeber> membersById) {
        boolean collapsedAny = true;
        while (collapsedAny) {
            collapsedAny = false;
            for (int i = 0; i < tokens.size() - 1; i++) {
                String t1 = tokens.get(i);
                String t2 = tokens.get(i + 1);
                boolean isUpStep = TOKEN_FATHER.equals(t1) || TOKEN_MOTHER.equals(t1);
                boolean isDownStep = TOKEN_SON.equals(t2) || TOKEN_DAUGHTER.equals(t2);

                if (!isUpStep || !isDownStep) {
                    continue;
                }

                Long startNode = nodes.get(i);
                Long endNode = nodes.get(i + 2);
                if (startNode.equals(endNode)) {
                    continue;
                }

                String siblingToken = siblingToken(membersById.get(startNode), membersById.get(endNode));

                tokens.remove(i + 1);
                tokens.set(i, siblingToken);
                nodes.remove(i + 1);

                collapsedAny = true;
                break;
            }
        }
    }

    private String siblingToken(FamilyMemeber start, FamilyMemeber end) {
        boolean endIsElder;
        Integer startOrder = start == null ? null : start.getBirthOrder();
        Integer endOrder = end == null ? null : end.getBirthOrder();

        if (startOrder == null || endOrder == null) {
            // 排行未知时默认按年长处理
            endIsElder = true;
        } else {
            endIsElder = endOrder < startOrder;
        }

        boolean endIsMale = end != null && UserConstants.MALE.equals(end.getGender());

        if (endIsMale) {
            return endIsElder ? "eB" : "yB";
        } else {
            return endIsElder ? "eZ" : "yZ";
        }
    }
}
