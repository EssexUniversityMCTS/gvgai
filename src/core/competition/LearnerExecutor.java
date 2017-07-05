package core.competition;

import tracks.LearningMachine;

import java.io.IOException;
import java.util.Random;

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
    String gamesPathPrepend = args[0];
    String game = args[1];
    String agentName = args[2];
    String clientType = args[3];
    String action_file = args[4];
    String port = args[5];
    System.out.println("gamesPathPrepend: " + gamesPathPrepend);
    System.out.println("Game: " + game);
    System.out.println("Agent Name: " + agentName);
    System.out.println("Client Type: " + clientType);
    System.out.println("Agent Action file: " + action_file );
    System.out.println("Port: " + port);

    // Available games:
    String gridGamesPath = gamesPathPrepend + "examples/gridphysics/";
    String contGamesPath = gamesPathPrepend + "examples/contphysics/";
    String gamesPath;

    boolean GRID_PHYSICS = true;
    // All public games (gridphysics)
    if(GRID_PHYSICS) {
      gamesPath = gridGamesPath;
    }else{
      gamesPath = contGamesPath;
    }

    //Game and level to play
    game = gamesPath + game + ".txt";
    String[] level_files = new String[5];
    for (int i = 0; i <= 4; i++){
      level_files[i] = gamesPath + game + "_lvl" + i +".txt";
    }

    String cmd[] = new String[]{null, agentName, port, clientType};
    boolean visuals = false;
    LearningMachine.runMultipleGames(game, level_files, cmd, new String[]{null}, visuals);
    System.out.println("END-GAME");
  }
}
