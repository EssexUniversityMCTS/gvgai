package tracks.singleLearning.utils;

/**
 * Created by Daniel on 05.04.2017.
 */

import java.io.*;

public class PipeComm extends Comm {

    /**
     * Reader of the player. Will read actions from the client.
     */
    public static BufferedReader input;

    /**
     * Writer of the player. Used to pass the client the state view information.
     */
    public static BufferedWriter output;

    /**
     * Client process
     */
    private Process client;


    /**
     * Public constructor of the player.
     * @param client process that runs the agent.
     */
    public PipeComm(Process client) {
        super();
        this.client = client;
        initBuffers();
    }

    /**
     * Creates the buffers for pipe communication.
     */
    @Override
    public void initBuffers() {
        input = new BufferedReader(new InputStreamReader(client.getInputStream()));
        output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
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
        //System.out.println("Received in server: " + ret);
        if(ret != null && ret.trim().length() > 0)
        {
            String messageParts[] = ret.split(TOKEN_SEP);
            if(messageParts.length < 2) {
                return null;
            }

            int receivedID = Integer.parseInt(messageParts[0]);
            String msg = messageParts[1];

            if(receivedID == (messageId-1)) {
                return msg.trim();
            } else if (receivedID < (messageId-1)) {
                //Previous message, ignore and keep waiting.
                return commRecv();
            }else{
                //A message from the future? Ignore and return null;
                return null;
            }
        }
        System.err.println("I will return nill");
        return null;
    }

}


