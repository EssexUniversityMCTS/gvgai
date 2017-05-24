package tracks.singleLearning;

/**
 * Created by Daniel on 05.04.2017.
 */

import core.game.SerializableStateObservation;
import core.game.StateObservation;
import ontology.Types;

import java.io.*;
import java.util.Random;
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
     * Special character to separate message ID from actual message
     */
    private String TOKEN_SEP = "#";

    /**
     * Message ID
     */
    private long messageId;

    /**
     * Client process
     */
    private Process client;

    /**
     * Static block that handles the debug log creation.
     */
    static {
        FileHandler fh;
        try {
            // This block configure the logger with handler and formatter
            fh = new FileHandler("logs/serverCommDebug.txt");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Public constructor of the player.
     * @param client process that runs the agent.
     */
    public ServerComm(Process client) {
        this.client = client;
        this.messageId = 0;
        initBuffers();
    }

    /**
     * Creates the buffers for pipe communication.
     */
    public void initBuffers() {
        input = new BufferedReader(new InputStreamReader(client.getInputStream()));
        output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
    }

    /***
     * This method is used to set the game state to either "ABORT_STATE" or "END_STATE"
     * depending on the termination of the game. Each game calls this method upon teardown
     * of the player object.
     *
     * END_STATE: Game ends normally
     * ABORT_STATE: Game is violently ended by player using "ABORT" message or ACTION_ESCAPE key
     *
     * @param so State observation of the game in progress to be used for message sending.
     * @return response by the client (level to be played)
     */
    public int finishGame(StateObservation so){

        try
        {
            // Set the game state to the appropriate state and the millisecond counter, then send the serialized observation.
            if(so.getAvatarLastAction() == Types.ACTIONS.ACTION_ESCAPE)
                so.currentGameState = Types.GAMESTATES.ABORT_STATE;
            else
                so.currentGameState = Types.GAMESTATES.END_STATE;

            SerializableStateObservation sso = new SerializableStateObservation(so);

            commSend(sso.serialize(null));

            String response = commRecv();

            if(response == null || response.equals("END_OVERSPENT"))
            {
                System.err.println("ServerComm:END_OVERSPENT");
                return Types.LEARNING_RESULT_DISQ;
            }

            if (response.matches("^[0-" + Types.NUM_TRAINING_LEVELS + "]$")) {
                return Integer.parseInt(response);
            }else if (response.equals("END_TRAINING") || response.equals("END_VALIDATION")) {
                return Types.LEARNING_FINISH_ROUND;
            } else {
                return new Random().nextInt(Types.NUM_TRAINING_LEVELS);
            }


        }catch(Exception e){
            System.out.println("Error sending results to the client:");
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Sends a message through the pipe.
     *
     * @param msg message to send.
     */
    public void commSend(String msg) throws IOException {
        String message = messageId + TOKEN_SEP + msg + lineSep;
        output.write(message);
        output.flush();
        messageId++;
    }

    /**
     * Receives a message from the client.
     *
     * @return the response got from the client, or null if no response was received after due time.
     */
    public String commRecv() throws IOException {
        String ret = input.readLine();
        //System.out.println(ret);

        if(ret != null && ret.trim().length() > 0)
        {
            String messageParts[] = ret.split(TOKEN_SEP);
            if(messageParts.length < 2)
                return null;

            int receivedID = Integer.parseInt(messageParts[0]);
            String msg = messageParts[1];

            if(receivedID == (messageId-1))
            {
                return msg.trim();
            }else if (receivedID < (messageId-1))
            {
                //Previous message, ignore and keep waiting.
                return commRecv();
            }else{
                //A message from the future? Ignore and return null;
                return null;
            }
        }

        return null;
    }

    /**
     * This function is called at the beginning of the game for
     * initialization.
     * Will give up if no "START_DONE" received after having received 11 responses
     */
    public boolean start() {

        try {
            commSend("START");
            String response;

            response = commRecv();
            if (response==null) {
                return start();
            } else if(response.equalsIgnoreCase("START_FAILED"))
            {
                //Disqualification because of timeout.
                System.out.println("START_FAILED");
                return false;
            } else if (response.equalsIgnoreCase("START_DONE")) {
                logger.fine("Received: " + response);
                return true;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Disqualification because of exception, communication fail.
        System.out.println("Communication failed for unknown reason, could not play any games :-( ");
        return false;

    }

}


