/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.alxio;

import core.game.Observation;
import core.game.StateObservation;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

/**
 *
 * @author ALX
 */
public class BFSNode implements INode, Comparable<BFSNode> {

    public static final double epsilon = 1e-6;
    public static MersenneTwisterFast random = new MersenneTwisterFast();

    public static HashMap<Long, BFSNode> map;
    public static PriorityQueue<BFSNode> queue;
    public static BFSNode leader;
    public static CollisionAnalyzer analyser;

    static long mCopies = 0;
    static long mAdvances = 0;

    private static double[][] visitedPunishment;
    private static double visitedPunishmentBase = 1;
    private static int x, y;

    public static BFSNode init(StateObservation a_gameState) {
        map = new HashMap<>();
        queue = new PriorityQueue<>();

        ArrayList<Observation>[][] grid = a_gameState.getObservationGrid();
        y = grid[0].length;
        x = grid.length;

        visitedPunishment = new double[y + 1][x + 1];

        analyser = new CollisionAnalyzer(a_gameState);

        treeSize = 0;
        return addToMap(null, a_gameState);
    }

    long zHash;
    ChildNodes[] children;
    StateObservation stateObs;
    BFSNode parent = null;

    double value = 0;
    double totalValue = 0;
    double expectedValue = 0;
    //double chance = 1.0;
    int nVisits = 0;

    int depth = 0;
    int epoch = 0;

    static int currentEpoch = 0;

    public static int treeSize = 0;

    static BFSNode root;

    @Override
    public String toString() {
        String s = super.toString();
        return s + " " + zHash + " " + stateObs.getAvatarPosition();
    }

    private void updateExpected() {
        if (children == null) {
            expectedValue = value;
        } else {
            double val = -Double.MAX_VALUE;
            double sum = 0;
            for (ChildNodes ch : children) {
                double exp = ch.expectedValue();
                sum += exp;
                if (exp > val) {
                    val = exp;
                }
            }
            expectedValue = C.DISCOUNT_RATE * val + (1 - C.DISCOUNT_RATE) * value;
        }
    }

    BFSNode(StateObservation state) {
        zHash = Z.hash(state);
        stateObs = state;
    }

    BFSNode(StateObservation state, long hash, BFSNode prnt) {
        zHash = hash;
        stateObs = state;
        parent = prnt;
        depth = prnt == null ? 0 : prnt.depth + 1;
    }

    public static int TICKS = 0;

    public int search(ElapsedCpuTimer elapsedTimer) {
        Debug.println("Search: " + this);

        ++TICKS;
        //double avgTimeTaken = 0;
        //double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int remainingLimit = 10;
        //int numIters = 0;

        int grows = 0;
        int mcts = 0;
        int greed = 0;

        UCT_CALLS = 0;
        UCT_INNER_CALLS = 0;
        int totalGrows = 0;
        int totalMSTS = 0;

        while (!queue.isEmpty() && remaining > remainingLimit && treeSize < Strategus.MAX_TREE_SIZE) {
            if(Strategus.FAIL) return -1;
            if (random.nextDouble() < Strategus.ROLLOUT_CHANCE) {
                mcts(false);
            } else if (greed++ < C.GREEDY_COUNT) {
                ++totalGrows;
                greedyGrow();
            } else if (grows++ < C.GROW_COUNT) {
                ++totalGrows;
                treeGrowth();
            } else if (mcts++ < C.MCTS_COUNT) {
                ++totalMSTS;
                mcts(true);
            } else {
                grows = mcts = 0;
            }
            remaining = elapsedTimer.remainingTimeMillis();
        }

        int selected = -1;
        BFSNode best = Strategus.DETERMINISTIC_CHOICE ? leader : null;

        if (best == null) {
            Debug.log(1, "No best?!");
        } else if (best == this) {
            Debug.log(3, "Current state is best?! " + best.value);
        } else {
            Debug.log(3, "Best is " + best.value + " exp: " + best.expectedValue);
            //Debug.log(3, Game.ActionName(i) + " expValue: " + children[i].expectedValue());
            for (int k = 0; k < 100; ++k) {
                if (best.parent == null) {
                    Debug.println("Null parent");
                    break;
                } else if (best.parent == this) {
                    Debug.println("Path ok, parent: " + this);
                    Debug.log(3, "Have path, len: " + k);
                    break;
                }
                Debug.println("Moving to " + best.parent);
                best = best.parent;
            }
            double maxPercent = 0;
            for (int i = 0; i < children.length; ++i) {
                double percent = children[i].contains(best);
                if (percent > maxPercent) {
                    selected = i;
                    maxPercent = percent;
                }
            }
        }

        if (selected == -1) {
            double childValue = -Double.MAX_VALUE;
            for (int i = 0; i < children.length; ++i) {
                double exp = children[i].expectedValue();
                if (exp > childValue) {
                    childValue = exp;
                    selected = i;
                }
                //Debug.log(3, Game.ActionName(i) + " expValue: " + children[i].expectedValue());
            }
            if (selected != -1) {
                Debug.log(3, "Selected expValue: " + children[selected].expectedValue());
            }
        }

        for (int i = 0; i < children.length; ++i) {
            children[i].mostProbableNode().printScores(Game.ActionName(i));
            //Debug.log(10, children[i].expectedValue() + "");
        }

        if (selected == -1) {
            Debug.log(3, "Using uct(0) to get best");
            selected = uct(0);
        }

        //Debug.log(1, "Copies: " + mCopies + " Advances: " + mAdvances);
        //Debug.log(1, "Collisions " + (COLLISION) + "/" + OK + " NODES:" + NEW_NODE);
        //selected = uct();
        return selected;
    }

    public BFSNode proceed(int lastAction, StateObservation obs) {
        long hash = Z.hash(obs);
        punishPosition(obs);
        if (hash == zHash) {
            return this;
        }
        Debug.log(1, "Observed state:" + hash);
        Debug.printGrid(obs.getObservationGrid());
        if (lastAction != -1) {
            BFSNode node = (BFSNode) children[lastAction].findByHashCode(hash);
            if (node != null) {
                return node;
            }
        }
        return addToMap(parent, obs);
    }

    void punishPosition(StateObservation obs) {
        Vector2d pos = obs.getAvatarPosition();
        int size = obs.getBlockSize();
        int x = (int) (pos.x / size);
        int y = (int) (pos.y / size);
        if(x < 0 || y < 0 || x >= this.x || y >= this.y) return;
        visitedPunishment[y][x] += visitedPunishmentBase;
        visitedPunishmentBase *= 2;

        if (visitedPunishmentBase > 1000) {
            for (int i = 0; i < BFSNode.y; ++i) {
                for (int j = 0; j < BFSNode.x; ++j) {
                    visitedPunishment[i][j] /= visitedPunishmentBase;
                }
            }
            visitedPunishmentBase = 1.0;
        }
    }

    double getPunishment(StateObservation obs) {
        Vector2d pos = obs.getAvatarPosition();
        int size = obs.getBlockSize();
        int x = (int) (pos.x / size);
        int y = (int) (pos.y / size);
        if(x < 0 || y < 0 || x >= this.x || y >= this.y) return 1;
        return visitedPunishment[y][x] / visitedPunishmentBase;
    }

    public void clean() {
        Debug.log(1, "Root state:" + Z.hash(stateObs));
        Debug.printGrid(stateObs.getObservationGrid());
        leader = null;
        parent = null;
        queue.clear();
        ++currentEpoch;
        depth = 0;
        //chance = 1;
        epoch = currentEpoch;
        treeSize = 0;
        int maxDepth = 0;

        ArrayDeque<BFSNode> Q = new ArrayDeque<>();
        ArrayDeque<BFSNode> Q1 = new ArrayDeque<>();

        Q.push(this);
        while (!Q.isEmpty()) {
            ++treeSize;
            BFSNode node = Q.remove();
            node.updateLeader();
            //if(node.chance > 0.5) node.updateLeader();
            Debug.println("Clean: " + node);
            if (node.children == null) {
                queue.add(node);
            } else {
                for (ChildNodes list : node.children) {
                    for (Pair<Double, BFSNode> child : list) {
                        if (child.y.epoch < currentEpoch) {
                            child.y.eval();
                            child.y.epoch = currentEpoch;
                            child.y.depth = node.depth + 1;
                            if (child.y.depth > maxDepth) {
                                maxDepth = child.y.depth;
                            }
                            Q.add(child.y);
                            Q1.addFirst(child.y);
                        }
                    }
                }
            }
        }
        for (BFSNode node : Q1) {
            node.updateExpected();
        }
        Q1.clear();
        ArrayList<Long> toDelete = new ArrayList<>();
        for (Map.Entry<Long, BFSNode> entry : map.entrySet()) {
            if (entry.getValue().epoch != currentEpoch) {
                toDelete.add(entry.getKey());
            }
        }
        for (Long id : toDelete) {
            map.remove(id);
        }
        toDelete.clear();

        Strategus.updateStrategy(treeSize);

        Debug.log(3, "SIZE: " + treeSize + " DEPTH: " + maxDepth);
        //
        //Debug.log(1, "SIZE: " + treeSize + " DEPTH: " + maxDepth + " DELETE: " + DELETED + " TOTAL: " + Runtime.getRuntime().totalMemory() / 1048576);
        //System.gc();
        //Debug.log(1, "SIZE: " + treeSize + " DEPTH: " + maxDepth + " DELETE: " + DELETED + " TOTAL: " + Runtime.getRuntime().totalMemory() / 1048576);
    }

    static long COLLISION = 0;
    static long OK = 0;
    static long NEW_NODE = 0;
    static int UCT_CALLS = 0;
    static int UCT_INNER_CALLS = 0;

    static BFSNode addToMap(BFSNode prev, StateObservation nextState) {
        mCopies++;
        mAdvances++;

        long hash = Z.hash(nextState);
        BFSNode next = map.get(hash);

        if (next == null) {
            next = new BFSNode(nextState, hash, prev);
            queue.offer(next);
            Debug.println("Queue: " + next);
            map.put(hash, next);
            next.eval();
            next.updateLeader();
            ++treeSize;
            ++NEW_NODE;
        } else {
            ++OK;
        }
        return next;
    }

    private void treeGrowth() {
        BFSNode curr = queue.remove();
        while (!grow(curr) && !queue.isEmpty()) {
            curr = queue.remove();
        }
    }

    private boolean grow(BFSNode curr) {
        if (curr.children != null) {
            return false;
        }
        curr.children = new ChildNodes[Game.NUM_ACTIONS];
        int[] indices = genArray(curr.children.length);
        for (int i : indices) {
            StateObservation nextState = curr.stateObs.copy();
            nextState.advance(Game.actions[i]);
            if (nextState.getAvatarPosition().equals(curr.stateObs.getAvatarPosition())
                    && !nextState.getAvatarOrientation().equals(curr.stateObs.getAvatarOrientation())) {
                nextState.advance(Game.actions[i]);
            }
            analyser.analyze(nextState, curr.stateObs.getGameScore());
            BFSNode next = addToMap(curr, nextState);
            curr.children[i] = new ChildNodes(next);
        }
        return true;
    }

    private void eval() {
        expectedValue = value = evalState(stateObs) - depth * C.DEPTH_PENETLY - getPunishment(stateObs) * C.VISITED_PENETLY;
    }

    public static double evalState(StateObservation state) {
        int res = 0;
        for (Integer resource : state.getAvatarResources().values()) {
            res += resource;
        }
        boolean gameOver = state.isGameOver();
        Types.WINNER win = state.getGameWinner();
        double rawScore = state.getGameScore();

        if (gameOver && win == Types.WINNER.PLAYER_LOSES) {
            rawScore -= C.WIN_BONUS;
        }
        if (gameOver && win == Types.WINNER.PLAYER_WINS) {
            rawScore += C.WIN_BONUS;
        }
        return rawScore + C.RESOURCE_BONUS * res + analyser.eval(state) * C.HEURISTIC_BONUS;
    }

    void printScores(String prefix) {
        if (Debug.level > 4) {
            return;
        }
        StringBuilder sb = new StringBuilder(prefix);
        sb.append(" Score:");
        sb.append(value);
        sb.append(" Exp:");
        sb.append(expectedValue);
        sb.append(" Heu:");
        sb.append(analyser.eval(stateObs));
        //System.out.print(" Score: %3f, Exp: %3f, Raw: %3f Heu: %3f", value, expectedValue, stateObs.getGameScore(), analyser.eval(stateObs));
        System.out.println(sb);
    }

    /**
     * Generates random permutation of array [0, ..., size-1] Used to randomize
     * child visits
     *
     * @param size
     * @return
     */
    static int[] genArray(int size) {
        int[] ar = new int[size];
        for (int i = 0; i < size; ++i) {
            ar[i] = i;
        }
        for (int i = size - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
        return ar;
    }

    @Override
    public long getHash() {
        return zHash;
    }

    private void updateLeader() {
        if (leader == null || value > leader.value) {
            Debug.log(1, "Leader " + this);
            Debug.printGrid(stateObs.getObservationGrid());
            leader = this;
        }
    }

    @Override
    public int compareTo(BFSNode o) {
        if (value > o.value) {
            return -1;
        }
        if (value < o.value) {
            return 1;
        }
        if (depth < o.depth) {
            return -1;
        }
        if (depth > o.depth) {
            return 1;
        }
        return 0;
    }

    private void greedyGrow() {
        ArrayDeque<BFSNode> toUpdate = new ArrayDeque<>();
        BFSNode selected = this;
        StateObservation tmpState = null;
        while (selected.children != null) {
            toUpdate.addFirst(selected);

            int childId = -1;
            double childValue = -Double.MAX_VALUE;

            for (int i = 0; i < children.length; ++i) {
                double exp = children[i].expectedValue();
                if (exp > childValue) {
                    childValue = exp;
                    childId = i;
                }
            }

            if (childId == -1) {
                break;
            }

            ChildNodes child = selected.children[childId];
            BFSNode next;
            if (child.isSure()) {
                tmpState = null;
                next = child.get();
            } else {
                if (tmpState == null) {
                    tmpState = selected.stateObs.copy();
                }
                double val = tmpState.getGameScore();
                tmpState.advance(Game.actions[childId]);
                analyser.analyze(tmpState, val);
                next = child.addNode(tmpState, selected);
            }
            if (next.depth <= selected.depth) {
                break;
            }
            selected = next;
        }
        grow(selected);
        backPropagate(toUpdate, selected, true);
    }

    private void mcts(boolean grow) {
        ArrayDeque<BFSNode> toUpdate = new ArrayDeque<>();
        BFSNode selected = this;
        StateObservation tmpState = null;
        while (selected.children != null) {
            toUpdate.addFirst(selected);
            int childId = selected.uct(C.K);
            if (childId == -1) {
                break;
            }
            ChildNodes child = selected.children[childId];
            if (child.isSure()) {
                tmpState = null;
                selected = child.get();
            } else {
                if (tmpState == null) {
                    tmpState = selected.stateObs.copy();
                }
                double val = tmpState.getGameScore();
                tmpState.advance(Game.actions[childId]);
                analyser.analyze(tmpState, val);
                selected = child.addNode(tmpState, selected);
            }
        }
        if (grow) {
            backPropagate(toUpdate, selected, grow(selected));
        } else {
            rollout(tmpState, toUpdate, selected);
        }
    }

    //toUpdate have leaf in begin and root in end
    private void backPropagate(ArrayDeque<BFSNode> toUpdate, BFSNode selected, boolean withValue) {
        double updateValue = 0;
        int count = children.length;
        if (withValue) {
            double values[] = new double[children.length];
            for (int i = 0; i < children.length; ++i) {
                values[i] = selected.children[i].nodes.get(0).value;
            }
            Arrays.sort(values);
            int start = 0;
            for (int i = 0; i < children.length / 2; ++i) {
                if (values[i] < selected.value) {
                    ++start;
                }
            }
            for (int i = start; i < children.length; ++i) {
                updateValue += values[i];
            }
            count = children.length - start;
        }
        for (BFSNode updated : toUpdate) {
            updated.totalValue += updateValue;
            updated.nVisits += count;
            updated.updateExpected();
            updateValue *= C.DISCOUNT_RATE;
        }
    }

//    Pair<Integer, Double> sumUp(ChildNodes child) {
//        Pair<Integer, Double> answer = new Pair<>(0, 0.0);
//        for (Pair<Double, BFSNode> node : child) {
//            answer.y += node.x * (node.y.totalValue + DISCOUNT_RATE * node.y.value) / (node.y.nVisits + 1);
//            answer.x += node.y.nVisits;
//        }
//        return answer;
//    }
    private int uct(double K) {
        UCT_CALLS++;
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;
        for (int i = 0; i < children.length; ++i) {
            UCT_INNER_CALLS++;
            ChildNodes child = children[i];
            //Disallow goint down the tree.
            if (child.mostProbableNode().depth <= depth) {
                continue;
            }

            double childValue = 0;
            int childVisits = 0;
            for (Pair<Double, BFSNode> node : child) {
                childValue += node.x * (node.y.totalValue + C.DISCOUNT_RATE * node.y.value) / (node.y.nVisits + 1);
                childVisits += node.y.nVisits;
            }
            double uctValue = childValue + K * Math.sqrt(Math.log(this.nVisits + 1) / (childVisits + this.epsilon));
            if (uctValue > bestValue) {
                selected = i;
                bestValue = uctValue;
            }
        }
        return selected;
    }

    public static int ROLLOUT_LENGTH = 5;

    private void rollout(StateObservation obs, ArrayDeque<BFSNode> toUpdate, BFSNode selected) {
        double updateValue = 0;
        if (obs == null) {
            obs = selected.stateObs.copy();
        }
        for (int i = 0; i < ROLLOUT_LENGTH && !obs.isGameOver(); ++i) {
            double val = obs.getGameScore();
            obs.advance(Game.actions[random.nextInt(Game.actions.length)]);
            analyser.analyze(obs, val);
        }
        updateValue = evalState(obs);

        if (updateValue < 0 && selected.value > 0) {
            updateValue = 0;
        }

        for (BFSNode updated : toUpdate) {
            updated.totalValue += updateValue;
            updated.nVisits += 1;
            updateValue *= C.DISCOUNT_RATE;
        }
    }
}
