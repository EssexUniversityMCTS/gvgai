package controllers.Return42.algorithms.deterministic.randomSearch;

import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

import controllers.Return42.algorithms.deterministic.DeterministicAgent;
import controllers.Return42.algorithms.deterministic.randomSearch.planning.Plan;
import controllers.Return42.algorithms.deterministic.randomSearch.planning.PlanGenerator;
import controllers.Return42.algorithms.deterministic.randomSearch.planning.PlanKeeper;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import core.game.StateObservation;

public class RandomSearch implements DeterministicAgent {

	private final PlanKeeper planKeeper;
	private final PlanGenerator planGenerator;
	private final int iterationLimit;
	private final boolean randomMovesHaveToBeNilMoves;
	
	int iterations;

	public RandomSearch(PlanKeeper planKeeper, PlanGenerator planGenerator, int iterationLimit, boolean randomMovesHaveToBeNilMoves) {
		this.planKeeper = planKeeper;
		this.planGenerator = planGenerator;
		this.iterationLimit = iterationLimit;
		this.randomMovesHaveToBeNilMoves = randomMovesHaveToBeNilMoves;
		this.iterations = 0;
	}
	
	public void updatePlansWithNewState( StateObservation state ) {
		if (state.getGameTick() == 0) {
			// we found some plans in the constructor phase. No need to discard them.
			return;
		}

		planKeeper.deletePlansThatDoNotMatch( state );
	}

	public void clearAllPlans() {
		planKeeper.clear();
	}
	
	@Override
	public ACTIONS act(StateObservation state, ElapsedCpuTimer timer) {
        iterations++;
        updatePlansWithNewState(state);

        planKeeper.addPlans( planGenerator.generateSingleStepPlans(state, randomMovesHaveToBeNilMoves ) );
        planKeeper.addPlans( generateRandomPlans( state, timer ) );
		
		if (planKeeper.hasAnyPlan()) {
			Plan bestPlan = planKeeper.getBestPlan();
			return bestPlan.getNextStep().getAction();
		} else {
			return ACTIONS.ACTION_NIL;
		}
	}
	
	private List<Plan> generateRandomPlans(StateObservation state, ElapsedCpuTimer elapsedTimer) {
		long initialTime = elapsedTimer.remainingTimeMillis();
		List<Plan> plans = new LinkedList<Plan>();

		int iterations = 0;
		while( hasEnoughTimeForOneMoreIteration( initialTime, iterations, elapsedTimer.remainingTimeMillis() ) ) {
			plans.addAll( planGenerator.randomWalk( state, elapsedTimer ) );
			iterations++;
		}
		
		return plans;
	}

	private boolean hasEnoughTimeForOneMoreIteration(long initialTime, int iterations, long remainingTimeMillis) {

		if (iterations == 0)
			return true;
		
		double msPerIteration = (initialTime-remainingTimeMillis) / iterations;
		return remainingTimeMillis > 2 * msPerIteration;
	}

	@Override
	public void useConstructorExtraTime( StateObservation state, ElapsedCpuTimer timer) {
	}

	@Override
	public boolean didFinish() {
		return iterations >= iterationLimit;
	}

	@Override
	public void draw(Graphics2D g) {
	}
	
}
