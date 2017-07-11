package tracks.singleLearning.utils;

import core.competition.CompetitionParameters;
import core.game.SerializableStateObservation;
import core.game.StateObservation;
import ontology.Types;

import java.io.IOException;
import java.util.Random;

/**
 * Created by dperez on 01/06/2017.
 */
public abstract class Comm extends Thread {


    /**
     * Variable to store the message type
     */
    protected Types.LEARNING_SSO_TYPE lastSsoType = Types.LEARNING_SSO_TYPE.JSON; // Type of message chosen by player (JSON/Image)

    /**
     * Line separator for messages.
     */
    protected String lineSep = System.getProperty("line.separator");

    /**
     * Special character to separate message ID from actual message
     */
    protected String TOKEN_SEP = "#";

    /**
     * Message ID
     */
    protected long messageId;

    /**
     * Default constructor
     */
    public Comm() {
        this.messageId = 0;
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

            if(response == null || response.equalsIgnoreCase("END_OVERSPENT"))
            {
//                System.err.println("PipeComm:END_OVERSPENT");
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
     * This function is called at the beginning of the game for
     * initialization.
     * Will give up if no "START_DONE" received after having received 11 responses
     */
    public boolean startComm() {

        try {

            //First thing we recieve: ACK from client about connection. We don't care about that, skip.
            if(!CompetitionParameters.USE_SOCKETS)
                commRecv();

            commSend("START");
            String response;

            response = commRecv();
            if (response==null) {
                //Odd. We don't like this, things went wrong.
                return false;
            } else if(response.equalsIgnoreCase("START_FAILED"))
            {
                //Disqualification because of timeout.
                System.out.println("START_FAILED");
                return false;
            } else if (response.equalsIgnoreCase("START_DONE")) {
                return true;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Disqualification because of exception, communication fail.
        System.out.println("Communication failed for unknown reason, could not play any games :-( ");
        return false;

    }


    /**
     * This function is called at the end of the whole process. Closes the communication.
     * Will give up if no "START_DONE" received after having received 11 responses
     */
    public boolean endComm() {

        try {

            //Send the finish message and don't wait for any response.
            commSend("FINISH");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    /**
     * Creates the buffers for communication.
     */
    public abstract void initBuffers();

    /**
     * Receives a message from the client.
     *
     * @return the response got from the client, or null if no response was received after due time.
     */
    public abstract String commRecv() throws IOException;

    /**
     * Sends a message through the pipe.
     *
     * @param msg message to send.
     */
    public abstract void commSend(String msg) throws IOException;

    public Types.LEARNING_SSO_TYPE getLastSsoType() {
        return this.lastSsoType;
    }
}
