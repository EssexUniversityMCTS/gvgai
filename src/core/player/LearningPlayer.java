package core.player;

import core.competition.CompetitionParameters;
import core.game.SerializableStateObservation;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tracks.singleLearning.utils.Comm;
import tracks.singleLearning.utils.PipeComm;
import tracks.singleLearning.utils.SocketComm;

import java.io.IOException;


/**
 * Created by Daniel on 07.03.2017.
 */
public class LearningPlayer extends Player {

    /**
     * Server communication channel
     */
    private Comm comm;

    /**
     * Learning Player constructor.
     * Creates a new server side communication channel for every player.
     */
    public LearningPlayer(Process proc, String port) {
        if (CompetitionParameters.USE_SOCKETS) {
            //Sockets:
            this.comm = new SocketComm(port);
        }
        //Else: pipes
        else {
            this.comm = new PipeComm(proc);
        }

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
     * @param so           Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    @Override
    public Types.ACTIONS act(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        // Sending messages.
        try {
            SerializableStateObservation sso;
            switch (comm.getLastSsoType()) {
                case JSON:
                    so.currentGameState = Types.GAMESTATES.ACT_STATE;
                    sso = new SerializableStateObservation(so);
                    comm.commSend(sso.serialize(null));
                    break;
                case IMAGE:
                    // Set the game state to the appropriate state and the millisecond counter, then send the serialized observation.
                    so.currentGameState = Types.GAMESTATES.ACT_STATE;
                    sso = new SerializableStateObservation(so, false);

                    // Used for debugging
//                    System.out.println(sso.toString());
                    comm.commSend(sso.serialize(null));
                    break;
                case BOTH:
                    // Set the game state to the appropriate state and the millisecond counter, then send the serialized observation.
                    so.currentGameState = Types.GAMESTATES.ACT_STATE;
                    sso = new SerializableStateObservation(so, true);
                    comm.commSend(sso.serialize(null));
                    break;
                default:
                    System.err.println("LearningPlayer: act(): This should never happen.");
                    break;
            }

            // Receive the response and set ACTION_NIL as default action
            String response = comm.commRecv();
            if (response == null || response == "")
                response = Types.ACTIONS.ACTION_NIL.toString();

            //System.out.println("Received ACTION: " + response + "; ACT (Server) Response time: "
            //        + elapsedTimer.elapsedMillis() + " ms.");

            if (response.equals("END_OVERSPENT")) {
                so.currentGameState = Types.GAMESTATES.ABORT_STATE;
                return Types.ACTIONS.ACTION_ESCAPE;
            }

            // Set the game to the kill state if the response is that of ABORT
            if (response.equals("ABORT")) {
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
     * @param so           State observation of the current game in its initial state
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

            comm.commSend(sso.serialize(null));
            String initResponse = comm.commRecv();

            if (initResponse.equals("INIT_FAILED"))
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
     * or the agent will be DISQUALIFIED
     *
     * @param stateObs the game state at the end of the game
     * @returns Level to be plated.
     */
    public int result(StateObservation stateObs) {
        int result = this.comm.finishGame(stateObs);
//        System.out.println("Client replied: " + result);
        return result;
    }

    /**
     * Starts the communication between the server and the client.
     *
     * @return true or false, depending on whether the initialization has been successful
     */
    public boolean startPlayerCommunication() {

        //Initialize the controller.
        if (!this.comm.startComm())
            return false;

        return true;
    }

    /**
     * Tells the client that this is over. Se finito.
     */
    public boolean finishPlayerCommunication() {

        //Initialize the controller.
        if (!this.comm.endComm())
            return false;

        return true;
    }

    public Types.LEARNING_SSO_TYPE getLearningSsoType() {
        return comm.getLastSsoType();
    }
}