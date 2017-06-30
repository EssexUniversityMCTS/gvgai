package tracks.singleLearning;

import core.competition.CompetitionParameters;
import tools.ElapsedWallTimer;
import tracks.LearningMachine;

import java.util.Random;

/**
 * Created by Daniel on 07.03.2017.
 */
public class TestSingleLearning {
    public static void main(String[] args) throws Exception {

        ElapsedWallTimer wallClock = new ElapsedWallTimer();

        // Type of client to test against (Python/Java)
        String clientType = "java"; //"python";

        //Available controllers:
        String scriptFile;
        if (CompetitionParameters.OS_WIN) {
            scriptFile = "src\\tracks\\singleLearning\\utils\\runClient_nocompile.bat";
        } else {
            scriptFile = CompetitionParameters.USE_SOCKETS ? "src/tracks/singleLearning/utils/runClient_nocompile.sh" :
                "src/tracks/singleLearning/utils/runClient_nocompile_pipes.sh";
        }

        //Port for the socket.
        String port = CompetitionParameters.SOCKET_PORT + "";

        //Agent to play with
        String agentName;
        if (clientType.equals("python")) {
            agentName = "sampleAgents";
        } else {
            agentName = "agents.random.Agent";
        }


        //Building the command line
        String cmd[] = new String[]{scriptFile, agentName, port, clientType};


        // Available games:
        String gridGamesPath = "examples/gridphysics/";
        String contGamesPath = "examples/contphysics/";
        String gamesPath;
        String games[];
        boolean GRID_PHYSICS = true;

        // All public games (gridphysics)
        if(GRID_PHYSICS) {
            gamesPath = gridGamesPath;
            games = new String[]{"aliens", "angelsdemons", "assemblyline", "avoidgeorge", "bait", // 0-4
                    "beltmanager", "blacksmoke", "boloadventures", "bomber", "bomberman", // 5-9
                    "boulderchase", "boulderdash", "brainman", "butterflies", "cakybaky", // 10-14
                    "camelRace", "catapults", "chainreaction", "chase", "chipschallenge", // 15-19
                    "clusters", "colourescape", "chopper", "cookmepasta", "cops", // 20-24
                    "crossfire", "defem", "defender", "digdug", "dungeon", // 25-29
                    "eighthpassenger", "eggomania", "enemycitadel", "escape", "factorymanager", // 30-34
                    "firecaster", "fireman", "firestorms", "freeway", "frogs", // 35-39
                    "garbagecollector", "gymkhana", "hungrybirds", "iceandfire", "ikaruga", // 40-44
                    "infection", "intersection", "islands", "jaws", "killBillVol1", // 45-49
                    "labyrinth", "labyrinthdual", "lasers", "lasers2", "lemmings", // 50-54
                    "missilecommand", "modality", "overload", "pacman", "painter", // 55-59
                    "pokemon", "plants", "plaqueattack", "portals", "raceBet", // 60-64
                    "raceBet2", "realportals", "realsokoban", "rivers", "roadfighter", // 65-69
                    "roguelike", "run", "seaquest", "sheriff", "shipwreck", // 70-74
                    "sokoban", "solarfox", "superman", "surround", "survivezombies", // 75-79
                    "tercio", "thecitadel", "thesnowman", "waitforbreakfast", "watergame", // 80-84
                    "waves", "whackamole", "wildgunman", "witnessprotection", "wrapsokoban", // 85-89
                    "zelda", "zenpuzzle"}; // 90, 91

        }else{
            gamesPath = contGamesPath;
            games = new String[]{"artillery", "asteroids", "bird", "bubble", "candy",   //0 - 4
                    "lander", "mario", "pong", "ptsp", "racing"};                       //5 - 9
        }

        //Other settings
        boolean visuals = false;
        String recordActionsFile = null; //where to record the actions executed. null if not to save.
        int seed = new Random().nextInt();

        //Game and level to play
        int gameIdx = 0;
        int levelIdx = 0; //level names from 0 to 4 (game_lvlN.txt).
        String game = gamesPath + games[gameIdx] + ".txt";
        String level1 = gamesPath + games[gameIdx] + "_lvl" + levelIdx +".txt";

        String[] level_files = new String[5];
        for (int i = 0; i <= 4; i++){
            level_files[i] = gamesPath + games[gameIdx] + "_lvl" + i +".txt";
        }


        LearningMachine.runMultipleGames(game, level_files, cmd, new String[]{null}, visuals);



        //Report total time spent.
        int minutes = (int) wallClock.elapsedMinutes();
        int seconds = ((int) wallClock.elapsedSeconds()) % 60;
        System.out.printf("\n \t --> Real execution time: %d minutes, %d seconds of wall time.\n", minutes, seconds);
    }
}
