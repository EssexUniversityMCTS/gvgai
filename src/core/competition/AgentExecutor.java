package core.competition;

import java.util.Random;

import tracks.ArcadeMachine;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/10/13
 * Time: 16:29
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class AgentExecutor {

    public static void main(String[] args) {

        String map = args[0];
        String level = args[1];
        String playerClassString = args[2];
        String action_file = args[3];
        System.out.println("Map: " + map);
        System.out.println("level: " + level);
        System.out.println("Player Class: " + playerClassString);
        System.out.println("Agent Action file: " + action_file );

        int seed = new Random().nextInt();

        double[] gameScore = ArcadeMachine.runOneGame(map, level, false, playerClassString, action_file, seed, 0);
        //System.out.println(gameScore);
    }
}
