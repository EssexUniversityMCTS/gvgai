package tracks.singleLearning;

/**
 * Created by Daniel on 05.04.2017.
 */

import core.game.SerializableStateObservation;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.io.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ServerComm {

    private static final Logger logger = Logger.getLogger(core.player.LearningPlayer.class.getName());

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
            fh = new FileHandler("logs/serverCommDebug.txt");
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

    public ServerComm(Process client) {
        this.client = client;
        initBuffers();
    }

    /**
     * Creates the buffers for pipe communication.
     */
    public void initBuffers() {
        input = new BufferedReader(new InputStreamReader(client.getInputStream()));
        output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
    }

    public void finishGame(StateObservation so, ElapsedCpuTimer elapsedTimer) throws IOException {
        initBuffers();

        // Set the game state to the appropriate state and the millisecond counter, then send the serialized observation.
        so.currentGameState = Types.GAMESTATES.END_STATE;

        SerializableStateObservation sso = new SerializableStateObservation(so);

        sso.elapsedTimer = elapsedTimer.remainingTimeMillis();
        commSend(sso.serialize(null));

        String response = commRecv();

        logger.fine("Received: " + response);

        if(so.isGameOver()){
            // TODO: 04/04/2017 Daniel: start new game
        }

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
     * @return the response got from the client, or null if no response was received after due time.
     */
    public String commRecv() throws IOException {
        String ret = "";
        //if (input.ready()) {
            //skip the first line
            //input.readLine();

            ret = input.readLine();
            while (ret != null && ret.trim().length() > 0) {
                //System.out.println("TIME OK");
                return ret.trim();
            }
        //}
        //if(elapsedTimer.remainingTimeMillis() <= 0)
        //    System.out.println("TIME OUT (" + idStr + "): " + elapsedTimer.elapsedMillis());

        return null;
    }

    /**
     * Picks an action. This function is called at the beginning of the game for
     * initialization.
     *
     * @param elapsedTimer Timer when the initialization is due to finish.
     */
    public boolean init(ElapsedCpuTimer elapsedTimer) {
        try {
            String response = "";

            commSend("INIT_START");

            // Ignore the first response
            response = commRecv();

            response = commRecv();

            logger.fine("Received: " + response);
            if ("INIT_DONE".equals(response)) {
                System.out.println("\nInit done");
                return true;
            } else {
                System.out.println("init failed");
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


