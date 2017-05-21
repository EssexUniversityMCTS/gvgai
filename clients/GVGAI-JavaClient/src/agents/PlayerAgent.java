package agents;

/**
 * Created by Daniel on 21.05.2017.
 */

import ontology.Types;
import serialization.SerializableStateObservation;

import java.util.Random;

/**
 * This class has been built with a simple design in mind.
 * It is to be used to store player agent information,
 * to be later used by the client to send and receive information
 * to and from the server.
 */
public class PlayerAgent {

    // Empty constructor
    public PlayerAgent(){}

    /**
     * Public method to be called at the start of the communication (not game!).
     * Perform one-time setup here.
     */
    public void START(){

    }

    /**
     * Public method to be called at the start of every level of a game.
     * Perform any level-entry initialization here.
     */
    public void INIT(){

    }

    /**
     * Method used to determine the next move to be performed by the agent.
     * This method can be used to identify the current state of the game and all
     * relevant details, then to choose the desired course of action.
     *
     * @param sso Observation of the current state of the game to be used in deciding
     *            the next action to be taken by the agent.
     * @return The action to be performed by the agent.
     */
    public Types.ACTIONS ACT(SerializableStateObservation sso){

        Random r = new Random();
        Types.ACTIONS rndAction;
        if (r.nextFloat() < 0.5)
            rndAction = Types.ACTIONS.ACTION_RIGHT;
        else
            rndAction = Types.ACTIONS.ACTION_LEFT;

        return rndAction;
    }

    /**
     * Method used to determine the next level of the current game to be played by the agent.
     * The level is bound in the range of [0,4). If the input is any different, then the level
     * chosen will be ignored, and the game will play a random one instead.
     *
     * @param sso Observation of the current state of the game to be used for informational
     *            reasons, if required by the player.
     * @return The next level of the current game to be played.
     */
    public int CHOOSE(SerializableStateObservation sso){
        Random r = new Random();
        Integer level = r.nextInt(3);

        return level;
    }

    /**
     * Method used to perform actions in case of an abort. This is the last thing called before the
     * level ends.
     * Use this for actions such as teardown.
     *
     * @param sso The current state observation of the game.
     */
    public void ABORT(SerializableStateObservation sso){

    }

    /**
     * Method used to perform actions in case of the game ending. This is the last thing called before
     * the game ends.
     * Use this for actions such as teardown.
     *
     * @param sso The current state observation of the game.
     */
    public void END(SerializableStateObservation sso){

    }
}
