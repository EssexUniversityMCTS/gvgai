package utils;

import java.io.*;

/**
 * Created by dperez on 23/05/2017.
 */
public class IOPipe extends IO {

    /**
     * Reader of the player. Will read the game state from the client.
     */
    public static BufferedReader input;

    /**
     * Writer of the player. Used to pass the action of the player to the server.
     */
    public static BufferedWriter output;

    /**
     * Creates the buffers for pipe communication.
     */
    @Override
    public void initBuffers() {

        try {
            input = new BufferedReader(new InputStreamReader(System.in));
            output = new BufferedWriter(new OutputStreamWriter(System.out));

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
        return input.readLine();
    }

    /**
     * Writes a line to the server, adding a line separator at the end.
     * @param line to write
     */
    @Override
    protected void writeToServer(String line)
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




}
