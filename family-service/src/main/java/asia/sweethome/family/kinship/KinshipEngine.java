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
 * 【亲属称谓计算引擎】见 doc/api.md 「七、亲属称谓计算算法」。
 * <p>
 * 一句话原理：把家庭看成一张「关系网络图」——每个成员是一个点，父子/夫妻是点之间的连线。
 * 要算「我」怎么称呼「某人」，就在这张图上找从我到他的一条最短路径，把沿途每一步翻译成
 * 一个字母 token（F=父、M=母、S=配偶、Son=儿、Dau=女），拼起来就是一条「关系编码」，
 * 例如 F.F = 爸爸的爸爸 = 爷爷。步骤：
 * <ol>
 *   <li>buildGraph：把数据库里的边加载成邻接表（血亲边排在姻亲边前，保证优先走血亲）；</li>
 *   <li>bfs：广度优先搜索找最短路径，得到沿途的节点和 token；</li>
 *   <li>reduce：把「上一辈再下一辈」这种绕路折叠成「兄弟姐妹」（如 F.Son → 兄/弟）；</li>
 *   <li>localize：把最终的关系编码翻译成人类可读的称谓文字。</li>
 * </ol>
 */
@Component
public class KinshipEngine {

    // 关系路径上每一步用到的 token（方向 + 性别）
    private static final String TOKEN_FATHER = "F";    // 向上一步，且对方是男性 → 父
    private static final String TOKEN_MOTHER = "M";    // 向上一步，且对方是女性 → 母
    private static final String TOKEN_SPOUSE = "S";    // 横向一步 → 配偶
    private static final String TOKEN_SON = "Son";     // 向下一步，且对方是男性 → 儿子
    private static final String TOKEN_DAUGHTER = "Dau";// 向下一步，且对方是女性 → 女儿

    /**
     * 图里的一条「有向」边。
     * @param to    这条边指向的节点（成员 id）
     * @param token 走这条边对应的称谓字母
     * @param blood true=血亲边(父子)，false=姻亲边(夫妻)
     */
    private record Edge(Long to, String token, boolean blood) {
    }

    /**
     * 计算 viewer 对 target 的称谓。返回关系编码 + 可读称谓；无路径则返回 NONE。
     */
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

        // 特例：查自己对自己 → SELF
        if (viewerMemberId.equals(targetMemberId)) {
            return new RelationResult("SELF", KinshipLocalization.localize("SELF", acceptLanguage));
        }

        // 1. 建图
        Map<Long, List<Edge>> graph = buildGraph(relations, membersById);

        // 2. BFS 找最短路径，结果写进 nodes（沿途节点）和 tokens（沿途称谓字母）
        List<Long> nodes = new ArrayList<>();
        List<String> tokens = new ArrayList<>();
        boolean found = bfs(graph, viewerMemberId, targetMemberId, nodes, tokens);

        if (!found) {
            return RelationResult.NONE;   // 两人不连通，无称谓
        }

        // 3. 折叠化简（把绕路的同辈关系合并成兄弟姐妹）
        reduce(nodes, tokens, membersById);

        // 4. 用「.」把 tokens 连起来得到关系编码，再翻译成可读称谓
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

    /**
     * 广度优先搜索（BFS）：像水波纹一样从 start 一层层向外扩散，最先到达 target 的路径
     * 就是最短路径。用 predecessor 记录「每个点是从哪个点走过来的」，找到后回溯即可还原整条路径。
     */
    private boolean bfs(
            Map<Long, List<Edge>> graph,
            Long start,
            Long target,
            List<Long> outNodes,
            List<String> outTokens
    ) {
        Map<Long, Long> predecessor = new HashMap<>();       // 记录：某点 <- 它的上一个点
        Map<Long, String> predecessorToken = new HashMap<>();// 记录：走到某点用的 token
        Set<Long> visited = new HashSet<>();                 // 已访问，防止重复和绕圈
        Queue<Long> queue = new ArrayDeque<>();              // 待扩散的队列

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

    /**
     * 折叠成同辈时，决定用哪个 token：eB=哥、yB=弟、eZ=姐、yZ=妹。
     * 长幼由出生顺序 birthOrder 判断（数字越小越年长）；性别决定用「兄弟」还是「姐妹」。
     * @param start 折叠路径的起点成员（即「我」这一侧）
     * @param end   折叠路径的终点成员（即被称呼的兄弟姐妹）
     */
    private String siblingToken(FamilyMemeber start, FamilyMemeber end) {
        boolean endIsElder;
        Integer startOrder = start == null ? null : start.getBirthOrder();
        Integer endOrder = end == null ? null : end.getBirthOrder();

        if (startOrder == null || endOrder == null) {
            // 排行未知时默认按年长处理
            endIsElder = true;
        } else {
            endIsElder = endOrder < startOrder;   // 对方排行数字更小 → 对方更年长
        }

        boolean endIsMale = end != null && UserConstants.MALE.equals(end.getGender());

        if (endIsMale) {
            return endIsElder ? "eB" : "yB";   // 哥 / 弟
        } else {
            return endIsElder ? "eZ" : "yZ";   // 姐 / 妹
        }
    }
}
