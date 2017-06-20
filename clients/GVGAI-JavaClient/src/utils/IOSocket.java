package utils;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by dperez on 23/05/2017.
 */
public class IOSocket extends IO {


    private Socket socket;
    private Scanner in;
    private PrintStream out;

    private String hostname = "localhost";
    private int port;

    public IOSocket(int port)
    {
        super();
        this.port = port;
    }

    /**
     * Creates the buffers for pipe communication.
     */
    @Override
    public void initBuffers() {

        boolean connected = false;
        try {

            while(!connected)
            {
                try{
                    socket = new Socket(hostname, port);
                    connected = true;
                    System.out.println("Client connected to server [OK]");
                }catch (ConnectException e) {
                    //System.out.println(e);
                }
            }

            out = new PrintStream(socket.getOutputStream());
            in = new Scanner(socket.getInputStream());

        } catch (Exception e) {
            System.out.println("Exception creating the client process: " + e);
            e.printStackTrace();
        }

    }


    /**
     * Writes a line to the server, adding a line separator at the end.
     * @param messageId the server is expecting.
     * @param line to write
     * @param log if true, write to file as well.
     */
    @Override
    public void writeToServer(long messageId, String line, boolean log)
    {
        String msg = messageId + ClientComm.TOKEN_SEP + line;
        this.writeToServer(msg);
        if(log) this.writeToFile(msg);
    }

    @Override
    public String readLine() throws IOException{
        return in.nextLine();
    }

    /**
     * Writes a line to the server, adding a line separator at the end.
     * @param line to write
     */
    @Override
    protected void writeToServer(String line)
    {
        try {
            out.print(line + lineSep);
            out.flush();
        }catch(Exception e)
        {
            System.out.println("Error trying to write " + line + " to the server.");
            e.printStackTrace();
        }
    }




}
