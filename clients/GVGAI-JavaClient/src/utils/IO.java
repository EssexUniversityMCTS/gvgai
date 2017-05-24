package utils;

import java.io.*;

/**
 * Created by dperez on 23/05/2017.
 */
public class IO
{

    /**
     * Reader of the player. Will read the game state from the client.
     */
    public static BufferedReader input;

    /**
     * Writer of the player. Used to pass the action of the player to the server.
     */
    public static BufferedWriter output;

    /**
     * Writer of the player. Used to pass the action of the player to the server.
     */
    public static PrintWriter fileOutput;

    /**
     * Line separator for messages.
     */
    private String lineSep = System.getProperty("line.separator");

    /**
     * Creates the buffers for pipe communication.
     */
    public void initBuffers() {
        try {
            fileOutput = new PrintWriter(new File("logs/clientLog.txt"), "utf-8");

            input = new BufferedReader(new InputStreamReader(System.in));
            output = new BufferedWriter(new OutputStreamWriter(System.out));



        } catch (Exception e) {
            System.out.println("Exception creating the client process: " + e);
            e.printStackTrace();
        }
    }

    /**
     * Writes a line to the server, adding a line separator at the end.
     * @param line to write
     */
    private void writeToServer(String line)
    {
        try {
            output.write(line + lineSep);
            output.flush();
        }catch(Exception e)
        {
            System.out.println("Error trying to write " + line + " to the server.");
            e.printStackTrace();
        }
    }


    /**
     * Writes a line to the server, adding a line separator at the end.
     * @param messageId the server is expecting.
     * @param line to write
     * @param log if true, write to file as well.
     */
    public void writeToServer(long messageId, String line, boolean log)
    {
        String msg = messageId + ClientComm.TOKEN_SEP + line;
        this.writeToServer(msg);
        if(log) this.writeToFile(msg);
    }

    /**
     * Writes a line to the client debug file, adding a line separator at the end.
     * @param line to write
     */
    public void writeToFile(String line)
    {
        try {
            fileOutput.write(line + lineSep);
            fileOutput.flush();
        }catch(Exception e)
        {
            System.out.println("Error trying to write " + line + " to the local file.");
            e.printStackTrace();
        }
    }

}
