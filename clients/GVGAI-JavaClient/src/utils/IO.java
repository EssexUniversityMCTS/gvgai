package utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by dperez on 01/06/2017.
 */
public abstract class IO extends Thread{

    /**
     * Line separator for messages.
     */
    protected String lineSep = System.getProperty("line.separator");

    /**
     * Writer of the player. Used to pass the action of the player to the server.
     */
    private PrintWriter fileOutput;


    /**
     * Default constructor.
     */
    public IO()
    {
        try
        {
            fileOutput = new PrintWriter(new File("logs/clientLog.txt"), "utf-8");
        } catch (Exception e) {
            System.out.println("Exception creating the log file on client: " + e);
            e.printStackTrace();
        }
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
            logStackTrace(e);
        }
    }


    public abstract void initBuffers();

    protected abstract void writeToServer(String line);

    public abstract void writeToServer(long messageId, String line, boolean log);

    public abstract String readLine() throws IOException;

    public void logStackTrace(Exception e) {
        e.printStackTrace(this.fileOutput);
    }

}
