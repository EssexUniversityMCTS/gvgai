import core.ArcadeMachine;
import core.LearningMachine;

import java.util.Random;

/**
 * Created by Daniel on 07.03.2017.
 */
public class ClientCommunicationTest {
    public static void main(String[] args) throws Exception {
        //Available controllers:
        String javaController = "..\\clients\\GVGAI-JavaClient\\src\\JavaClient.java";

        //Available games:
        String gamesPath = "examples/gridphysics/";

        //CIG 2014 Training Set Games
        String games[] = new String[]{"aliens", "boulderdash", "butterflies", "chase", "frogs",
                "missilecommand", "portals", "sokoban", "survivezombies", "zelda"};

        //CIG 2014 Validation Set Games
        //String games[] = new String[]{"camelRace", "digdug", "firestorms", "infection", "firecaster",
        //      "overload", "pacman", "seaquest", "whackamole", "eggomania"};

        //CIG 2015 New Training Set Games
        //String games[] = new String[]{"bait", "boloadventures", "brainman", "chipschallenge",  "modality",
        //                              "painter", "realportals", "realsokoban", "thecitadel", "zenpuzzle"};


        //Other settings
        boolean visuals = true;
        String recordActionsFile = null; //where to record the actions executed. null if not to save.
        int seed = new Random().nextInt();

        //Game and level to play
        int gameIdx = 7;
        int levelIdx = 0; //level names from 0 to 4 (game_lvlN.txt).
        String game = gamesPath + games[gameIdx] + ".txt";
        String level1 = gamesPath + games[gameIdx] + "_lvl" + levelIdx +".txt";

        // 1. This starts a game, in a level, played by a human.
        //ArcadeMachine.playOneGame(game, level1, recordActionsFile, seed);

        // 2. This plays a game in a level by the controller (through the "Learning Machine").
        //int trainingPlays = 100;
        LearningMachine.runOneGame(game, level1, visuals, javaController, recordActionsFile, seed, true);

        // 3. This replays a game from an action file previously recorded
        //String readActionsFile = "seminar/SeaQuest.txt";  //This example is for
        //ArcadeMachine.replayGame(game, level1, visuals, readActionsFile);
    }
}
