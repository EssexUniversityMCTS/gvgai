import utils.ClientComm;
import utils.CompetitionParameters;
import utils.ElapsedWallTimer;

import java.io.IOException;

/**
 * Created by dperez on 01/06/2017.
 */
public class TestLearningClient
{
    public static void main(String[] args)
    {
        assert (CompetitionParameters.USE_SOCKETS);

        ElapsedWallTimer wallClock = new ElapsedWallTimer();

        //Available controllers:
        String scriptFile;
        if(CompetitionParameters.OS_WIN)
        {
            scriptFile = "src\\utils\\runServer_nocompile.bat";
        }else{
            scriptFile = "src/utils/runServer_nocompile.sh";
        }

        //Agent to play with
        String agentName = "agents.random.Agent";

        //Start the server side of the communication.
        try{
            ProcessBuilder builder = new ProcessBuilder(scriptFile);
            builder.redirectErrorStream(true);
            builder.start();
            System.out.println("Server process started [OK]");
        }catch(IOException e)
        {
            e.printStackTrace();
        }

        //Start the client side of the communication
        ClientComm ccomm = new ClientComm(agentName);
        ccomm.startComm();

        //Report total time spent.
        int minutes = (int) wallClock.elapsedMinutes();
        int seconds = ((int) wallClock.elapsedSeconds()) % 60;
        System.out.printf("\n \t --> Real execution time: %d minutes, %d seconds of wall time.\n", minutes, seconds);
    }

}