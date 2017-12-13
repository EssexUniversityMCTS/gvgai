package tracks.singleLearning.utils;

/**
 * Created by Daniel on 05.04.2017.
 */

import core.competition.CompetitionParameters;
import ontology.Types.LEARNING_SSO_TYPE;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class SocketComm extends Comm {


    public int port = CompetitionParameters.SOCKET_PORT; //default
    private Socket socket;
    private Scanner in;
    private PrintStream out;
    private boolean end;
    private static int THRESHOLD = 60000;

    /**
     * Public constructor of the player.
     */
    public SocketComm(String portStr) {
        super();
        end = false;
        port = Integer.parseInt(portStr);
        initBuffers();
    }

    /**
     * Creates the buffers for pipe communication.
     */
    @Override
    public void initBuffers() {
        try{
            //Accepting the socket connection.
            while (socket == null) {
                ServerSocket serverSocket = new ServerSocket(port);
                socket = serverSocket.accept();
            }


            //Initialize input and output through socket.
            in = new Scanner(socket.getInputStream());
            out = new PrintStream(socket.getOutputStream());

        } catch(java.net.BindException e)
        {
            System.out.println(e.toString());
            e.printStackTrace();
        }catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    public void start()
    {
        try {
            initBuffers();

            while(!end)
            {

            }
            //out.format("Sending back: " + received);

            // may want to close this client side instead
            socket.close();
            System.out.println("Closed socket");
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a message through the pipe.
     *
     * @param msg message to send.
     */
    public void commSend(String msg) throws IOException {
        String message = messageId + TOKEN_SEP + msg + lineSep;
        out.format(message);
        out.flush();
        messageId++;
    }

    /**
     * Receives a message from the client.
     *
     * @return the response got from the client, or null if no response was received after due time.
     */
//    public String commRecv() {
//        String ret = null;
//        if (in.hasNextLine()) {
//            ret = in.nextLine();
//            //System.out.println("Received in server: " + ret);
//            if (ret != null && ret.trim().length() > 0) {
//                String messageParts[] = ret.split(TOKEN_SEP);
//                if (messageParts.length < 2) {
//                    System.err.println("SocketComm: commRecv(): received message incomplete.");
//                    return null;
//                }
//                int receivedID = Integer.parseInt(messageParts[0]);
//                String msg = messageParts[1];
//
//                if (messageParts.length >= 3) {
//                    String ssoType = messageParts[2];
//                    switch (ssoType) {
//                        case "JSON":
//                            this.lastSsoType = LEARNING_SSO_TYPE.JSON;
//                            break;
//                        case "IMAGE":
//                            this.lastSsoType = LEARNING_SSO_TYPE.IMAGE;
//                            break;
//                        case "BOTH":
//                            this.lastSsoType = LEARNING_SSO_TYPE.BOTH;
//                            break;
//                        default:
//                            System.err.println("SocketComm: commRecv(): This should never happen.");
//                            break;
//                    }
//                }
//
//                if (receivedID == (messageId - 1)) {
//                    return msg.trim();
//                } else if (receivedID < (messageId - 1)) {
//                    //Previous message, ignore and keep waiting.
//                    return commRecv();
//                } else {
//                    //A message from the future? Ignore and return null;
//                    System.err.println("SocketComm: commRecv: Communication Error! A message from the future!");
//                    return null;
//                }
//            } else {
//                return commRecv();
//            }
//        } else {
//            return commRecv();
//        }
//    }

    /**
     * Receives a message from the client.
     *
     * @return the response got from the client, or null if no response was received after due time.
     */
    public String commRecv() {
        float timeout = 0;
        String response = null;
        while (timeout < THRESHOLD && response == null)
        {
            response = processCommRecv();
        }
        if (response == null){
            System.err.println("SocketComm: commRecv: No message received. Time threshold exceeded.");
        }
        return response;
    }

    private String processCommRecv(){
        String ret = null;
        if (in.hasNextLine()) {
            ret = in.nextLine();
            //System.out.println("Received in server: " + ret);
            if (ret != null && ret.trim().length() > 0) {
                String messageParts[] = ret.split(TOKEN_SEP);
                if (messageParts.length < 2) {
                    System.err.println("SocketComm: commRecv(): received message incomplete.");
                    return null;
                }
                int receivedID = Integer.parseInt(messageParts[0]);
                String msg = messageParts[1];

                if (messageParts.length >= 3) {
                    String ssoType = messageParts[2];
                    switch (ssoType) {
                        case "JSON":
                            this.lastSsoType = LEARNING_SSO_TYPE.JSON;
                            break;
                        case "IMAGE":
                            this.lastSsoType = LEARNING_SSO_TYPE.IMAGE;
                            break;
                        case "BOTH":
                            this.lastSsoType = LEARNING_SSO_TYPE.BOTH;
                            break;
                        default:
                            System.err.println("SocketComm: commRecv(): This should never happen.");
                            break;
                    }
                }

                if (receivedID == (messageId - 1)) {
                    return msg.trim();
                } else if (receivedID < (messageId - 1)) {
                    //Previous message, ignore and keep waiting.
                    return commRecv();
                } else {
                    //A message from the future? Ignore and return null;
                    System.err.println("SocketComm: commRecv: Communication Error! A message from the future!");
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

}