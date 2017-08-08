import utils.ClientComm;
import utils.CompetitionParameters;
import utils.ElapsedWallTimer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dperez on 01/06/2017.
 */
public class TestLearningClient
{
    public static void main(String[] args)
    {
        assert (CompetitionParameters.USE_SOCKETS);
        /** Init params */
        int gameId = 2;
        String shDir = "./src/utils";
        String serverDir;
        String serverJar = "";
        String gameFile = "";
        String levelFile = "";
        if (CompetitionParameters.OS_WIN) {
            serverDir = "..\\..";
        } else {
            serverDir = "../..";
        }
        String agentName = "sampleRandom.Agent";         //Agent to play with
        boolean visuals = false;
        /** Get arguments */
        Map<String, List<String>> params = new HashMap<>();
        List<String> options = null;
        for (int i = 0; i < args.length; i++) {
            final String a = args[i];
            if (a.charAt(0) == '-') {
                if (a.length() < 2) {
                    System.err.println("Error at argument " + a);
                    return;
                }
                options = new ArrayList<>();
                params.put(a.substring(1), options);
            } else if (options != null) {
                options.add(a);
            }
            else {
                System.err.println("Illegal parameter usage");
                return;
            }
        }
        /** Update params */
        if (params.containsKey("gameId")) {
            gameId = Integer.parseInt(params.get("gameId").get(0));
        }
        if (params.containsKey("shDir")) {
            shDir = params.get("shDir").get(0);
        }
        if (params.containsKey("serverDir")) {
            serverDir = params.get("serverDir").get(0);
        }
        if (params.containsKey("agentName")) {
            agentName = params.get("agentName").get(0);
        }
        if (params.containsKey("visuals")) {
            visuals = true;
        }
        if (params.containsKey("serverJar")) {
            serverJar = params.get("serverJar").get(0);
        }
        if (params.containsKey("gameFile")) {
            gameFile = params.get("gameFile").get(0);
        }
        if (params.containsKey("levelFile")) {
            levelFile = params.get("levelFile").get(0);
        }
        ElapsedWallTimer wallClock = new ElapsedWallTimer();

        //Available controllers:
        String scriptFile;
        String[] cmd;
        if (serverJar == "") {
            if (CompetitionParameters.OS_WIN) {
                scriptFile = shDir + "\\runServer_nocompile.bat";
            } else {
                scriptFile = shDir + "/runServer_nocompile.sh";
            }
            if (visuals) {
                cmd = new String[]{scriptFile, gameId + "", serverDir, "true"};
            } else {
                cmd = new String[]{scriptFile, gameId + "", serverDir, "false"};
            }
        } else {
            scriptFile = shDir + "/runServer_compile.sh";
//            cmd = new String[]{scriptFile, serverJar, gameId + "", serverDir};
            cmd = new String[]{scriptFile, serverJar, gameId + "", serverDir,gameFile,levelFile};
        }

        //Start the server side of the communication.
        try{
            ProcessBuilder builder = new ProcessBuilder(cmd);
            builder.redirectErrorStream(true);
            builder.start();
            System.out.println("Server process started [OK]");
            System.out.println("Agent name:" + agentName);
        } catch(IOException e) {
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