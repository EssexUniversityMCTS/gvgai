package core.player;

import com.google.gson.Gson;
import core.VGDLRegistry;
import core.game.SerializableStateObservation;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import core.game.StateView;
import ontology.Types;
import tools.ElapsedCpuTimer;

import javax.sql.rowset.serial.SerialArray;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by Daniel on 07.03.2017.
 */
public class LearningPlayer extends Player {

    private static final Logger logger = Logger.getLogger(LearningPlayer.class.getName());

    /**
     * Last action executed by this agent.
     */
    private Types.ACTIONS lasAction = null;

    /**
     * Reader of the player. Will read actions from the client.
     */
    public static BufferedReader input;

    /**
     * Writer of the player. Used to pass the client the state view information.
     */
    public static BufferedWriter output;

    /**
     * Line separator for messages.
     */
    private String lineSep = System.getProperty("line.separator");

    /**
     * Client process
     */
    private Process client;


    /**
     * Public constructor of the player.
     * @param client process that runs the agent.
     */


    static {

        FileHandler fh;

        try {

            // This block configure the logger with handler and formatter
            fh = new FileHandler("./debug.txt");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            // the following statement is used to log any messages


        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public LearningPlayer(Process client) {
        isLearner = true;


        this.client = client;
        initBuffers();


    }

    /**
     * Creates the buffers for pipe communication.
     */
    private void initBuffers() {

        input = new BufferedReader(new InputStreamReader(client.getInputStream()));
        output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));


    }

    /**
     * Picks an action. This function is called at the beginning of the game for
     * initialization.
     *
     * @param sso    View of the current state.
     * @param elapsedTimer Timer when the initialization is due to finish.
     */
    public void init(SerializableStateObservation sso, ElapsedCpuTimer elapsedTimer) {
        Gson serializer = new Gson();

        try {
            // Set the game state to the appropriate state and the millisecond counter, then send the serialized observation.
            sso.gameState = SerializableStateObservation.State.INIT_STATE;
            sso.elapsedTimer = elapsedTimer.remainingTimeMillis();
            commSend(sso.serialize(null));

            String response = commRecv(elapsedTimer, "INIT");
            logger.fine("Received: " + response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player. The action returned must be contained in the
     * actions accessible from stateObs.getAvailableActions(), or action NIL
     * will be applied.
     *
     * @param sso     Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(SerializableStateObservation sso, ElapsedCpuTimer elapsedTimer) {
        initBuffers();

        //Sending messages.
        try {
            // Set the game state to the appropriate state and the millisecond counter, then send the serialized observation.
            sso.gameState = SerializableStateObservation.State.ACT_STATE;
            sso.elapsedTimer = elapsedTimer.remainingTimeMillis();
            commSend(sso.serialize(null));
            // TODO: 27/03/2017 Daniel: what if received ABORT ?
            String response = commRecv(elapsedTimer, "ACT");
            logger.fine("Received ACTION: " + response + "; ACT Response time: "
                    + elapsedTimer.elapsedMillis() + " ms.");
            Types.ACTIONS action = Types.ACTIONS.fromString(response);
            return action;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        return null;
    }

    @Override
    public Types.ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer) {
        return null;
    }

    @Override
    public Types.ACTIONS act(String stateObsStr, ElapsedCpuTimer elapsedTimer) {
        return null;
    }


    // TODO: 27/03/2017 Daniel: check the following two methods, why client side ?
    public void finishGame(SerializableStateObservation sso, ElapsedCpuTimer elapsedTimer) throws IOException {
        initBuffers();

        // TODO: 27/03/2017 Daniel: is the game stopped correctly ?
        // Set the game state to the appropriate state and the millisecond counter, then send the serialized observation.
        sso.gameState = SerializableStateObservation.State.END_STATE;
        sso.elapsedTimer = elapsedTimer.remainingTimeMillis();
        commSend(sso.serialize(null));

        String response = commRecv(elapsedTimer, "GAME_DONE");
        // TODO: 27/03/2017 Daniel: not finished yet. What should happen?
        logger.fine("Received: " + response);
    }

    /**
     * Sends a message through the pipe.
     *
     * @param msg message to send.
     */
    public void commSend(String msg) throws IOException {

        output.write(msg + lineSep);
        output.flush();

    }

    /**
     * Waits for a response during T milliseconds.
     *
     * @param elapsedTimer Timer when the initialization is due to finish.
     * @param idStr        String identifier of the phase the communication is in.
     * @return the response got from the client, or null if no response was received after due time.
     */
    // TODO: 27/03/2017 Daniel: check the whole method
    public static String commRecv(ElapsedCpuTimer elapsedTimer, String idStr) throws IOException {
        String ret = null;


        while (elapsedTimer.remainingTimeMillis() > 0) {
            if (input.ready()) {

                ret = input.readLine();
                if (ret != null && ret.trim().length() > 0) {
                    //System.out.println("TIME OK");
                    return ret.trim();
                }
            }
        }


        //if(elapsedTimer.remainingTimeMillis() <= 0)
        //    System.out.println("TIME OUT (" + idStr + "): " + elapsedTimer.elapsedMillis());

        return null;
    }

//    public final void close() {
//        try {
//            input.close();
//            output.close();
//
//        } catch (IOException e) {
//            logger.severe("IO Exception closing the buffers: " + e.getStackTrace());
//
//        } catch (Exception e) {
//            logger.severe("Exception closing the buffers: " + e.getStackTrace());
//
//        }
//    }

}

