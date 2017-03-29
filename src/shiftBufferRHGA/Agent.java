package shiftBufferRHGA;

import core.game.StateObservation;
import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import shiftBufferRHGA.evodef.EvoAlg;
import shiftBufferRHGA.evodef.GameActionSpaceAdapter;
import shiftBufferRHGA.evodef.GameActionSpaceAdapterMulti;
import shiftBufferRHGA.evodef.SearchSpaceUtil;
import shiftBufferRHGA.rhAlgo.search.RHGA;

import java.util.ArrayList;

public class Agent extends AbstractMultiPlayer {

    public int num_actions;

    // these used to be static, but have made them instance variables
    // to allow experiments between multiple instances of the agent
    // with different values
    public int sequenceLength = 5;
    public boolean useShiftBuffer = true;
    public int nResamplings = 1;
    int nEvals;
    public EvoAlg evoAlg;

    public int[] NUM_ACTIONS;
    public Types.ACTIONS[][] actions;
    public int id, oppID, no_players;
    public ElapsedCpuTimer timer;

    // int index;
    int[] solution;
    // will only recalculate after this number of steps
    // static int playoutLength = 1;

//    public static void main(String[] args) {
//        System.out.println();
//    }


    /**
     * GVGAI agent constructor
     * @param so
     * @param elapsedTimer
     * @param playerID
     */
    public Agent(StateObservationMulti so, ElapsedCpuTimer elapsedTimer, int playerID) {
        this.timer = elapsedTimer;
        this.evoAlg = new RHGA(nResamplings, useShiftBuffer);
        //get game information
        no_players = so.getNoPlayers();
        id = playerID;
        oppID = (id + 1) % so.getNoPlayers();

        //Get the actions for all players in a static array.
        NUM_ACTIONS = new int[no_players];
        actions = new Types.ACTIONS[no_players][];
        for (int i = 0; i < no_players; i++) {

            ArrayList<Types.ACTIONS> act = so.getAvailableActions(i);

            actions[i] = new Types.ACTIONS[act.size()];
            for (int j = 0; j < act.size(); ++j) {
                actions[i][j] = act.get(j);
            }
            NUM_ACTIONS[i] = actions[i].length;
        }
    }

    /**
     * Public constructor with state observation and time due.
     *
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservationMulti so, ElapsedCpuTimer elapsedTimer,
                 EvoAlg evoAlg, int playerID, int nEvals)
    {
        //get game information

        this.evoAlg = evoAlg;
        no_players = so.getNoPlayers();
        id = playerID;
        oppID = (id + 1) % so.getNoPlayers();
        this.nEvals = nEvals;

        //Get the actions for all players in a static array.
        NUM_ACTIONS = new int[no_players];
        actions = new Types.ACTIONS[no_players][];
        for (int i = 0; i < no_players; i++) {

            ArrayList<Types.ACTIONS> act = so.getAvailableActions(i);

            actions[i] = new Types.ACTIONS[act.size()];
            for (int j = 0; j < act.size(); ++j) {
                actions[i][j] = act.get(j);
            }
            NUM_ACTIONS[i] = actions[i].length;
        }
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     *
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        //Set the state observation object as the new root of the tree.
        // we'll set up a game adapter and run the algorithm independently each
        // time at least to being with
        GameActionSpaceAdapter game = new GameActionSpaceAdapter(stateObs, sequenceLength);
        if (solution != null) {
            solution = SearchSpaceUtil.shiftLeftAndRandomAppend(solution, game);
            evoAlg.setInitialSeed(solution);
        }

        solution = evoAlg.runTrial(game, timer);

        // System.out.println(Arrays.toString(solution) + "\t " + game.evaluate(solution));

        int action = solution[0];
        // already return the first element, so now set it to 1 ...
        if (!useShiftBuffer) solution = null;

        // index = 1;

        //... and return it.
        // return actions[action];

        return actions[id][action];
    }

    @Override
    public Types.ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedCpuTimer) {
//        System.out.println("start act(...): " + elapsedCpuTimer.remainingTimeMillis());

        //Set the state observation object as the new root of the tree.

        // we'll set up a game adapter and run the algorithm independently each
        // time at least to being with

        int action1;
        GameActionSpaceAdapterMulti game = new GameActionSpaceAdapterMulti(stateObs, sequenceLength, id, oppID);

        if (solution != null) {
            solution = SearchSpaceUtil.shiftLeftAndRandomAppend(solution, game);
            evoAlg.setInitialSeed(solution);
        }

        solution = evoAlg.runTrial(game, elapsedCpuTimer);

        if (solution == null) {
            System.out.println("null solution");
        }
//        System.out.println(Arrays.toString(solution) + "\t " + game.pairEvaluate(solution));

        action1 = solution[0];
        // already return the first element, so now set it to 1 ...

        if (!useShiftBuffer) solution = null;
        return actions[id][action1];
    }


}
