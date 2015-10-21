package controllers.Return42.algorithms.deterministic.puzzleSolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import controllers.Return42.algorithms.deterministic.puzzleSolver.cache.StateCache;
import controllers.Return42.algorithms.deterministic.puzzleSolver.costFunction.AStarCostFunction;
import controllers.Return42.algorithms.deterministic.puzzleSolver.heuristic.AStarHeuristic;
import controllers.Return42.algorithms.deterministic.puzzleSolver.simulation.AStarAdvanceFunction;
import controllers.Return42.algorithms.deterministic.puzzleSolver.simulation.AdvanceResult;
import controllers.Return42.util.TimeoutException;
import ontology.Types;
import ontology.Types.ACTIONS;
import ontology.Types.WINNER;
import tools.ElapsedCpuTimer;
import core.game.StateObservation;

/**
 * Created by Oliver on 03.05.2015.
 */
public class AStar {

    private static final int MAX_DEPTH = 300;

    private final int maxIterations;
    private final PriorityQueue<AStarNode> nonExpandedStates;
    private final Map<Integer, AStarNode> allNodes;
    private final StateCache cache;
    private final AStarCostFunction costFunction;
    private final AStarHeuristic heuristic;
    private final StateBuilder builder;
    private final AStarAdvanceFunction advancer;
    private final boolean hasUseAction;
    private final AStarStateHasher hasher;
    private final StateObservation initialState;

    private int iterationsPerformed;
    private int ticksWastedSearching;
    private AStarNode currentBestSolution;
    private double currentBestSolutionScore;
    private boolean currentBestSolutionWins;

    public AStar( int maxIterations, StateObservation initialState, AStarCostFunction costFunction, AStarHeuristic heuristic, StateCache cache, 
    		StateBuilder builder, AStarAdvanceFunction advancer, AStarStateHasher hasher ) {
    	this.maxIterations = maxIterations;
        this.cache = cache;
        this.heuristic = heuristic;
        this.costFunction = costFunction;
        this.builder = builder;
        this.advancer = advancer;
        this.hasher = hasher;
        this.initialState = initialState;
        this.allNodes = new HashMap<Integer, AStarNode>();

        AStarNode firstNode = new AStarNode( null, null, 0, 0, 0 );
        this.cache.storeState( firstNode, initialState );
        this.allNodes.put( hasher.hash(initialState), firstNode );
        
        this.nonExpandedStates = new PriorityQueue<>();
        this.nonExpandedStates.add( firstNode );
        this.hasUseAction = initialState.getAvailableActions().contains(ACTIONS.ACTION_USE);
        
        this.currentBestSolution = firstNode;
        this.currentBestSolutionWins = initialState.getGameWinner() == WINNER.PLAYER_WINS;
        this.currentBestSolutionScore = initialState.getGameScore();
    }

    public StateObservation getStartState() {
    	return initialState;
    }
    
    public Result simulate( ElapsedCpuTimer timer ) {
        ticksWastedSearching++;
        while ( shouldPerformOneMoreIterations( timer ) ) {
            iterate( timer );
            iterationsPerformed++;
            
            if (iterationsPerformed%1000 == 0)
            	System.out.println(iterationsPerformed);
        }

        if (currentBestSolutionWins) {
            System.out.println( "A-Star found a solution in "+iterationsPerformed+ " iterations.");
            return Result.FOUND_SOLUTION;
        }
        else if (nonExpandedStates.isEmpty() || ticksWastedSearching > maxIterations) {
            System.out.println( "A-Star found no solution after "+iterationsPerformed+ " iterations.");
            return Result.NO_SOLUTION_FOUND;
        }
        else
            return Result.NEED_MORE_TIME;
    }

    private boolean shouldPerformOneMoreIterations(ElapsedCpuTimer timer) {
        if (nonExpandedStates.isEmpty())
            return false;

        if (currentBestSolutionWins)
            return false;

        return !timer.exceededMaxTime();
    }

    private void iterate( ElapsedCpuTimer timer ) {
        AStarNode node = nonExpandedStates.poll();
        StateObservation state = cache.getState( node );

        // cache miss - we have to reconstruct the state.
        if (state == null) {
            try {
                state = builder.buildState(node, timer);
            } catch (TimeoutException e) {
                // add the node back to the queue, if our time is out.
                nonExpandedStates.add( node );
                return;
            }
        }

        expandNode(node, state );
    }

    private void expandNode(AStarNode node, StateObservation state) {
    	if ( isBetterThanCurrentBestSolution( node, state ) ) {
            this.currentBestSolution = node;
            this.currentBestSolutionScore = state.getGameScore();
            this.currentBestSolutionWins = state.getGameWinner() == WINNER.PLAYER_WINS;
            
            if (currentBestSolutionWins) {
            	return;
            }
        }

        if (hasUseAction) {
        	expandNodeWithAction(node, state, Types.ACTIONS.ACTION_USE );
        }
        
        expandNodeWithAction(node, state, Types.ACTIONS.ACTION_RIGHT);
        expandNodeWithAction(node, state, Types.ACTIONS.ACTION_LEFT);
        expandNodeWithAction(node, state, Types.ACTIONS.ACTION_DOWN);
        expandNodeWithAction(node, state, Types.ACTIONS.ACTION_UP);
    }

    private boolean isBetterThanCurrentBestSolution( AStarNode node, StateObservation state) {
    	if (state.getGameWinner() == WINNER.PLAYER_LOSES)
    		return false;

    	if (state.getGameWinner() == WINNER.PLAYER_WINS && !currentBestSolutionWins)
    		return true;
    	
    	if (state.getGameWinner() != WINNER.PLAYER_WINS && currentBestSolutionWins)
    		return false;
    	
    	return state.getGameScore() > currentBestSolutionScore;
	}

	private void expandNodeWithAction( AStarNode node, StateObservation lastState, Types.ACTIONS action) {
        AdvanceResult advanced = advancer.advance( lastState, lastState.copy(), action );
    	StateObservation nextState = advanced.getNewState();
    	ACTIONS[] actions = advanced.getActionsPerformed();
    	
    	int depth = node.getDepth() +1;

        // do not visit the same state twice.
        if (nextState.getGameWinner() == WINNER.PLAYER_LOSES || depth > MAX_DEPTH ) {
            return;
        }

        double g = node.getG() + costFunction.evaluate( lastState, nextState );
        double h = heuristic.evaluate( lastState, nextState);

        int hash = hasher.hash(nextState);
        if (allNodes.containsKey(hash)) {

        	AStarNode nextNode = allNodes.get( hash );
       	
        	if ( nextNode != node && nextNode.getG() > g ) {
        		nextNode.updatePredecessor( node, actions, g, depth );
        		nonExpandedStates.remove( nextNode );
        		nonExpandedStates.add( nextNode );
        	}
        } else {
        	 AStarNode nextNode = new AStarNode( node, actions, g, h, depth );
             cache.storeState(nextNode, nextState);
             nonExpandedStates.add(nextNode);
             allNodes.put( hash, nextNode );
        }
    }

    public List<Types.ACTIONS> getSolution() {
        return AStarUtils.extractActions( currentBestSolution );
    }
    
    public enum Result {
        FOUND_SOLUTION,
        NEED_MORE_TIME,
        NO_SOLUTION_FOUND
    }
}
