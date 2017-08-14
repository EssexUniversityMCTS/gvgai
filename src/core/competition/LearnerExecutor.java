package core.competition;

import tools.ElapsedWallTimer;
import tracks.LearningMachine;

import java.io.IOException;
import java.util.*;

import static core.competition.CompetitionParameters.IMG_PATH;

/**
 * Created by Jialin Liu on 23/06/2017.
 * CSEE, University of Essex, UK
 * Email: jialin.liu@essex.ac.uk
 * <p>
 * Respect to Google Java Style Guide:
 * https://google.github.io/styleguide/javaguide.html
 */
public class LearnerExecutor {
  public static void main(String[] args) throws IOException {
    /** Init params */
    int gameIdx = 0;
    String clientType = "java"; //"python"; // Type of client to test against (Python/Java)
    String gameFile = "";
    String[] levelFile = new String[5];
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
      gameIdx = Integer.parseInt(params.get("gameId").get(0));
    }
    if (params.containsKey("clientType")) {
      clientType = params.get("clientType").get(0);
    }
    if (params.containsKey("gamesDir")) {
      IMG_PATH = params.get("gamesDir").get(0) + "/" + IMG_PATH;
    }

    if (params.containsKey("gameFile")) {
      gameFile = params.get("gameFile").get(0);
    }
    if (params.containsKey("levelFile")) {
      String levelFileStr = params.get("levelFile").get(0);
      String[] levelFileSplitted = levelFileStr.split(":");
      for (int i=0; i<5; i++) {
        levelFile[i] = levelFileSplitted[i];
      }
    }
    /** Now prepare to start */
    ElapsedWallTimer wallClock = new ElapsedWallTimer();

    //Port for the socket.
    String port = CompetitionParameters.SOCKET_PORT + "";

    //Building the command line
    String cmd[] = new String[]{null, null, port, clientType};


    System.out.println("[GAME] Game idx:" + gameIdx);
    LearningMachine.runMultipleGames(gameFile, levelFile, cmd, new String[]{null}, visuals);

    //Report total time spent.
    int minutes = (int) wallClock.elapsedMinutes();
    int seconds = ((int) wallClock.elapsedSeconds()) % 60;
    System.out.printf("\n \t --> Real execution time: %d minutes, %d seconds of wall time.\n", minutes, seconds);
  }
}