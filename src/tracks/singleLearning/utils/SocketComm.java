package tracks.singleLearning.utils;

/**
 * Created by Daniel on 05.04.2017.
 */

import core.competition.CompetitionParameters;

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
    BufferedReader br;

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
    public String commRecv() throws IOException {
        String ret = in.nextLine();

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
        System.err.println("I will return null");
        return null;
    }

}


