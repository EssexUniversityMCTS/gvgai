package tracks.singleLearning.utils;

import core.competition.CompetitionParameters;
import tools.ElapsedWallTimer;
import tracks.LearningMachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static core.competition.CompetitionParameters.IMG_PATH;

/**
 * Created by dperez on 01/06/2017.
 */
public class JavaServer {

    public static void main(String[] args) throws Exception {
        /** Init params */
        int gameIdx = 0;
        String clientType = "java"; //"python"; // Type of client to test against (Python/Java)
        String shDir = "src/tracks/singleLearning/utils";
        String clientDir = ".";
        String gamesDir = ".";
        //Other settings
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
        if (params.containsKey("shDir")) {
            shDir = params.get("shDir").get(0);
        }
        if (params.containsKey("clientDir")) {
            clientDir = params.get("clientDir").get(0);
        }
        if (params.containsKey("gamesDir")) {
            gamesDir = params.get("gamesDir").get(0);
            IMG_PATH = gamesDir + "/" + IMG_PATH;
        }
        if (params.containsKey("imgDir")) {
            String imgDir = params.get("imgDir").get(0);
            IMG_PATH = imgDir + "/" + IMG_PATH;
        }
        if (params.containsKey("visuals")) {
            visuals = true;
        } else {
            visuals = false;
        }
        /** Now prepare to start */
        ElapsedWallTimer wallClock = new ElapsedWallTimer();

        //Port for the socket.
        String port = CompetitionParameters.SOCKET_PORT + "";

        //Building the command line
        String cmd[] = new String[]{null, null, port, clientType};


        // Available games:
        String gridGamesPath = gamesDir + "/examples/gridphysics/";
        String contGamesPath = gamesDir + "/examples/contphysics/";
        String gamesPath;
        String games[];
        boolean GRID_PHYSICS = true;

//        System.out.println("[LOG] Server asked to run at port: " + port + " where games are IN " + new File(gamesDir+ "/examples").getAbsolutePath());

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



        //Game and level to play
        String game = gamesPath + games[gameIdx] + ".txt";
        String[] level_files = new String[5];
        for (int i = 0; i <= 4; i++){
            level_files[i] = gamesPath + games[gameIdx] + "_lvl" + i +".txt";
        }
        // This plays a training round for a specified game.
        System.out.println("[GAME] Game idx:" + gameIdx + " game name " + games[gameIdx]);
        LearningMachine.runMultipleGames(game, level_files, cmd, new String[]{null}, visuals);



        //Report total time spent.
        int minutes = (int) wallClock.elapsedMinutes();
        int seconds = ((int) wallClock.elapsedSeconds()) % 60;
        System.out.printf("\n \t --> Real execution time: %d minutes, %d seconds of wall time.\n", minutes, seconds);
    }
}