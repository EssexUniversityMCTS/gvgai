import com.google.gson.Gson;
import ontology.Avatar;
import ontology.BitGrid;
import ontology.Game;

import java.awt.*;
import java.io.*;
import java.util.Random;

/**
 * Created by Daniel on 04/03/2017.
 */
public class ClientComm {

    public static enum COMM_STATE {
        START, INIT, INIT_END, ACT, ACT_END, ENDED, ENDED_END
    }

    /**
     * Reader of the player. Will read the game state from the client.
     */
    public static BufferedReader input;

    /**
     * Writer of the player. Used to pass the action of the player to the server.
     */
    public static BufferedWriter output;

    /**
     * Line separator for messages.
     */
    private String lineSep = System.getProperty("line.separator");

    /**
     * Communication state
     */
    private COMM_STATE commState;

    /**
     * Number of games played
     */
    private int numGames;

    /**
     * Game information
     */
    public Game game;

    /**
     * Avatar information
     */
    public Avatar avatar;


    /**
     * Indicates if the current game is a training game
     */
    private boolean isTraining;

    /**
     * Creates the client.
     */
    public ClientComm() {
        commState = COMM_STATE.START;
        game = new Game();
        avatar = new Avatar();
        numGames = 0;
        isTraining = false;
    }

    /**
     * Creates communication buffers and starts listening.
     */
    public void start()
    {
        initBuffers();

        try {
            listen();
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    private void listen() throws IOException {
        String line = "start";
        int messageIdx = 0;
        while (line != null) {
            line = input.readLine();

            commState = processCommandLine(line);
            processLine(line);

            if(commState == COMM_STATE.INIT_END)
            {
                //We can work on some initialization stuff here.
                writeToServer("INIT_DONE");

            }else if(commState == COMM_STATE.ACT_END)
            {
                //This is the place to think and return what action to take.
                int rndActionIdx = new Random().nextInt(avatar.actionList.size());
                String rndAction = avatar.actionList.get(rndActionIdx);
                writeToServer(rndAction);

            }else if(commState == COMM_STATE.ENDED_END)
            {
                //We can study what happened in the game here.
                //For debug, print here game and avatar info:
                game.printToFile(numGames);
                avatar.printToFile(numGames);

                game = new Game();
                avatar = new Avatar();

                writeToServer("GAME_DONE");
            }

            messageIdx++;
        }
    }


    /**
     * Creates the buffers for pipe communication.
     */
    private void initBuffers() {
        try {
            input = new BufferedReader(new InputStreamReader(System.in));
            output = new BufferedWriter(new OutputStreamWriter(System.out));

        } catch (Exception e) {
            System.out.println("Exception creating the client process: " + e);
            e.printStackTrace();
        }
    }

    /**
     * Writes a line to the server, adding a line separator at the end.
     * @param line to write
     */
    private void writeToServer(String line) throws IOException
    {
        output.write(line + lineSep);
        output.flush();
    }

    public COMM_STATE processCommandLine(String commLine)
    {
        if(commLine.contains("INIT-END"))
        {
            String[] splitLine = commLine.split(" ");
            game.remMillis = Integer.parseInt(splitLine[1]);
            return COMM_STATE.INIT_END;

        }else if(commLine.contains("ACT-END")){
            String[] splitLine = commLine.split(" ");
            game.remMillis = Integer.parseInt(splitLine[1]);
            return COMM_STATE.ACT_END;

        }else if(commLine.contains("ENDGAME-END")){
            String[] splitLine = commLine.split(" ");
            game.remMillis = Integer.parseInt(splitLine[1]);
            return COMM_STATE.ENDED_END;

        }else if(commLine.contains("INIT")){
            numGames++;
            String[] splitLine = commLine.split(" ");
            isTraining = (splitLine[1].equalsIgnoreCase("true"));
            return COMM_STATE.INIT;

        }else if(commLine.contains("ACT")){
            return COMM_STATE.ACT;

        }else if(commLine.contains("ENDGAME")){
            return COMM_STATE.ENDED;

        }
        return commState;
    }

    public void processLine(String line)
    {
        Gson gson = new Gson();
        String json = gson.fromJson(line, String.class);

        if(splitLine[0].equalsIgnoreCase("Game"))
        {
            game.score = Double.parseDouble(splitLine[1]);
            game.gameTick = Integer.parseInt(splitLine[2]);
            game.gameWinner = splitLine[3];
            game.gameOver = (splitLine[4].equalsIgnoreCase("true"));

            if(splitLine.length > 6) // It will only be >6 in the init case.
            {
                game.worldDim = new Dimension( Integer.parseInt(splitLine[5]),
                        Integer.parseInt(splitLine[6]));
                game.blockSize = Integer.parseInt(splitLine[7]);
            }

        }else if(splitLine[0].equalsIgnoreCase("Actions"))
        {
            String[] actions = splitLine[1].split(",");
            for (String act : actions)
                avatar.actionList.add(act);

        }else if(splitLine[0].equalsIgnoreCase("Avatar"))
        {
            avatar.position[0] = Double.parseDouble(splitLine[1]);
            avatar.position[1] = Double.parseDouble(splitLine[2]);
            avatar.orientation[0] = Double.parseDouble(splitLine[3]);
            avatar.orientation[1] = Double.parseDouble(splitLine[4]);
            avatar.speed = Double.parseDouble(splitLine[5]);
            avatar.lastAction = splitLine[6];

            if(splitLine.length > 7)
            {
                //We have resources
                String resources[] = splitLine[7].split(";");
                for (String r : resources)
                {
                    int key = Integer.parseInt(r.split(",")[0]);
                    int val = Integer.parseInt(r.split(",")[1]);
                    avatar.resources.put(key, val);
                }
            }

        }else if(splitLine[0].charAt(0) == 's') //Observation grid.
        {
            int spriteID = Integer.parseInt(lineType.substring(1));
            String[] bitData = splitLine[1].split(",");

            int nRows = bitData.length;
            int nColumns = bitData[0].length();

            BitGrid g = new BitGrid(nRows, nColumns);
            for(int r = 0; r < nRows; ++r)
            {
                String row = bitData[r];
                for (int c = 0; c < nColumns; ++c)
                {
                    g.grid[r][c] = (row.charAt(c) == '1');
                }
            }
            game.grid.put(spriteID, g);
        }

    }

}
