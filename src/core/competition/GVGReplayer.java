package core.competition;

import tracks.ArcadeMachine;

/**
 * Created by dperez on 24/05/16.
 */
public class GVGReplayer {

    public static String game;
    public static String level;
    public static String actionFile;
    public static int delay;

    public static void printHelp()
    {
        System.out.println("Usage: java GVGReplayer <params>");
        System.out.println("\t-g Game file to play in.\n" +
                "\t\t[Mandatory]\n" +
                "\t\t[Ex: -g examples/gridphysics/aliens.txt]");
        System.out.println("\t-l Level file to play in.\n" +
                "\t\t[Mandatory]\n" +
                "\t\t[Ex: -l examples/gridphysics/aliens_lvl0.txt");
        System.out.println("\t-a Action file with the actions.\n" +
                "\t\t[Mandatory]\n" +
                "\t\t[Ex: -a actionsFile_aliens_lvl0.txt]");
        System.out.println("\t-d Delay.\n" +
                "\t\t[Optional]\n" +
                "\t\t[Default: 0ms (no delay)]\n" +
                "\t\t[Ex: -d 15]\n");
        System.out.printf("\tComplete example:\n" +
                "\t\tjava GVGReplayer -g examples/gridphysics/aliens.txt -l examples/gridphysics/aliens_lvl0.txt -a actionsFile_aliens_lvl0.txt -d 10");

    }

    public static void parseParameter(String arg1, String arg2)
    {
        if(arg1.equalsIgnoreCase("-g"))
            game = arg2;
        else if(arg1.equalsIgnoreCase("-l"))
            level = arg2;
        else if(arg1.equalsIgnoreCase("-a"))
            actionFile = arg2;
        else if(arg1.equalsIgnoreCase("-d"))
            delay = Integer.parseInt(arg2);
    }

    public static void main(String args[])
    {
        if(args.length < 6 || (args.length % 2 != 0))
        {
            printHelp();
            return;
        }

        delay = 1;
        for(int i = 0; i < args.length; i+=2)
            parseParameter(args[i], args[i+1]);

        CompetitionParameters.LONG_DELAY = delay; //This is a bit of a hack, admittedly.
        ArcadeMachine.replayGame(game, level, true, actionFile);
    }

}
