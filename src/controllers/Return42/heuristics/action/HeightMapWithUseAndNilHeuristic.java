package controllers.Return42.heuristics.action;

import ontology.Types.ACTIONS;
import controllers.Return42.knowledgebase.KnowledgeBase;
import core.game.StateObservation;

/**
 * Like {@link HeightMapHeuristic}, but this heuristic boosts the Use and Nil action by a random number
 * 
 * @author Frederik Buss-Joraschek
 *
 */
public class HeightMapWithUseAndNilHeuristic implements ActionHeuristic {

	private final HeightMapHeuristic heightMapHeuristic;

	public HeightMapWithUseAndNilHeuristic( KnowledgeBase knowledge ) {
		this.heightMapHeuristic = new HeightMapHeuristic( knowledge );
	}

	@Override
	public double evaluate(StateObservation state, ACTIONS action) {
		double standStillScore = heightMapHeuristic.evaluate(state, ACTIONS.ACTION_NIL);
		if (action == ACTIONS.ACTION_NIL || action == ACTIONS.ACTION_USE) {
			return Math.random();
		}

		return heightMapHeuristic.evaluate(state, action) - standStillScore; //
	}

}
