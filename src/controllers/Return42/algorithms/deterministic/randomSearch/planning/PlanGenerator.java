package controllers.Return42.algorithms.deterministic.randomSearch.planning;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import controllers.Return42.algorithms.deterministic.randomSearch.depthControl.DepthControl;
import controllers.Return42.algorithms.deterministic.randomSearch.rollout.heuristic.RolloutHeuristic;
import controllers.Return42.algorithms.deterministic.randomSearch.rollout.picker.RolloutPicker;
import controllers.Return42.algorithms.deterministic.randomSearch.rollout.strategy.RollOutStrategy;
import controllers.Return42.util.NilMoveChecker;
import controllers.Return42.util.StateObservationUtils;
import ontology.Types.ACTIONS;
import ontology.Types.WINNER;
import tools.ElapsedCpuTimer;
import core.game.StateObservation;

public class PlanGenerator {

	private final RolloutHeuristic heurisic;
	private final DepthControl depthControl;
	private final RolloutPicker rolloutPicker;

	public PlanGenerator( RolloutHeuristic heuristic, DepthControl depthControl, RolloutPicker rolloutPicker ) {
		this.heurisic = heuristic;
		this.depthControl = depthControl;
		this.rolloutPicker = rolloutPicker;
	}

	public List<Plan> randomWalk(StateObservation state, ElapsedCpuTimer timer) {
		List<Step> takenSteps = new LinkedList<>();
		List<Plan> newPlans = new LinkedList<>();
		
		state = state.copy();
		int iteration = 0;
		
		heurisic.preStep(state);
		double lastScore = heurisic.postStep(state);
		
		RollOutStrategy strategy = rolloutPicker.getCurrentRolloutStrategy();
		strategy.beginnRollOutPhase( state );
		
		while( shouldPerformOneMoreIteration( state, iteration, timer) ) {
			iteration++;

			heurisic.preStep( state );
			ACTIONS nextAction = strategy.getNextAction(state);
			Step nextStep = new Step( nextAction, StateObservationUtils.getNpcPositions( state ) );
			takenSteps.add( nextStep );
			state.advance(nextAction);

			double score = heurisic.postStep( state );
			boolean didWin = state.getGameWinner() == WINNER.PLAYER_WINS;

			if (score > lastScore || didWin) {
				newPlans.add( new Plan( new LinkedList<>( takenSteps ), score, didWin ) );
				lastScore = score;
			}
		}

		boolean didFindPlans = !newPlans.isEmpty();
		rolloutPicker.iterationFinished( didFindPlans );
		depthControl.iterationFinished( didFindPlans );

		return newPlans;
	}
	
	private boolean shouldPerformOneMoreIteration(StateObservation state, int iterations, ElapsedCpuTimer timer) {
		if (state.isGameOver())
			return false;

		int iterationLimit = depthControl.getMaxIterationDepth();
		if (iterations > iterationLimit)
			return false;

		if (timer.exceededMaxTime())
			return false;

		return true;
	}

	public List<Plan> generateSingleStepPlans( StateObservation state, boolean onlyNilMoves ) {
		List<Plan> singleStepPlans = new LinkedList<>();
		List<ACTIONS> actions = new LinkedList<>( state.getAvailableActions() );
		Collections.shuffle( actions );
		
		for( ACTIONS action: actions ) {
			StateObservation stateCopy = state.copy();
			
			heurisic.preStep( stateCopy );
			boolean isNilMove = NilMoveChecker.advanceStateAndCheckIfIsNilMove( stateCopy, action );
			double score = heurisic.postStep(stateCopy);

			Step step = new Step( action, StateObservationUtils.getNpcPositions(stateCopy) );
			boolean didWin = state.getGameWinner() == WINNER.PLAYER_WINS;
			
			Plan newPlan = new Plan( Collections.singletonList( step ), score, didWin );
			if (isNilMove || !onlyNilMoves ) {
				singleStepPlans.add( newPlan );
			}
		}
		
		return singleStepPlans;
	}
}
