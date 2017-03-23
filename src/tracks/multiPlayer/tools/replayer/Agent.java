package tracks.multiPlayer.tools.replayer;

import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractMultiPlayer {

    /**
     * List of actions to execute. They must be loaded using loadActions().
     */
    private ArrayList<Types.ACTIONS> actions;

    /**
     * Current index of the action to be executed.
     */
    private int actionIdx;


    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     * @param playerID ID of this player.
     */
    public Agent(StateObservationMulti so, ElapsedCpuTimer elapsedTimer, int playerID)
    {
        actions = new ArrayList<Types.ACTIONS>();
    }

    /**
     * Loads the action from the contents of the object received as parameter.
     * @param actionsToLoad ArrayList of actions to execute.
     */
    public void setActions(ArrayList<Types.ACTIONS> actionsToLoad)
    {
        actionIdx = 0;
        this.actions = actionsToLoad;
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer)
    {
        Types.ACTIONS action = actions.get(actionIdx);
        actionIdx++;

        long remaining = elapsedTimer.remainingTimeMillis();
        while(remaining > 1)
        {
            //This allows visualization of the replay.
            remaining = elapsedTimer.remainingTimeMillis();
        }

        return action;
    }
}
