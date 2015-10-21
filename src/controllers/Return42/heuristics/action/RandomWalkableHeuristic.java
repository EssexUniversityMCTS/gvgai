package controllers.Return42.heuristics.action;

import ontology.Types.ACTIONS;
import controllers.Return42.knowledgebase.GameInformation;
import controllers.Return42.knowledgebase.WalkableSpaceGenerator;
import core.game.StateObservation;

public class RandomWalkableHeuristic implements ActionHeuristic {

	private ActionHeuristic walkableHeuristic;

	public RandomWalkableHeuristic(GameInformation gameInformation, WalkableSpaceGenerator walkableSpaceGenerator) {
		walkableHeuristic = new WalkableHeuristic(gameInformation, walkableSpaceGenerator);
	}

	/**
	 * Returns a value beetween -1 and 1.
	 */
	@Override
	public double evaluate(StateObservation state, ACTIONS action) {
		if (action == ACTIONS.ACTION_NIL || action == ACTIONS.ACTION_USE) {
			return Math.random();
		}

		double score = walkableHeuristic.evaluate(state, action);
		return Math.random() + score;
	}

}
