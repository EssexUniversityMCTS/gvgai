package core.competition;

import tracks.ArcadeMachine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by dperez on 24/05/16.
 */
public class GVGExecutor {

    public static String[] gameFiles;
    public static ArrayList<String>[] levelFiles;
    public static String agent;
    public static boolean saveActions;
    public static String[] resultFiles;
    public static int[] seeds;
    public static boolean visibility;
    public static int repetitions;

    public static void printHelp()
    {
        System.out.println("Usage: java GVGExecutor <params>");
        System.out.println("\t-g Game file(s) to play in.\n" +
                "\t\t[Mandatory]\n" +
                "\t\t[Games separated by ':']\n" +
                "\t\t[Ex: -g examples/gridphysics/aliens.txt:examples/gridphysics/sokoban.txt]");
        System.out.println("\t-l Level file(s) to play in.\n" +
                "\t\t[Mandatory]\n" +
                "\t\t[Games separated by ':'. Level files within each game separated by ';'. Number of games must match the one from '-g']\n" +
                "\t\t[Ex: -l examples/gridphysics/aliens_lvl0.txt;examples/gridphysics/aliens_lvl1.txt:examples/gridphysics/sokoban_lvl0.txt;examples/gridphysics/sokoban_lvl1.txt;examples/gridphysics/sokoban_lvl2.txt]");
        System.out.println("\t-ag Agent name to execute.\n" +
                "\t\t[Mandatory]\n" +
                "\t\t[Only one agent]\n" +
                "\t\t[Ex: -ag tracks.singlePlayer.deprecated.sampleMCTS.Agent]" +
                "\t\t[Ex for HUMANS: -ag tracks.singlePlayer.tools.human.Agent]");
        System.out.println("\t-res Output results file.\n" +
                "\t\t[Optional]\n" +
                "\t\t[Games separated by ':'. A file per game, or all games the same common file ('output.txt' as default). Number of games must match the one from '-g']\n" +
                "\t\t[Ex: -res output_game1.txt:output_game2.txt]");
        System.out.println("\t-sds Seeds for all games\n" +
                "\t\t[Optional]\n" +
                "\t\t[Seeds separated by ':'. A seed per game, or all random (default). Number of games must match the one from '-g']\n" +
                "\t\t[Ex: -sds 1342:3513]");
        System.out.println("\t-vis Graphics on.\n" +
                "\t\t[Optional]\n" +
                "\t\t[Default: on for humans, off for bots.]\n" +
                "\t\t[Ex: -vis 1]");
        System.out.println("\t-rep Repetitions per level\n" +
                "\t\t[Optional]\n" +
                "\t\t[Default: 1 repetition.]\n" +
                "\t\t[Ex: -rep 5]\n");
        System.out.printf("\tComplete example:\n" +
                "\t\tjava GVGExecutor -g examples/gridphysics/aliens.txt:examples/gridphysics/sokoban.txt -l examples/gridphysics/aliens_lvl0.txt;examples/gridphysics/aliens_lvl1.txt:examples/gridphysics/sokoban_lvl0.txt;examples/gridphysics/sokoban_lvl1.txt;examples/gridphysics/sokoban_lvl2.txt -ag tracks.singlePlayer.deprecated.sampleMCTS.Agent -res output_game1.txt:output_game2.txt -seed 1342:3513 -vis 1 -rep 5");

        //Other examples:
        // -g examples/gridphysics/aliens.txt -l examples/gridphysics/aliens_lvl0.txt -ag tracks.singlePlayer.deprecated.sampleMCTS.Agent -res output_game1.txt -sds 1342 -vis 1 -rep 1
        // -g examples/gridphysics/aliens.txt -l examples/gridphysics/aliens_lvl0.txt:examples/gridphysics/aliens_lvl1.txt -ag tracks.singlePlayer.deprecated.sampleMCTS.Agent -res output_game1.txt -sds 1342 -vis 1 -rep 2
        // -g examples/gridphysics/aliens.txt -l examples/gridphysics/aliens_lvl0.txt -ag tracks.singlePlayer.deprecated.sampleMCTS.Agent -res output_game1.txt -vis 0 -rep 1
        // -g examples/gridphysics/aliens.txt:examples/gridphysics/camelRace.txt -l examples/gridphysics/aliens_lvl0.txt:examples/gridphysics/aliens_lvl1.txt;examples/gridphysics/camelRace_lvl0.txt -ag tracks.singlePlayer.deprecated.sampleMCTS.Agent -res o1.txt:o2.txt -sds 1342:111 -vis 1 -rep 2
        // -g examples/gridphysics/aliens.txt:examples/gridphysics/camelRace.txt -l examples/gridphysics/aliens_lvl0.txt:examples/gridphysics/aliens_lvl1.txt;examples/gridphysics/camelRace_lvl0.txt -ag tracks.singlePlayer.deprecated.sampleMCTS.Agent -res o1.txt:o2.txt -vis 0 -rep 2
    }

    @SuppressWarnings("unchecked")
    public static void parseParameter(String arg1, String arg2)
    {
        if(arg1.equalsIgnoreCase("-g"))
            gameFiles = arg2.split(":");
        else if(arg1.equalsIgnoreCase("-l"))
        {
            String allLevels[] = arg2.split(";");
            levelFiles = new ArrayList[allLevels.length];
            for(int i = 0; i < allLevels.length; ++i)
            {
                levelFiles[i] = new ArrayList<>();
                String levels[] = allLevels[i].split(":");
                for(String l : levels)
                    levelFiles[i].add(l);
            }
        }
        else if(arg1.equalsIgnoreCase("-ag"))
            agent = arg2;
        else if(arg1.equalsIgnoreCase("-act"))
            saveActions = Integer.parseInt(arg2) == 0 ? false : true;
        else if(arg1.equalsIgnoreCase("-res"))
            resultFiles = arg2.split(":");
        else if(arg1.equalsIgnoreCase("-sds"))
        {
            String allSeeds [] = arg2.split(":");
            seeds = new int[allSeeds.length];
            for(int i = 0; i < seeds.length; ++i)
            {
                seeds[i] = Integer.parseInt(allSeeds[i]);
            }
        }
        else if(arg1.equalsIgnoreCase("-vis"))
            visibility = Integer.parseInt(arg2) == 0 ? false : true;
        else if(arg1.equalsIgnoreCase("-rep"))
            repetitions = Integer.parseInt(arg2);
    }

    public static void main(String args[])
    {
        if(args.length < 6 || (args.length % 2 != 0))
        {
            printHelp();
            return;
        }

        //Some default values
        saveActions = false;
        visibility = false;
        repetitions = 1;

        for(int i = 0; i < args.length; i+=2)
            parseParameter(args[i], args[i+1]);

        //Some checks
        int num_games = gameFiles.length;
        if(num_games != levelFiles.length)
            throw new RuntimeException("Number of games in -g and -l must match.");
        if(resultFiles != null && num_games != resultFiles.length)
            throw new RuntimeException("If result output files are provided, their number must match the number of games.");
        if(seeds != null && num_games != seeds.length)
            throw new RuntimeException("If seeds are provided, their number must match the number of games.");
        if(seeds == null)
        {
            seeds = new int[num_games];
            for(int i = 0; i < num_games; ++i) seeds[i] = -1;
        }

        if(resultFiles == null)
        {
            resultFiles = new String[]{"output.txt"};
        }

        try {

            BufferedWriter writer = null;
            //For each game:
            for (int i = 0; i < num_games; ++i) {

                String outputFile = resultFiles.length==1? resultFiles[0] : resultFiles[i];

                if(resultFiles.length==1 && writer==null)
                    writer = new BufferedWriter(new FileWriter(new File(outputFile)));
                else if(resultFiles.length > 1)
                {
                    if(writer != null)
                        writer.close();
                    writer = new BufferedWriter(new FileWriter(new File(outputFile)));
                }


                String game = gameFiles[i];
                int num_levels = levelFiles[i].size();
                String levels[] = new String[num_levels];

                //For each level:
                for (int j = 0; j < num_levels; ++j) {
                    levels[j] = levelFiles[i].get(j);

                    //For each repetition:
                    for (int k = 0; k < repetitions; ++k) {
                        String actionFile = "actions_game_" + i + "_level_" + j + "_" + k + ".txt";

                        int seed = seeds[i];
                        if(seed == -1)
                            seed = new Random().nextInt();

                        //Play!
                        double[] result = ArcadeMachine.runOneGame(game, levels[j], visibility, agent,
                                saveActions ? actionFile : null, seed, 0);

                        String line = game + " " + levels[j] + " " + seed + " ";
                        for (double d : result)
                            line += (d + " ");
                        writer.write(line + "\n");
                        System.out.println(line);

                    }
                }
            }

            writer.close();

        }catch(Exception e)
        {
            System.out.println(e);
        }



    }

}
