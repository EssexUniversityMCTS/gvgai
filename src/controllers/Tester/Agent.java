package controllers.Tester;

import java.util.ArrayList;
import java.util.Random;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

    public static int NUM_ACTIONS;
    public static int ROLLOUT_DEPTH = 10;
    public static double K = Math.sqrt(2);
    public static Types.ACTIONS[] actions;

    /**
     * Random generator for the agent.
     */
    private SingleMCTSPlayer mctsPlayer;


    /**
     * Predicted state in the previous game step.
     * null at the first call.
     */
    StateObservation predictedState;

    /**
     * Last action replied by the act() method.
     */
    Types.ACTIONS lastActionReplied;

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        predictedState = null;
        //Get the actions in a static array.
        ArrayList<Types.ACTIONS> act = so.getAvailableActions();
        actions = new Types.ACTIONS[act.size()];
        for(int i = 0; i < actions.length; ++i)
        {
            actions[i] = act.get(i);
        }
        NUM_ACTIONS = actions.length;

        //Create the player.
        mctsPlayer = new SingleMCTSPlayer(new Random());
    }

    public static boolean predicting;
    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        if(predictedState != null)
        {
            compareStates(predictedState, stateObs);
        }

        //Set the state observation object as the new root of the tree.
        mctsPlayer.init(stateObs);

        //Determine the action using MCTS...
        int action = mctsPlayer.run(elapsedTimer);
        Types.ACTIONS actionT = actions[action];

        //Prepare next state for next cycle's verification.
        predictedState = stateObs.copy();
        predictedState.setNewSeed(stateObs.getGameTick() * 100); //Set a new seed for the random generator.
        predicting = true;
        predictedState.advance(actionT);
        predicting = false;
        lastActionReplied = actionT;

        //... and return it.
        return actionT;
    }

    /**
     * Compares two states to check if they are equal.
     * @param predicted predicted state
     * @param real real state.
     */
    private void compareStates(StateObservation predicted, StateObservation real)
    {

        //NOTE: As it is now, if used to test if the next state prediction and the real next state
        // are the same, it ONLY WORKS for DETERMINISTIC transitions.
        // In order to test it with STOCHASTIC games, uncomment the lines that assign a new random
        // generator in Game.tick(): (if(sp == avatar) random = new Random(this.gameTick * 100);)
        Types.ACTIONS realLastAction = real.getAvatarLastAction();
        if(lastActionReplied == realLastAction)
        {
            if (!predicted.equiv(real)) {
                throw new RuntimeException("Prediction error!");
            }
        }

        // if (controllers.Tester.Agent.predicting || !(this instanceof ForwardModel)) { }

    }

}
