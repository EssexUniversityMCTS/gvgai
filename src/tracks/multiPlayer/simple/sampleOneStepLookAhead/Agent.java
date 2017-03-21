package tracks.multiPlayer.simple.sampleOneStepLookAhead;


import tracks.multiPlayer.tools.heuristics.SimpleStateHeuristic;
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
    int id; //ID of this player
    int no_players; //number of players in the game
    public static double epsilon = 1e-6;
    public static Random m_rnd;

    /**
     * initialize all variables for the agent
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @param playerID ID if this agent
     */
    public Agent(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, int playerID) {
        m_rnd = new Random();

        //get game information
        no_players = stateObs.getNoPlayers();
        id = playerID; //player ID of this agent
        oppID = (playerID + 1) % stateObs.getNoPlayers();
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

        //A random non-suicidal action by the opponent.
        Types.ACTIONS oppAction = getOppNotLosingAction(stateObs, id, oppID);
        SimpleStateHeuristic heuristic =  new SimpleStateHeuristic(stateObs);

        for (Types.ACTIONS action : stateObs.getAvailableActions(id)) {

            StateObservationMulti stCopy = stateObs.copy();

            //need to provide actions for all players to advance the forward model
            Types.ACTIONS[] acts = new Types.ACTIONS[no_players];

            //set this agent's action
            acts[id] = action;
            acts[oppID] = oppAction;

            stCopy.advance(acts);

            double Q = heuristic.evaluateState(stCopy, id);
            Q = Utils.noise(Q, this.epsilon, this.m_rnd.nextDouble());

            //System.out.println("Action:" + action + " score:" + Q);
            if (Q > maxQ) {
                maxQ = Q;
                bestAction = action;
            }
        }

        //System.out.println("======== " + getPlayerID() + " " + maxQ + " " + bestAction + "============");
        //System.out.println(elapsedTimer.remainingTimeMillis());
        return bestAction;
    }

    //Returns an action, at random, that the oppponet would make, assuming I do NIL, which wouldn't make it lose the game.
    private Types.ACTIONS getOppNotLosingAction(StateObservationMulti stm, int thisID, int oppID)
    {
        int no_players = stm.getNoPlayers();
        ArrayList<Types.ACTIONS> oppActions = stm.getAvailableActions(oppID);

        ArrayList<Types.ACTIONS> nonDeathActions = new ArrayList<>();

        //Look for the opp actions that would not kill the opponent.
        for (Types.ACTIONS action : stm.getAvailableActions(oppID)) {
            Types.ACTIONS[] acts = new Types.ACTIONS[no_players];
            acts[thisID] = Types.ACTIONS.ACTION_NIL;
            acts[oppID] = action;

            StateObservationMulti stCopy = stm.copy();
            stCopy.advance(acts);

            if(stCopy.getMultiGameWinner()[oppID] != Types.WINNER.PLAYER_LOSES)
                nonDeathActions.add(action);
        }

        if(nonDeathActions.size() == 0)
            //Simply random
            return oppActions.get(new Random().nextInt(oppActions.size()));
        else
            //Random, but among those that would not kill the opponent.
            return (Types.ACTIONS) Utils.choice(nonDeathActions.toArray(), m_rnd);
    }
}
