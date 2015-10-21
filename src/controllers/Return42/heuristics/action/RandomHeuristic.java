package controllers.Return42.heuristics.action;

import ontology.Types.ACTIONS;
import core.game.StateObservation;

/**
 * Pure random heuristic
 * 
 * @author Frederik Buss-Joraschek
 *
 */
public class RandomHeuristic implements ActionHeuristic {

	/**
	 * Redirects to {@link Math#random()}
	 */
	@Override
	public double evaluate(StateObservation state, ACTIONS action) {
		return Math.random();
	}

}
