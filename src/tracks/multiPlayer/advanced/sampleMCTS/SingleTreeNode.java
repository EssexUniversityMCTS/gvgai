package tracks.multiPlayer.advanced.sampleMCTS;

import java.util.Random;

import core.game.StateObservationMulti;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;
import tools.Vector2d;
import tracks.multiPlayer.tools.heuristics.SimpleStateHeuristic;
import tracks.multiPlayer.tools.heuristics.StateHeuristicMulti;
import tracks.multiPlayer.tools.heuristics.WinScoreHeuristic;
import utilsUI.DrawingAgent;
import utilsUI.ParameterSetTS;

import static utilsUI.Constants.HEURISTIC_SIMPLESTATE;
import static utilsUI.Constants.HEURISTIC_WINSCORE;

public class SingleTreeNode
{
    private final double HUGE_NEGATIVE = -10000000.0;
    private final double HUGE_POSITIVE =  10000000.0;
    public double epsilon = 1e-6;
    public double egreedyEpsilon = 0.05;
    public SingleTreeNode parent;
    public SingleTreeNode[] children;
    public double totValue;
    public int nVisits;
    public Random m_rnd;
    public int m_depth;
    protected double[] bounds = new double[]{Double.MAX_VALUE, -Double.MAX_VALUE};
    public int childIdx;

    public int[] NUM_ACTIONS;
    public Types.ACTIONS[][] actions;
    public int id, oppID, no_players;

    public StateObservationMulti rootState;
    private StateHeuristicMulti heuristic;
    private DrawingAgent drawingAgent;

    ParameterSetTS params;

    public SingleTreeNode(Random rnd, int[] NUM_ACTIONS, Types.ACTIONS[][] actions, int id, int oppID, int no_players,
                          ParameterSetTS params, StateObservationMulti stateObs, DrawingAgent drawingagent) {
        this(null, -1, rnd, id, oppID, no_players, NUM_ACTIONS, actions, params, stateObs, drawingagent);
    }

    public SingleTreeNode(SingleTreeNode parent, int childIdx, Random rnd, int id, int oppID, int no_players,
                          int[] NUM_ACTIONS, Types.ACTIONS[][] actions, ParameterSetTS params,
                          StateObservationMulti stateObs, DrawingAgent drawingagent) {
        this.id = id;
        this.oppID = oppID;
        this.no_players = no_players;
        this.parent = parent;
        this.m_rnd = rnd;
        totValue = 0.0;
        this.childIdx = childIdx;
        if(parent != null)
            m_depth = parent.m_depth+1;
        else
            m_depth = 0;
        this.NUM_ACTIONS = NUM_ACTIONS;
        children = new SingleTreeNode[NUM_ACTIONS[id]];
        this.actions = actions;
        this.params = params;

        switch(params.HEURISTIC_TYPE){
            case HEURISTIC_SIMPLESTATE: heuristic = new SimpleStateHeuristic(stateObs); break;
            case HEURISTIC_WINSCORE:
            default: heuristic = new WinScoreHeuristic(stateObs);
        }
    }


    public void mctsSearch(ElapsedCpuTimer elapsedTimer) {

        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int numIters = 0;

        int remainingLimit = 5;
        while(remaining > 2*avgTimeTaken && remaining > remainingLimit){
        //while(numIters < Agent.MCTS_ITERATIONS){

            StateObservationMulti state = rootState.copy();

            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
            SingleTreeNode selected = treePolicy(state);
            double delta = selected.rollOut(state);
            backUp(selected, delta);

            numIters++;
            acumTimeTaken += (elapsedTimerIteration.elapsedMillis()) ;
            //System.out.println(elapsedTimerIteration.elapsedMillis() + " --> " + acumTimeTaken + " (" + remaining + ")");
            avgTimeTaken  = acumTimeTaken/numIters;
            remaining = elapsedTimer.remainingTimeMillis();
        }

//        System.out.println("-- " + numIters + " -- ( " + avgTimeTaken + ")");
    }

    public SingleTreeNode treePolicy(StateObservationMulti state) {

        SingleTreeNode cur = this;

        while (!state.isGameOver() && cur.m_depth < params.SIMULATION_DEPTH)
        {
            if (cur.notFullyExpanded()) {
                return cur.expand(state);

            } else {
                SingleTreeNode next = cur.uct(state);
                cur = next;
            }
        }

        return cur;
    }


    public SingleTreeNode expand(StateObservationMulti state) {

        int bestAction = 0;
        double bestValue = -1;

        for (int i = 0; i < children.length; i++) {
            double x = m_rnd.nextDouble();
            if (x > bestValue && children[i] == null) {
                bestAction = i;
                bestValue = x;
            }
        }

        //Roll the state

        //need to provide actions for all players to advance the forward model
        Types.ACTIONS[] acts = new Types.ACTIONS[no_players];

        //set this agent's action
        acts[id] = actions[id][bestAction];

        //get actions available to the opponent and assume they will do a random action
        Types.ACTIONS[] oppActions = actions[oppID];
        acts[oppID] = oppActions[new Random().nextInt(oppActions.length)];

        advance(state, acts, drawingAgent);

        SingleTreeNode tn = new SingleTreeNode(this,bestAction,this.m_rnd, id, oppID, no_players, NUM_ACTIONS,
                actions, params, state, drawingAgent);
        children[bestAction] = tn;
        return tn;
    }

    public SingleTreeNode uct(StateObservationMulti state) {

        SingleTreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;
        for (SingleTreeNode child : this.children)
        {
            double hvVal = child.totValue;
            double childValue =  hvVal / (child.nVisits + this.epsilon);

            childValue = Utils.normalise(childValue, bounds[0], bounds[1]);
            //System.out.println("norm child value: " + childValue);

            double uctValue = childValue +
                    params.K * Math.sqrt(Math.log(this.nVisits + 1) / (child.nVisits + this.epsilon));

            uctValue = Utils.noise(uctValue, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly

            // small sampleRandom numbers: break ties in unexpanded nodes
            if (uctValue > bestValue) {
                selected = child;
                bestValue = uctValue;
            }
        }
        if (selected == null)
        {
            throw new RuntimeException("Warning! returning null: " + bestValue + " : " + this.children.length + " " +
            + bounds[0] + " " + bounds[1]);
        }

        //Roll the state:

        //need to provide actions for all players to advance the forward model
        Types.ACTIONS[] acts = new Types.ACTIONS[no_players];

        //set this agent's action
        acts[id] = actions[id][selected.childIdx];

        //get actions available to the opponent and assume they will do a random action
        Types.ACTIONS[] oppActions = actions[oppID];
        acts[oppID] = oppActions[new Random().nextInt(oppActions.length)];

        advance(state, acts, drawingAgent);

        return selected;
    }


    public double rollOut(StateObservationMulti state)
    {
        int thisDepth = this.m_depth;

        while (!finishRollout(state,thisDepth)) {

            //random move for all players
            Types.ACTIONS[] acts = new Types.ACTIONS[no_players];
            for (int i = 0; i < no_players; i++) {
                acts[i] = actions[i][m_rnd.nextInt(NUM_ACTIONS[i])];
            }
            advance(state, acts, drawingAgent);

            thisDepth++;
        }


        double delta = heuristic.evaluateState(state, id);

        if(delta < bounds[0])
            bounds[0] = delta;
        if(delta > bounds[1])
            bounds[1] = delta;

        //double normDelta = utils.normalise(delta ,lastBounds[0], lastBounds[1]);

        return delta;
    }

    public boolean finishRollout(StateObservationMulti rollerState, int depth)
    {
        if(depth >= params.SIMULATION_DEPTH)      //rollout end condition.
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
            if (result < n.bounds[0]) {
                n.bounds[0] = result;
            }
            if (result > n.bounds[1]) {
                n.bounds[1] = result;
            }
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

                double childValue = children[i].nVisits;
                childValue = Utils.noise(childValue, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly
                if (childValue > bestValue) {
                    bestValue = childValue;
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

            if(children[i] != null) {
                //double tieBreaker = m_rnd.nextDouble() * epsilon;
                double childValue = children[i].totValue / (children[i].nVisits + this.epsilon);
                childValue = Utils.noise(childValue, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly
                if (childValue > bestValue) {
                    bestValue = childValue;
                    selected = i;
                }
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

    void advance(StateObservationMulti state, Types.ACTIONS[] acts, DrawingAgent drawingAgent) {
        state.advance(acts);
        // Draw simulations
        if (DrawingAgent.drawing && drawingAgent != null) {
            Vector2d pos = state.getAvatarPosition(id);
            drawingAgent.updatePosThinking(pos);
        }
    }
}
