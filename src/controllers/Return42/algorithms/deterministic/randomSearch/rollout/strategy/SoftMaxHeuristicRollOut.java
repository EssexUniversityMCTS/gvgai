package controllers.Return42.algorithms.deterministic.randomSearch.rollout.strategy;

import java.util.ArrayList;
import java.util.Random;

import controllers.Return42.heuristics.action.ActionHeuristic;
import controllers.Return42.util.Util;
import ontology.Types.ACTIONS;
import core.game.StateObservation;

/**
 * Chooses randomly an action. The probability for each action is determined by
 * the softmax function.
 * 
 * @see Util#softMax(double[], double)
 * 
 * @author Frederik Buss-Joraschek
 *
 */
public class SoftMaxHeuristicRollOut implements RollOutStrategy {
	
	private final Random random;
	private final ActionHeuristic actionHeuristic;
	private final double softMaxTemperature;

	public SoftMaxHeuristicRollOut(ActionHeuristic actionHeuristic, double softMaxTemperature) {
		this.random = new Random();
		this.actionHeuristic = actionHeuristic;
		this.softMaxTemperature = softMaxTemperature;
	}

	@Override
	public void beginnRollOutPhase( StateObservation state ) {
	}

	@Override
	public ACTIONS getNextAction(StateObservation state) {
		ArrayList<ACTIONS> availableActions = state.getAvailableActions();
		double[] scores = new double[availableActions.size()];
		
		for(int i = 0; i < scores.length; i++){
			scores[i] = actionHeuristic.evaluate(state, availableActions.get(i));
		}
		
		double results[] = Util.softMax(scores, softMaxTemperature);
		
		double randomNumber = random.nextDouble();		
		int actionIndex = 0;
		while(randomNumber > results[actionIndex]){
			randomNumber -= results[actionIndex];
			actionIndex++;
		}
		

		return availableActions.get(actionIndex);
	}

}
