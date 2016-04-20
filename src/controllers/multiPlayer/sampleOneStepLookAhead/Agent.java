package controllers.multiPlayer.sampleOneStepLookAhead;


import controllers.multiPlayer.heuristics.SimpleStateHeuristic;
import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;

import java.util.ArrayList;
import java.util.Random;

/**
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractMultiPlayer {
    int oppID; //player ID of the opponent
    public static double epsilon = 1e-6;
    public static Random m_rnd;

    public Agent(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer) {
        m_rnd = new Random();
    }

    /**
     *
     * Very simple one step lookahead agent.
     * Pass player ID to all state observation methods to query the right player.
     * Omitting the player ID will result in it being set to the default 0 (first player, whichever that is).
     *
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer) {

        Types.ACTIONS bestAction = null;
        double maxQ = Double.NEGATIVE_INFINITY;

        int no_players = stateObs.getNoPlayers();
        int id = getPlayerID(); //player ID of this agent
        oppID = (getPlayerID() + 1) % stateObs.getNoPlayers();

        SimpleStateHeuristic heuristic =  new SimpleStateHeuristic(stateObs);

        for (Types.ACTIONS action : stateObs.getAvailableActions(id)) {

            StateObservationMulti stCopy = stateObs.copy();

            //need to provide actions for all players to advance the forward model
            Types.ACTIONS[] acts = new Types.ACTIONS[no_players];

            //set this agent's action
            acts[id] = action;

            //get actions available to the opponent and assume they will do a random action
            ArrayList<Types.ACTIONS> oppActions = stateObs.getAvailableActions(oppID);
            acts[oppID] = oppActions.get(new Random().nextInt(oppActions.size()));

            stCopy.advance(acts);
            double Q = heuristic.evaluateState(stCopy, id);
            Q = Utils.noise(Q, this.epsilon, this.m_rnd.nextDouble());

            //System.out.println("Action:" + action + " score:" + Q);
            if (Q > maxQ) {
                maxQ = Q;
                bestAction = action;
            }
        }

        //System.out.println("======== "  + maxQ + " " + bestAction + "============");
        //System.out.println("Action:" + bestAction);
        return bestAction;
    }
}
