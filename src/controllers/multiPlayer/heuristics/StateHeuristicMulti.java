package controllers.multiPlayer.heuristics;

import core.game.StateObservationMulti;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 11/02/14
 * Time: 15:43
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public abstract class StateHeuristicMulti {

    abstract public double evaluateState(StateObservationMulti stateObs, int playerID);
}
