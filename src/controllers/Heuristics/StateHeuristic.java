package controllers.Heuristics;

import core.game.StateObservation;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 11/02/14
 * Time: 15:43
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public abstract class StateHeuristic {

    abstract public double evaluateState(StateObservation stateObs);
}
