package controllers.Return42.heuristics.action;

import ontology.Types.ACTIONS;
import core.game.StateObservation;

/**
 * Returns a score that indicates the approximate value of applying the action to the state.
 * 
 * @author Frederik Buss-Joraschek
 *
 */
public interface ActionHeuristic {
	double evaluate( StateObservation state, ACTIONS action );
}
