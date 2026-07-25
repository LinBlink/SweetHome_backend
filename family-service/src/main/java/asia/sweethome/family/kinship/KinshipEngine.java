package asia.sweethome.family.kinship;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

import asia.sweethome.common.constants.RelationTypeConstants;
import asia.sweethome.common.constants.UserConstants;
import asia.sweethome.family.entity.po.FamilyMember;
import asia.sweethome.family.entity.po.FamilyRelation;

/**
 * 【亲属称谓计算引擎】见 doc/API.md 「十一、亲属称谓计算算法」。
 * <p>
 * 一句话原理：把家庭看成一张「关系网络图」——每个成员是一个点，父子/夫妻是点之间的连线。
 * 要算「我」怎么称呼「某人」，就在这张图上找从我到他的一条路径，把沿途每一步翻译成一个
 * token（{@link KinshipToken}），拼起来就是一条「关系编码」，例如 F.F = 爸爸的爸爸 = 爷爷。
 * <ol>
 *   <li>{@link #buildGraph}：把数据库里的边加载成邻接表；</li>
 *   <li>{@link #shortestPaths}：从 viewer 出发一次遍历，求出到<b>所有</b>成员的规范最短路径；</li>
 *   <li>{@link #reduce}：把「上一辈再下一辈」折成兄弟姐妹、「下一辈再上一辈」折成配偶；</li>
 *   <li>用「.」拼接成 relationCode。本地化完全交给前端，后端不翻译（见 API.md 11.6）。</li>
 * </ol>
 *
 * <h3>为什么不是普通 BFS</h3>
 * 旧实现用「谁先摸到门把手谁进」的 BFS（visited-on-enqueue），于是选出哪条路径取决于
 * 数据库返回边的顺序——同一份数据换个执行计划就可能算出不同的称谓，且线上无法复现。
 * 现在改成按一个<b>全序</b>挑路径（{@link #PATH_ORDER}）：先比跳数、再比姻亲跳数（血亲优先）、
 * 再比 token 字面量、最后比成员 id。这个顺序里没有任何一项依赖输入顺序，因此
 * <b>把 relations 列表任意打乱，结果都完全一致</b>（见 KinshipEngineTest 的乱序测试）。
 */
@Component
public class KinshipEngine {

    private static final Logger log = LoggerFactory.getLogger(KinshipEngine.class);

    /** viewer 就是 target 自己时的关系编码 */
    public static final String SELF_CODE = "SELF";

    /**
     * 图里的一条「有向」边：走到 to 这个成员，对应 token 这一步。
     */
    private record Edge(Long to, KinshipToken token) {
    }

    /**
     * 一条候选路径。nodes 比 tokens 多一个元素（nodes[0] 是起点 viewer）。
     * @param affinalHops 路径里姻亲（配偶）边的条数，选路时用来实现「血亲优先」
     */
    private record Candidate(List<Long> nodes, List<KinshipToken> tokens, int affinalHops) {
    }

    /**
     * 【规范路径全序】决定「多条路径都能到达时选哪条」，是本引擎可复现的根基。
     * 依次比较：① 跳数（越短越好）→ ② 姻亲跳数（血亲优先）→ ③ token 字面量序 → ④ 成员 id 序。
     * 后两项是纯粹的 tie-break，保证任何两条不同路径都能比出胜负，从而结果唯一。
     */
    private static final Comparator<Candidate> PATH_ORDER =
            Comparator.<Candidate>comparingInt(c -> c.tokens().size())
                    .thenComparingInt(Candidate::affinalHops)
                    .thenComparing(Candidate::tokens, KinshipEngine::compareTokens)
                    .thenComparing(Candidate::nodes, KinshipEngine::compareIds);

    /**
     * 计算 viewer 对 target 的称谓。无路径则返回 {@link RelationResult#NONE}。
     * <p>
     * 只需要一个 target 时用这个；要算「我对全家每个人」的称谓请用
     * {@link #computeRelations}——那个只跑一次遍历，不要在循环里调本方法。
     */
    public RelationResult computeRelation(
            List<FamilyRelation> relations,
            Map<Long, FamilyMember> membersById,
            Long viewerMemberId,
            Long targetMemberId
    ) {
        if (viewerMemberId == null || targetMemberId == null) {
            return RelationResult.NONE;
        }
        return computeRelations(relations, membersById, viewerMemberId)
                .getOrDefault(targetMemberId, RelationResult.NONE);
    }

    /**
     * 【批量】一次算出 viewer 对家里<b>所有</b>可达成员的称谓。
     * <p>
     * 图遍历天生就是「单源多汇」：从 viewer 出发跑一次，到每个人的路径就都有了。
     * 旧代码在成员列表的循环里逐个调 computeRelation，每次都重新建一遍图、重新遍历一次，
     * N 个成员就是 N 次重复劳动（O(N·E)）；改成批量后是 O(E)，20 人的家庭省掉 19 次建图。
     *
     * @return memberId → 称谓结果，只含可达成员（含 viewer 自己 → SELF）；不可达的成员不在 map 里
     */
    public Map<Long, RelationResult> computeRelations(
            List<FamilyRelation> relations,
            Map<Long, FamilyMember> membersById,
            Long viewerMemberId
    ) {
        if (viewerMemberId == null) {
            return Map.of();
        }

        Map<Long, List<Edge>> graph = buildGraph(relations, membersById);

        Map<Long, RelationResult> results = new HashMap<>();
        results.put(viewerMemberId, new RelationResult(SELF_CODE));

        for (Map.Entry<Long, Candidate> entry : shortestPaths(graph, viewerMemberId).entrySet()) {
            Candidate path = entry.getValue();
            List<KinshipToken> reduced = reduce(path.nodes(), path.tokens(), membersById);

            // 折叠有可能把整条路径消解干净（理论上只在 target 就是 viewer 时发生，已被上面提前处理）
            if (reduced.isEmpty()) {
                results.put(entry.getKey(), new RelationResult(SELF_CODE));
                continue;
            }

            StringBuilder code = new StringBuilder();
            for (KinshipToken token : reduced) {
                if (code.length() > 0) {
                    code.append('.');
                }
                code.append(token.getCode());
            }
            results.put(entry.getKey(), new RelationResult(code.toString()));
        }

        return results;
    }

    /**
     * 把关系表变成邻接表。
     * <p>
     * 两类边都必须校验「两端成员真的存在」：调用方给的 members 是<b>已过滤退出成员</b>的列表，
     * 而 relations 只按 deletedAt 过滤——一个人退出家庭但夫妻关系行没同步软删，就会变成一个
     * 「幽灵节点」留在图里，还能被当成中转站走过去，最后静默算出错误称谓。旧实现只在血亲分支
     * 做了这个校验，姻亲分支一行都没做。
     */
    private Map<Long, List<Edge>> buildGraph(List<FamilyRelation> relations, Map<Long, FamilyMember> membersById) {
        Map<Long, List<Edge>> graph = new HashMap<>();
        if (relations == null) {
            return graph;
        }

        for (FamilyRelation rel : relations) {
            Long subjectId = rel.getSubjectMemberId();
            Long objectId = rel.getObjectMemberId();
            if (subjectId == null || objectId == null || subjectId.equals(objectId)) {
                continue;   // 自环（A 是 A 的父母）是脏数据，直接丢掉
            }

            FamilyMember subject = membersById.get(subjectId);
            FamilyMember object = membersById.get(objectId);
            if (subject == null || object == null) {
                // 关系行还在、成员已退出/被移除 → 幽灵边，丢掉并留下痕迹便于排查数据不一致
                log.warn("关系边引用了不存在的成员，已忽略：type={} subject={} object={}",
                        rel.getRelationType(), subjectId, objectId);
                continue;
            }

            if (RelationTypeConstants.PARENT_OF.equals(rel.getRelationType())) {
                // subject 是 object 的父/母：向下一步看孩子性别，向上一步看家长性别
                addEdge(graph, subjectId, objectId, childToken(object));
                addEdge(graph, objectId, subjectId, parentToken(subject));
            } else if (RelationTypeConstants.SPOUSE_OF.equals(rel.getRelationType())) {
                // 配偶边的两个方向 token 不同：走向谁，就用谁的性别
                addEdge(graph, subjectId, objectId, spouseToken(object));
                addEdge(graph, objectId, subjectId, spouseToken(subject));
            }
        }

        return graph;
    }

    private void addEdge(Map<Long, List<Edge>> graph, Long from, Long to, KinshipToken token) {
        graph.computeIfAbsent(from, k -> new ArrayList<>()).add(new Edge(to, token));
    }

    /**
     * 求 viewer 到所有其它成员的规范最短路径。
     * <p>
     * 做法是「按 {@link #PATH_ORDER} 排序的优先队列」版 Dijkstra：每次取出全局最优的候选路径，
     * 第一次取到某个成员时就把它<b>定下来</b>（settled），之后再也不改。因为 PATH_ORDER 是全序，
     * 「全局最优」唯一确定，所以邻接表的插入顺序对结果毫无影响——这正是修掉「同样数据算出不同
     * 称谓」的关键。只从已定下来的点向<b>未</b>定下来的点扩展，路径天然不会绕回自己（简单路径）。
     *
     * @return memberId → 到它的规范最短路径（不含 viewer 自己）
     */
    private Map<Long, Candidate> shortestPaths(Map<Long, List<Edge>> graph, Long start) {
        Map<Long, Candidate> settled = new HashMap<>();
        Set<Long> done = new HashSet<>();
        done.add(start);

        PriorityQueue<Candidate> queue = new PriorityQueue<>(PATH_ORDER);
        seed(queue, graph, start, new Candidate(List.of(start), List.of(), 0));

        while (!queue.isEmpty()) {
            Candidate candidate = queue.poll();
            Long node = candidate.nodes().get(candidate.nodes().size() - 1);

            if (!done.add(node)) {
                continue;   // 已经有更优的路径定下来了
            }
            settled.put(node, candidate);

            seed(queue, graph, node, candidate);
        }

        return settled;
    }

    /** 把 from 的所有「还没定下来」的邻居作为新候选路径压进队列 */
    private void seed(PriorityQueue<Candidate> queue, Map<Long, List<Edge>> graph, Long from, Candidate base) {
        for (Edge edge : graph.getOrDefault(from, List.of())) {
            List<Long> nodes = new ArrayList<>(base.nodes());
            nodes.add(edge.to());
            List<KinshipToken> tokens = new ArrayList<>(base.tokens());
            tokens.add(edge.token());
            queue.add(new Candidate(nodes, tokens, base.affinalHops() + (edge.token().isAffinal() ? 1 : 0)));
        }
    }

    /**
     * 反复折叠相邻的两步，直到一轮扫描无法再折叠为止（允许级联折叠）：
     * <ul>
     *   <li>(F|M) + (Son|Dau) → 同辈 token：「爸爸的儿子」其实是「我兄弟」，不该表述成两跳；</li>
     *   <li>(Son|Dau) + (F|M) → S：「我儿子的妈妈」其实是「我配偶」。这条是旧实现漏掉的方向——
     *       夫妻关系没登记 SPOUSE_OF（未婚生育、离异未清理、用户懒得填）但两个孩子的
     *       PARENT_OF 都在时就会走到，界面上会出现「我儿子的妈妈」这种四不像。</li>
     * </ul>
     * 注：不需要再判断「折叠两端是不是同一个人」——{@link #shortestPaths} 产出的是简单路径，
     * nodes 里不可能有重复元素，旧代码里那个 startNode.equals(endNode) 的检查是永远为 false 的死代码。
     *
     * @return 折叠后的 token 序列（不修改传入的 list）
     */
    private List<KinshipToken> reduce(List<Long> nodes, List<KinshipToken> tokens, Map<Long, FamilyMember> membersById) {
        List<Long> ns = new ArrayList<>(nodes);
        List<KinshipToken> ts = new ArrayList<>(tokens);

        boolean collapsedAny = true;
        while (collapsedAny) {
            collapsedAny = false;
            for (int i = 0; i + 1 < ts.size(); i++) {
                KinshipToken first = ts.get(i);
                KinshipToken second = ts.get(i + 1);

                KinshipToken folded;
                if (first.isUpStep() && second.isDownStep()) {
                    folded = siblingToken(membersById.get(ns.get(i)), membersById.get(ns.get(i + 2)));
                } else if (first.isDownStep() && second.isUpStep()) {
                    // 折出来的是配偶，用哪个 token 取决于「共同亲代」那一方的性别
                    folded = spouseToken(membersById.get(ns.get(i + 2)));
                } else {
                    continue;
                }

                ts.set(i, folded);
                ts.remove(i + 1);
                ns.remove(i + 1);   // 中间那个人被折掉了
                collapsedAny = true;
                break;
            }
        }

        return ts;
    }

    /**
     * 折叠成同辈时，决定用哪个 token：eB=哥、yB=弟、eZ=姐、yZ=妹。
     * 长幼由出生顺序 birthOrder 判断（数字越小越年长）；性别决定用「兄弟」还是「姐妹」。
     *
     * @param start 折叠路径的起点成员（这一侧的「我」，多跳路径里未必是 viewer 本人）
     * @param end   折叠路径的终点成员（被称呼的那个兄弟姐妹）
     */
    private KinshipToken siblingToken(FamilyMember start, FamilyMember end) {
        Integer startOrder = start == null ? null : start.getBirthOrder();
        Integer endOrder = end == null ? null : end.getBirthOrder();

        boolean endIsElder;
        if (startOrder == null || endOrder == null) {
            // 排行未知时默认按年长处理（见 API.md 11.4 的已知精度局限）。
            // 打日志是为了能量化这个兜底分支在真实数据里的命中率——目前 birthOrder 没有任何
            // 录入入口，怀疑它几乎总是 null，也就是说线上几乎所有兄弟姐妹都被叫成了「哥/姐」。
            log.debug("birthOrder 缺失，长幼判定回退为「年长」：start={} end={}",
                    start == null ? null : start.getId(), end == null ? null : end.getId());
            endIsElder = true;
        } else {
            endIsElder = endOrder < startOrder;
        }

        return switch (genderOf(end)) {
            case MALE -> endIsElder ? KinshipToken.ELDER_BROTHER : KinshipToken.YOUNGER_BROTHER;
            case FEMALE -> endIsElder ? KinshipToken.ELDER_SISTER : KinshipToken.YOUNGER_SISTER;
            case UNKNOWN -> endIsElder ? KinshipToken.ELDER_SIBLING : KinshipToken.YOUNGER_SIBLING;
        };
    }

    /** 向上一步用哪个 token */
    private KinshipToken parentToken(FamilyMember parent) {
        return switch (genderOf(parent)) {
            case MALE -> KinshipToken.FATHER;
            case FEMALE -> KinshipToken.MOTHER;
            case UNKNOWN -> KinshipToken.PARENT;
        };
    }

    /** 向下一步用哪个 token */
    private KinshipToken childToken(FamilyMember child) {
        return switch (genderOf(child)) {
            case MALE -> KinshipToken.SON;
            case FEMALE -> KinshipToken.DAUGHTER;
            case UNKNOWN -> KinshipToken.CHILD;
        };
    }

    /** 横向一步用哪个 token */
    private KinshipToken spouseToken(FamilyMember spouse) {
        return switch (genderOf(spouse)) {
            case MALE -> KinshipToken.HUSBAND;
            case FEMALE -> KinshipToken.WIFE;
            case UNKNOWN -> KinshipToken.SPOUSE;
        };
    }

    /** 成员性别的三种可能。注意「未知」是独立的一种，不能和「女性」混为一谈 */
    private enum Gender {
        MALE, FEMALE, UNKNOWN
    }

    /**
     * 性别判定。旧实现用 {@code MALE.equals(gender)} 判断，false 就一律当女性，
     * 于是 gender 为 null（老数据、第三方登录没拿到）时会静默产出「女性」称谓。
     * 现在「不知道」有自己的分支，最终会走成中性 token（P/C/S/eX/yX）。
     */
    private Gender genderOf(FamilyMember member) {
        String gender = member == null ? null : member.getGender();
        if (UserConstants.MALE.equals(gender)) {
            return Gender.MALE;
        }
        if (UserConstants.FEMALE.equals(gender)) {
            return Gender.FEMALE;
        }
        log.warn("成员性别缺失或取值非法，将产出中性称谓：memberId={} gender={}",
                member == null ? null : member.getId(), gender);
        return Gender.UNKNOWN;
    }

    private static int compareTokens(List<KinshipToken> a, List<KinshipToken> b) {
        int shared = Math.min(a.size(), b.size());
        for (int i = 0; i < shared; i++) {
            int cmp = a.get(i).getCode().compareTo(b.get(i).getCode());
            if (cmp != 0) {
                return cmp;
            }
        }
        return Integer.compare(a.size(), b.size());
    }

    private static int compareIds(List<Long> a, List<Long> b) {
        int shared = Math.min(a.size(), b.size());
        for (int i = 0; i < shared; i++) {
            int cmp = Long.compare(a.get(i), b.get(i));
            if (cmp != 0) {
                return cmp;
            }
        }
        return Integer.compare(a.size(), b.size());
    }
}
