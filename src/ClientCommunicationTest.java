import core.gameMachine.LearningMachine;

import java.util.Random;

/**
 * Created by Daniel on 07.03.2017.
 */
public class ClientCommunicationTest {
    public static void main(String[] args) throws Exception {
        //Available controllers:
        String javaController = "src\\runClient_nocompile.bat";

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

        String[] level_files = new String[3];
        for (int i = 0; i <= 2; i++){
            level_files[i] = gamesPath + games[gameIdx] + "_lvl" + i +".txt";
        }

        // 1. This plays a game in a level by the controller (through the "Learning Machine").
        //int trainingPlays = 100;
        //LearningMachine.runOneGame(game, level1, visuals, javaController, recordActionsFile, seed, true);

        // 1. This plays a training round for a specified game.
        LearningMachine.runMultipleGames(game, level_files, javaController, new String[]{null});
    }
}
