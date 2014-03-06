package controllers.sampleMCTS;

import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;

import java.util.Random;

public class SingleTreeNode
{
    private static final double HUGE_NEGATIVE = -10000000.0;
    private static final double HUGE_POSITIVE =  10000000.0;
    public static double epsilon = 1e-6;
    public static double egreedyEpsilon = 0.05;
    public StateObservation state;
    public SingleTreeNode parent;
    public SingleTreeNode[] children;
    public double totValue;
    public int nVisits;
    public static Random m_rnd;
    private int m_depth;
    private static double[] lastBounds = new double[]{0,1};
    private static double[] curBounds = new double[]{0,1};

    public SingleTreeNode(Random rnd) {
        this(null, null, rnd);
    }

    public SingleTreeNode(StateObservation state, SingleTreeNode parent, Random rnd) {
        this.state = state;
        this.parent = parent;
        this.m_rnd = rnd;
        children = new SingleTreeNode[Agent.NUM_ACTIONS];
        totValue = 0.0;
        if(parent != null)
            m_depth = parent.m_depth+1;
        else
            m_depth = 0;
    }


    public void mctsSearch(ElapsedCpuTimer elapsedTimer) {

        lastBounds[0] = curBounds[0];
        lastBounds[1] = curBounds[1];

        long remaining = elapsedTimer.remainingTimeMillis();
        //int numIters = 0;
        while(remaining > 10)   {
            SingleTreeNode selected = treePolicy();
            double delta = selected.rollOut();
            backUp(selected, delta);
            remaining = elapsedTimer.remainingTimeMillis();
            //numIters++;
        }
        //System.out.println("-- " + numIters + " --");
    }

    public SingleTreeNode treePolicy() {

        SingleTreeNode cur = this;

        while (!cur.state.isGameOver() && cur.m_depth < Agent.ROLLOUT_DEPTH)
        {
            if (cur.notFullyExpanded()) {
                return cur.expand();

            } else {
                SingleTreeNode next = cur.uct();
                //SingleTreeNode next = cur.egreedy();
                cur = next;
            }
        }

        return cur;
    }


    public SingleTreeNode expand() {

        int bestAction = 0;
        double bestValue = -1;

        for (int i = 0; i < children.length; i++) {
            double x = m_rnd.nextDouble();
            if (x > bestValue && children[i] == null) {
                bestAction = i;
                bestValue = x;
            }
        }

        StateObservation nextState = state.copy();
        nextState.advance(Agent.actions[bestAction]);

        SingleTreeNode tn = new SingleTreeNode(nextState, this, this.m_rnd);
        children[bestAction] = tn;
        return tn;

    }

    public SingleTreeNode uct() {

        SingleTreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;
        for (SingleTreeNode child : this.children)
        {
            double hvVal = child.totValue;
            double childValue =  hvVal / (child.nVisits + this.epsilon);

            double uctValue = childValue +
                    Agent.K * Math.sqrt(Math.log(this.nVisits + 1) / (child.nVisits + this.epsilon)) +
                    this.m_rnd.nextDouble() * this.epsilon;

            // small sampleRandom numbers: break ties in unexpanded nodes
            if (uctValue > bestValue) {
                selected = child;
                bestValue = uctValue;
            }
        }

        if (selected == null)
        {
            throw new RuntimeException("Warning! returning null: " + bestValue + " : " + this.children.length);
        }

        return selected;
    }

    public SingleTreeNode egreedy() {


        SingleTreeNode selected = null;

        if(m_rnd.nextDouble() < egreedyEpsilon)
        {
            //Choose randomly
            int selectedIdx = m_rnd.nextInt(children.length);
            selected = this.children[selectedIdx];

        }else{
            //pick the best Q.
            double bestValue = -Double.MAX_VALUE;
            for (SingleTreeNode child : this.children)
            {
                double hvVal = child.totValue;

                // small sampleRandom numbers: break ties in unexpanded nodes
                if (hvVal > bestValue) {
                    selected = child;
                    bestValue = hvVal;
                }
            }

        }


        if (selected == null)
        {
            throw new RuntimeException("Warning! returning null: " + this.children.length);
        }

        return selected;
    }


    public double rollOut()
    {
        StateObservation rollerState = state.copy();
        int thisDepth = this.m_depth;

        while (!finishRollout(rollerState,thisDepth)) {

            int action = m_rnd.nextInt(Agent.NUM_ACTIONS);
            rollerState.advance(Agent.actions[action]);
            thisDepth++;
        }

        double delta = value(rollerState);

        if(delta < curBounds[0]) curBounds[0] = delta;
        if(delta > curBounds[1]) curBounds[1] = delta;

        double normDelta = Utils.normalise(delta ,lastBounds[0], lastBounds[1]);

        return normDelta;
    }

    public double value(StateObservation a_gameState) {

        boolean gameOver = a_gameState.isGameOver();
        Types.WINNER win = a_gameState.getGameWinner();
        double rawScore = a_gameState.getGameScore();

        if(gameOver && win == Types.WINNER.PLAYER_LOSES)
            return HUGE_NEGATIVE;

        if(gameOver && win == Types.WINNER.PLAYER_WINS)
            return HUGE_POSITIVE;

        return rawScore;
    }

    public boolean finishRollout(StateObservation rollerState, int depth)
    {
        if(depth >= Agent.ROLLOUT_DEPTH)      //rollout end condition.
            return true;

        if(rollerState.isGameOver())               //end of game
            return true;

        return false;
    }

    public void backUp(SingleTreeNode node, double result)
    {
        SingleTreeNode n = node;
        while(n != null)
        {
            n.nVisits++;
            n.totValue += result;
            n = n.parent;
        }
    }


    public int mostVisitedAction() {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;
        boolean allEqual = true;
        double first = -1;

        for (int i=0; i<children.length; i++) {

            if(children[i] != null)
            {
                if(first == -1)
                    first = children[i].nVisits;
                else if(first != children[i].nVisits)
                {
                    allEqual = false;
                }

                if (children[i].nVisits + m_rnd.nextDouble() * epsilon > bestValue) {
                    bestValue = children[i].nVisits;
                    selected = i;
                }
            }
        }

        if (selected == -1)
        {
            System.out.println("Unexpected selection!");
            selected = 0;
        }else if(allEqual)
        {
            //If all are equal, we opt to choose for the one with the best Q.
            selected = bestAction();
        }
        return selected;
    }

    public int bestAction()
    {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;

        for (int i=0; i<children.length; i++) {

            if(children[i] != null && children[i].totValue + m_rnd.nextDouble() * epsilon > bestValue) {
                bestValue = children[i].totValue;
                selected = i;
            }
        }

        if (selected == -1)
        {
            System.out.println("Unexpected selection!");
            selected = 0;
        }

        return selected;
    }


    public boolean notFullyExpanded() {
        for (SingleTreeNode tn : children) {
            if (tn == null) {
                return true;
            }
        }

        return false;
    }
}
