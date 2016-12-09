import core.ArcadeMachine;

import java.util.Random;

/**
 * Created by Jialin Liu on 21/10/2016.
 * CSEE, University of Essex, UK
 * Email: jialin.liu@essex.ac.uk
 * <p/>
 * Respect to Google Java Style Guide:
 * https://google.github.io/styleguide/javaguide.html
 */
public class TestLearner {

  public static void main(String[] args) {
    //Available controllers:
    String sampleRandomController = "controllers.learner.sampleRandom.Agent";
    String doNothingController = "controllers.singlePlayer.doNothing.Agent";


    String gamesPath = "examples/gridphysics/";
    String games[] = new String[]{};
    String generateLevelPath = "examples/gridphysics/";

    //All public games
    games = new String[]{"aliens", "angelsdemons", "assemblyline", "avoidgeorge", "bait", //0-4
        "beltmanager", "blacksmoke", "boloadventures", "bomber", "bomberman",         //5-9
        "boulderchase", "boulderdash", "brainman", "butterflies", "cakybaky",         //10-14
        "camelRace", "catapults", "chainreaction", "chase", "chipschallenge",         //15-19
        "clusters", "colourescape", "chopper", "cookmepasta", "cops",                 //20-24
        "crossfire", "defem", "defender", "digdug", "dungeon",                       //25-29
        "eighthpassenger", "eggomania", "enemycitadel", "escape", "factorymanager",   //30-34
        "firecaster", "fireman", "firestorms", "freeway", "frogs",                   //35-39
        "garbagecollector", "gymkhana", "hungrybirds", "iceandfire", "ikaruga",       //40-44
        "infection", "intersection", "islands", "jaws", "killbillVol1",               //45-49
        "labyrinth", "labyrinthdual", "lasers", "lasers2", "lemmings",                //50-54
        "missilecommand", "modality", "overload", "pacman", "painter",                //55-59
        "pokemon", "plants", "plaqueattack", "portals", "racebet",                    //60-64
        "raceBet2", "realportals", "realsokoban", "rivers", "roadfighter",            //65-69
        "roguelike", "run", "seaquest", "sheriff", "shipwreck",                       //70-74
        "sokoban", "solarfox", "superman", "surround", "survivezombies",              //75-79
        "tercio", "thecitadel", "thesnowman", "waitforbreakfast", "watergame",       //80-84
        "waves", "whackamole", "wildgunman", "witnessprotection", "wrapsokoban",      //85-89
        "zelda", "zenpuzzle"};                                                       //90, 91


    //Other settings
    boolean visuals = true;
    int seed = new Random().nextInt();

    //Game and level to play
    for (int gameIdx = 0; gameIdx < games.length; gameIdx++) {
//      int gameIdx = 58;
      int levelIdx = 0; //level names from 0 to 4 (game_lvlN.txt).
      String game = gamesPath + games[gameIdx] + ".txt";
      String level1 = gamesPath + games[gameIdx] + "_lvl" + levelIdx + ".txt";

      ArcadeMachine.runOneGame(game, level1, false, doNothingController, null, seed, 0);
    }
  }
}
