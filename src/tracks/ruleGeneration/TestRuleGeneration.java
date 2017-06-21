package tracks.ruleGeneration;

import java.util.Random;

/**
 * Created by dperez on 19/03/2017.
 */
public class TestRuleGeneration {
    public static void main(String[] args) throws Exception {
	//Available Controllers
	String sampleMCTSController = "tracks.singlePlayer.advanced.sampleMCTS.Agent";

        // Available Rule Generator
        String randomRuleGenerator = "tracks.ruleGeneration.randomRuleGenerator.RuleGenerator";
        String constructiveRuleGenerator = "tracks.ruleGeneration.constructiveRuleGenerator.RuleGenerator";
        String geneticRuleGenerator = "tracks.ruleGeneration.geneticRuleGenerator.RuleGenerator";

        // Available games:
        String gamesPath = "examples/gridphysics/";
        String physicsGamesPath = "examples/contphysics/";
        String generateRulePath = gamesPath;

        // All public games (gridphysics)
        String[] games = new String[]{"aliens", "angelsdemons", "assemblyline", "avoidgeorge", "bait", // 0-4
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


        String recordActionsFile = null;// "actions_" + games[gameIdx] + "_lvl"
        // + levelIdx + "_" + seed + ".txt";
        // where to record the actions
        // executed. null if not to save.

        // Other settings
        boolean visuals = true;
        int seed = new Random().nextInt();
        int gameIdx = 0;
        int levelIdx = 0; // level names from 0 to 4 (game_lvlN.txt).
        String game = generateRulePath + games[gameIdx] + ".txt";
        String level1 = gamesPath + games[gameIdx] + "_lvl" + levelIdx + ".txt";
        String recordGameFile = generateRulePath + games[gameIdx] + "_ggame.txt";

        // 1. Generate rules (Interaction and Terminations) for a fixed level
        if(RuleGenMachine.generateRules(game, level1, randomRuleGenerator, recordGameFile, seed)){
            // RuleGenMachine.playOneGame(game, recordGameFile, level1, recordActionsFile, seed);
            RuleGenMachine.runOneGame(game, recordGameFile, level1, visuals, sampleMCTSController, recordActionsFile, seed, 0);
        }
    }
}

