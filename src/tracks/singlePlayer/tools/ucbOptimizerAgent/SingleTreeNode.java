package tracks.singlePlayer.tools.ucbOptimizerAgent;

import java.util.Random;

import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;

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
    public double maxValue;
    public int nVisits;
	public int[][] visitedTiles;
    public static Random m_rnd;
    private int m_depth;
    protected static double[] bounds = new double[]{Double.MAX_VALUE, -Double.MAX_VALUE};
    public SingleTreeNode(Random rnd, int[][] visSpace) {
        this(null, null, rnd, visSpace);
    }

    public static int totalIters = 0;

    public SingleTreeNode(StateObservation state, SingleTreeNode parent, Random rnd, int[][] visSpace) {
        this.state = state;
        this.parent = parent;
        this.m_rnd = rnd;
        this.visitedTiles = visSpace;
        if(state != null){
	        this.visitedTiles = Helper.updateTilesValue(visSpace, (int)(state.getAvatarPosition().x / state.getBlockSize()), 
	        		(int)(state.getAvatarPosition().y / state.getBlockSize()));
        }
        children = new SingleTreeNode[Agent.NUM_ACTIONS];
        totValue = 0.0;
        maxValue = 0.0;
        if(parent != null)
            m_depth = parent.m_depth+1;
        else
            m_depth = 0;
    }


    public void mctsSearch(ElapsedCpuTimer elapsedTimer) {

        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int numIters = 0;

        int remainingLimit = 5;
        while(remaining > 2*avgTimeTaken + Agent.safetyMargin && remaining > remainingLimit + Agent.safetyMargin){
            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
            SingleTreeNode selected = treePolicy();
            double delta = selected.rollOut();
            backUp(selected, delta);

            numIters++;
            acumTimeTaken += (elapsedTimerIteration.elapsedMillis()) ;

            avgTimeTaken  = acumTimeTaken/numIters;
            remaining = elapsedTimer.remainingTimeMillis();
            //System.out.println(elapsedTimerIteration.elapsedMillis() + " --> " + acumTimeTaken + " (" + remaining + ")");
        }
        //System.out.println("-- " + numIters + " -- ( " + avgTimeTaken + ")");
        totalIters = numIters;

        //ArcadeMachine.performance.add(numIters);
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

    public int getCurrentAction(SingleTreeNode node){
    	if(node!=null && node.parent != null){
    		for(int i=0; i<node.parent.children.length; i++){
    			if(node.parent.children[i] == node){
    				return i;
    			}
    		}
    	}
    	return -1;
    }
    
    public int getNumberOfReverseActions(SingleTreeNode node){
    	int result = 0;
    	
    	int previousAction = -1;
    	while(node != null){
    		int currentAction = getCurrentAction(node);
    		if(previousAction != -1 && currentAction != -1){
    			if(Helper.isOpposite(Agent.actions[previousAction], Agent.actions[currentAction])){
    				result += 1;
    			}
    		}
    		node = node.parent;
    	}
    	
    	return result;
    }
    
    public int getNumberOfSameActions(SingleTreeNode node){
    	int result = 0;
    	
    	int previousAction = -1;
    	while(node != null){
    		int currentAction = getCurrentAction(node);
    		if(previousAction != -1 && currentAction != -1){
    			if(currentAction == previousAction){
    				result += 1;
    			}
    		}
    		node = node.parent;
    	}
    	
    	return result;
    }
    
    public boolean isUselessMove(StateObservation oldState, StateObservation newState){
    	return (oldState.getAvatarPosition().equals(newState.getAvatarPosition()) && 
    			oldState.getAvatarOrientation().equals(newState.getAvatarOrientation()));
    }
    
    public int getNumberOfUselessMoves(SingleTreeNode node){
    	int result = 0;
    	
    	while(node != null){
    		int currentAction = getCurrentAction(node);
    		if(currentAction != -1 && Agent.actions[currentAction] != Types.ACTIONS.ACTION_USE){
    			if(isUselessMove(node.parent.state, node.state)){
    				result += 1;
    			}
    		}
    		node = node.parent;
    	}
    	
    	return result;
    }
    
    public int getMaxVisitedValue(){
    	int result = 0;
    	
    	for(int i=0; i<visitedTiles.length; i++){
    		for(int j=0; j<visitedTiles[i].length; j++){
    			if(visitedTiles[i][j] > result){
    				result = visitedTiles[i][j];
    			}
    		}
    	}
    	
    	return result;
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

        SingleTreeNode tn = new SingleTreeNode(nextState, this, this.m_rnd, visitedTiles);
        children[bestAction] = tn;
        return tn;

    }

    public SingleTreeNode uct() {

        SingleTreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;
        double[] values = new double[32];
        for (SingleTreeNode child : this.children)
        {
        	values[Helper.TREE_CHILD_DEPTH] = Double.valueOf(child.m_depth);        	
        	values[Helper.TREE_CHILD_VALUE] = Double.valueOf(child.totValue);
        	values[Helper.TREE_PARENT_VISITS] = Double.valueOf(this.nVisits);
        	values[Helper.TREE_CHILD_VISITS] = Double.valueOf(child.nVisits);
        	values[Helper.TREE_CHILD_MAX_VALUE] = Double.valueOf(child.maxValue);
        	
        	//Game related variables
        	values[Helper.HISTORY_REVERSE_VALUE] = getNumberOfReverseActions(child);
        	values[Helper.HISTORY_REPEATING_VALUE] = getNumberOfSameActions(child);
        	values[Helper.USELESS_MOVE_VALUE] = getNumberOfUselessMoves(child);
        	int x = (int)(child.state.getAvatarPosition().x / child.state.getBlockSize());
        	int y = (int)(child.state.getAvatarPosition().y / child.state.getBlockSize());
        	if(x >= 0 && y>= 0 && x < visitedTiles.length && y < visitedTiles[0].length){
        		values[Helper.SPACE_EXPLORATION_VALUE] = visitedTiles[x][y];
        	}
        	else{
        		values[Helper.SPACE_EXPLORATION_VALUE] = getMaxVisitedValue();
        	}
        	values[Helper.SPACE_EXPLORATION_MAX_VALUE] = getMaxVisitedValue();
        	
        	//VGDL related variables
        	values[Helper.DISTANCE_MAX_IMMOVABLE] = 
        			Helper.getMaxObservation(child.state.getImmovablePositions(), child.state.getAvatarPosition());
        	values[Helper.DISTANCE_MAX_MOVABLE] =
        			Helper.getMaxObservation(child.state.getMovablePositions(), child.state.getAvatarPosition());
        	values[Helper.DISTANCE_MAX_NPC] =
        			Helper.getMaxObservation(child.state.getNPCPositions(), child.state.getAvatarPosition());
        	values[Helper.DISTANCE_MAX_PORTAL] = 
        			Helper.getMaxObservation(child.state.getPortalsPositions(), child.state.getAvatarPosition());
        	values[Helper.DISTANCE_MAX_RESOURCE] =
        			Helper.getMaxObservation(child.state.getResourcesPositions(), child.state.getAvatarPosition());
        	
        	values[Helper.DISTANCE_MIN_IMMOVABLE] = 
        			Helper.getMinObservation(child.state.getImmovablePositions(), child.state.getAvatarPosition());
        	values[Helper.DISTANCE_MIN_MOVABLE] =
        			Helper.getMinObservation(child.state.getMovablePositions(), child.state.getAvatarPosition());
        	values[Helper.DISTANCE_MIN_NPC] =
        			Helper.getMinObservation(child.state.getNPCPositions(), child.state.getAvatarPosition());
        	values[Helper.DISTANCE_MIN_PORTAL] =
        			Helper.getMinObservation(child.state.getPortalsPositions(), child.state.getAvatarPosition());
        	values[Helper.DISTANCE_MIN_RESOURCE] =
        			Helper.getMinObservation(child.state.getResourcesPositions(), child.state.getAvatarPosition());
        	
        	values[Helper.DISTANCE_TOT_IMMOVABLE] =
        			Helper.getTotObservation(child.state.getImmovablePositions(), child.state.getAvatarPosition());
        	values[Helper.DISTANCE_TOT_MOVABLE] =
        			Helper.getTotObservation(child.state.getMovablePositions(), child.state.getAvatarPosition());
        	values[Helper.DISTANCE_TOT_NPC] =
        			Helper.getTotObservation(child.state.getNPCPositions(), child.state.getAvatarPosition());
        	values[Helper.DISTANCE_TOT_PORTAL] = 
        			Helper.getTotObservation(child.state.getPortalsPositions(), child.state.getAvatarPosition());
        	values[Helper.DISTANCE_TOT_RESOURCE] =
        			Helper.getTotObservation(child.state.getResourcesPositions(), child.state.getAvatarPosition());
        	
        	values[Helper.NUMBER_IMMOVABLE] = 
        			Helper.getObservationLength(child.state.getImmovablePositions());
        	values[Helper.NUMBER_MOVABLE] =
        			Helper.getObservationLength(child.state.getMovablePositions());
        	values[Helper.NUMBER_NPC] =
        			Helper.getObservationLength(child.state.getNPCPositions());
        	values[Helper.NUMBER_PORTAL] =
        			Helper.getObservationLength(child.state.getPortalsPositions());
        	values[Helper.NUMBER_RESOURCE] =
        			Helper.getObservationLength(child.state.getResourcesPositions());
        	
        	values[Helper.GRID_WIDTH] = state.getObservationGrid()[0].length;
        	values[Helper.GRID_HEIGHT] = state.getObservationGrid().length;
        	
            double uctValue = Agent.ucb.evaluate(values, Agent.parameters);

            // small sampleRandom numbers: break ties in unexpanded nodes
            uctValue = Utils.noise(uctValue, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly

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
                hvVal = Utils.noise(hvVal, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly
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

        if(delta < bounds[0])
            bounds[0] = delta;

        if(delta > bounds[1])
            bounds[1] = delta;

        return delta;
    }

    public double value(StateObservation a_gameState) {
        boolean gameOver = a_gameState.isGameOver();
        Types.WINNER win = a_gameState.getGameWinner();
        double rawScore = a_gameState.getGameScore();

        if(gameOver && win == Types.WINNER.PLAYER_LOSES)
            rawScore += HUGE_NEGATIVE;

        if(gameOver && win == Types.WINNER.PLAYER_WINS)
            rawScore += HUGE_POSITIVE;

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
            if(result > n.maxValue){
            	n.maxValue = result;
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
}
