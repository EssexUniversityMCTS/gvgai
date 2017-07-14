package utils;

import serialization.SerializableStateObservation;
import serialization.Types;

/**
 * Created by dperez on 24/05/2017.
 */
public abstract class AbstractPlayer {
    protected Types.LEARNING_SSO_TYPE lastSsoType = Types.LEARNING_SSO_TYPE.JSON;

    /**
     * Public method to be called at the start of every level of a game.
     * Perform any level-entry initialization here.
     * @param sso Phase Observation of the current game.
     * @param elapsedTimer Timer (1s)
     */
    public abstract void init(SerializableStateObservation sso, ElapsedCpuTimer elapsedTimer);


    /**
     * Method used to determine the next move to be performed by the agent.
     * This method can be used to identify the current state of the game and all
     * relevant details, then to choose the desired course of action.
     *
     * @param sso Observation of the current state of the game to be used in deciding
     *            the next action to be taken by the agent.
     * @param elapsedTimer Timer (40ms)
     * @return The action to be performed by the agent.
     */
    public abstract Types.ACTIONS act(SerializableStateObservation sso, ElapsedCpuTimer elapsedTimer);


    /**
     * Method used to perform actions in case of a game end.
     * This is the last thing called when a level is played (the game is already in a terminal state).
     * Use this for actions such as teardown or process data.
     *
     * @param sso The current state observation of the game.
     * @param elapsedTimer Timer (up to CompetitionParameters.TOTAL_LEARNING_TIME
     * or CompetitionParameters.EXTRA_LEARNING_TIME if current global time is beyond TOTAL_LEARNING_TIME)
     * @return The next level of the current game to be played.
     * The level is bound in the range of [0,2]. If the input is any different, then the level
     * chosen will be ignored, and the game will play a sampleRandom one instead.
     */
    public abstract int result(SerializableStateObservation sso, ElapsedCpuTimer elapsedTimer);
}
