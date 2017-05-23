package core.player;

import core.competition.CompetitionParameters;
import tracks.singleLearning.ServerComm;
import core.game.SerializableStateObservation;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.io.*;
import java.util.logging.Logger;

/**
 * Created by Daniel on 07.03.2017.
 */
public class LearningPlayer extends Player {

    private static final Logger logger = Logger.getLogger(LearningPlayer.class.getName());

    /**
     * Last action executed by this agent.
     */
    private Types.ACTIONS lastAction = null;

    /**
     * Line separator for messages.
     */
    private String lineSep = System.getProperty("line.separator");

    /**
     * Client process
     */
    private Process client;

    /**
     * Server communication channel
     */
    private ServerComm serverComm;

    /**
     * Learning Player constructor.
     * Creates a new server side communication channel for every player.
     */
    public LearningPlayer(Process proc){
        this.serverComm = new ServerComm(proc);
    }

    // Getter for the server comm object
    public ServerComm getServerComm() {
        return serverComm;
    }

    public Types.ACTIONS act(SerializableStateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        return null;
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player. The action returned must be contained in the
     * actions accessible from stateObs.getAvailableActions(), or action NIL
     * will be applied.
     *
     * @param so     Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    @Override
    public Types.ACTIONS act(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        //Sending messages.
        try {
            // Set the game state to the appropriate state and the millisecond counter, then send the serialized observation.
            so.currentGameState = Types.GAMESTATES.ACT_STATE;
            SerializableStateObservation sso = new SerializableStateObservation(so);

            serverComm.commSend(sso.serialize(null));

            // Receive the response and set ACTION_NIL as default action
            String response = serverComm.commRecv();
            if (response == null)
                response = Types.ACTIONS.ACTION_NIL.toString();

            // Log and debug the received action
            logger.fine("Received ACTION: " + response + "; ACT (Server) Response time: "
                    + elapsedTimer.elapsedMillis() + " ms.");

            System.out.println("Received ACTION: " + response + "; ACT (Server) Response time: "
                    + elapsedTimer.elapsedMillis() + " ms.");

            // Set the game to the kill state if the response is that of ABORT
            if (response.equals("ABORT")){
                so.currentGameState = Types.GAMESTATES.ABORT_STATE;
                return Types.ACTIONS.ACTION_ESCAPE;
            }

            Types.ACTIONS action = Types.ACTIONS.fromString(response);
            return action;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /***
     *
     * @param so State observation of the current game in its initial state
     * @param isValidation true if the level to play is a validation one.
     * @return true if Init worked.
     */
    public boolean init(StateObservation so, boolean isValidation) {
        //Sending messages.
        try {
            // Set the game state to the appropriate state and the millisecond counter, then send the serialized observation.
            so.currentGameState = Types.GAMESTATES.INIT_STATE;
            SerializableStateObservation sso = new SerializableStateObservation(so);
            sso.isValidation = isValidation;

            serverComm.commSend(sso.serialize(null));
            String initResponse = serverComm.commRecv();

            if(initResponse.equals("INIT_FAILED"))
                return false;
            return true;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Types.ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer) {
        return null;
    }

    /**
     * Function called when the game is over. This method must finish before CompetitionParameters.TEAR_DOWN_TIME,
     *  or the agent will be DISQUALIFIED
     * @param stateObs the game state at the end of the game
     * @returns Level to be plated.
     *
     */
    public int result(StateObservation stateObs)
    {
        return this.serverComm.finishGame(stateObs);
    }

    /**
     * Starts the communication between the server and the client.
     * @return true or false, depending on whether the initialization has been successful
     */
    public boolean startPlayerCommunication() {

        //Initialize the controller.
        if (!this.serverComm.start())
            return false;

        return true;
    }
}

