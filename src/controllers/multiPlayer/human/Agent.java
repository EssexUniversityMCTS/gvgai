package controllers.multiPlayer.human;

import core.game.Game;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.Direction;
import tools.ElapsedCpuTimer;
import tools.Utils;

/**
 * Created by diego on 06/02/14.
 */
public class Agent extends AbstractMultiPlayer
{

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservationMulti so, ElapsedCpuTimer elapsedTimer)
    {
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
        //int id = (getPlayerID() + 1) % stateObs.getNoPlayers();
        Direction move = Utils.processMovementActionKeys(Game.humanki.getMask());
        boolean useOn = Utils.processUseKey(Game.humanki.getMask());

        //In the keycontroller, move has preference.
        Types.ACTIONS action = Types.ACTIONS.fromVector(move);
        if(action == Types.ACTIONS.ACTION_NIL && useOn)
            action = Types.ACTIONS.ACTION_USE;

        return action;
    }

    public void result(StateObservation stateObservation, ElapsedCpuTimer elapsedCpuTimer)
    {
        //System.out.println("Thanks for playing! " + stateObservation.isAvatarAlive());
    }
}
