package controllers.human;

import core.game.Game;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;
import tools.Vector2d;

/**
 * Created by diego on 06/02/14.
 */
public class Agent extends AbstractPlayer
{

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
    {
        Vector2d move = Utils.processMovementActionKeys(Game.ki.getMask());
        boolean useOn = Utils.processUseKey(Game.ki.getMask());

        //In the keycontroller, move has preference.
        Types.ACTIONS action = Types.ACTIONS.fromVector(move);

        if(action == Types.ACTIONS.ACTION_NIL && useOn)
            action = Types.ACTIONS.ACTION_USE;

        return action;
    }

    public void result(StateObservation stateObservation, ElapsedCpuTimer elapsedCpuTimer)
    {
        //System.out.println("Thanks for playing!");
    }
}
