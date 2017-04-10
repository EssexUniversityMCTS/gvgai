import com.google.gson.Gson;
import ontology.Game;
import ontology.Avatar;
import ontology.Game;
import ontology.Types;

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
    public COMM_STATE commState;

    /**
     * Number of games played
     */
    private int numGames;

    /**
     * Game information
     */
    public ontology.Game game;

    /**
     * Avatar information
     */
    public Avatar avatar;

    /**
     * Avatar information
     */
    public SerializableStateObservation sso;


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
                // TODO: 27/03/2017 Daniel: no agent for the moment
                //This is the place to think and return what action to take.
                String rndAction = Types.ACTIONS.ACTION_NIL.toString();
                writeToServer(rndAction);

            }else if(commState == COMM_STATE.ENDED_END)
            {
                // TODO: 27/03/2017 Daniel: is the game stopped ?
                //We can study what happened in the game here.
                //For debug, print here game and avatar info:
                game.printToFile(numGames);
                avatar.printToFile(numGames);

                game = new Game();
                avatar = new Avatar();

                // TODO: 27/03/2017 Daniel:  after stopped, start another game ???
                writeToServer("GAME_DONE");
            } else {
                writeToServer("null");
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
        //output.flush();
    }

    public COMM_STATE processCommandLine(String commLine)
    {
        Gson gson = new Gson();
        SerializableStateObservation sso = gson.fromJson(commLine, SerializableStateObservation.class);

        if(sso.gameState == SerializableStateObservation.State.INIT_STATE)
        {
            game.remMillis = sso.elapsedTimer;
            return COMM_STATE.INIT_END;

        }else if(sso.gameState == SerializableStateObservation.State.ACT_STATE){
            game.remMillis = sso.elapsedTimer;;
            return COMM_STATE.ACT_END;

        }else if(commLine.contains("ENDGAME-END")) {
            game.remMillis = sso.elapsedTimer;
            return COMM_STATE.ENDED_END;
        }

        return commState;
    }

    public void processLine(String json)
    {
        Gson gson = new Gson();
        this.sso = gson.fromJson(json, SerializableStateObservation.class);
//        String data = gson.fromJson(gson, String.class);
//
//        for (String act : availableActions)
//            avatar.actionList.add(act);
//
//        for (String r : avatarResources)
//        {
//            int key = Integer.parseInt(r.split(",")[0]);
//            int val = Integer.parseInt(r.split(",")[1]);
//            avatar.resources.put(key, val);
//        }
    }

}

