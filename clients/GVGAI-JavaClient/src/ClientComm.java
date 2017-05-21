import agents.PlayerAgent;
import com.google.gson.Gson;
import ontology.Game;
import ontology.Avatar;
import serialization.SerializableStateObservation;

import java.io.*;
import java.util.Random;

/**
 * Created by Daniel on 04/03/2017.
 */
public class ClientComm {

    public static enum COMM_STATE {
        START, INIT, ACT, ABORT, ENDED, CHOOSE
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
     * Writer of the player. Used to pass the action of the player to the server.
     */
    public static PrintWriter fileOutput;

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
     * State information
     */
    public SerializableStateObservation sso;

    /**
     * Variable to store the player's agent information
     */
    public PlayerAgent player;

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
        player = new PlayerAgent();
        sso = new SerializableStateObservation();
    }

    /**
     * Creates communication buffers and starts listening.
     */
    public void start()
    {
        initBuffers();

        // Comment this for testing purposes
        try {
            listen();
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    /***
     * Method that perpetually listens for messages from the server.
     * With the use of additional helper methods, this function interprets
     * messages and represents the core response-generation methodology of the agent.
     * @throws IOException
     */
    private void listen() throws IOException {
        String line = "start client";
        writeToFile(line);

        int messageIdx = 0;

        // Continuously listen for messages
        while (line != null) {
            // Read a line from System.in and save it as a String
            line = input.readLine();

            writeToFile("going to processing");

            // Process the line
            processLine(line);

            // Generate a communication state based on the processed line
            // This will influence the further action to be taken by the client.
            commState = processCommandLine();

            // Decide on the further action to perform, based on the communication state,
            // as deciphered from the received line
            if(commState == COMM_STATE.START)
            {
                // Perform one-time startup initialization here
                writeToFile("beginning start");
                player.START();
                writeToFile("start done");
                writeToServer("START_DONE");

            }else if(commState == COMM_STATE.INIT)
            {
                // Perform level-entry initialization here
                player.INIT();
                writeToFile("init done");
                writeToServer("INIT_DONE");

            }else if(commState == COMM_STATE.ACT)
            {
                //This is the place to think and return what action to take.
                ElapsedCpuTimer ect = new ElapsedCpuTimer();

                // Save the player's action in a string
                String action = player.ACT(sso).toString();

                writeToFile("action: " + action + " " + ect.elapsedMillis());
                writeToServer(action);

            }else if(commState == COMM_STATE.CHOOSE)
            {
                //This is the place to pick a level to be played after the initial 2 levels have gone through
                Integer message = player.CHOOSE(sso);
                writeToServer(message.toString());

            }else if(commState == COMM_STATE.ABORT)
            {
                // Perform abort-state actions (such as teardown) here
                player.ABORT(sso);

                writeToFile("game aborted");
                writeToServer("GAME_DONE_ABORT");
            }else if(commState == COMM_STATE.ENDED)
            {
                // Perform end-state actions (such as teardown) here
                player.END(sso);

                writeToFile("game ended");
                writeToServer("GAME_DONE_ENDED");
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
            fileOutput = new PrintWriter(new File("logs/clientLog.txt"), "utf-8");

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

    /**
     * Writes a line to the client debug file, adding a line separator at the end.
     * @param line to write
     */
    private void writeToFile(String line) throws IOException{
        fileOutput.write(line + lineSep);
        fileOutput.flush();
    }

    /***
     * This method interprets the game-state as received from the server,
     * then decides what the appropriate client state is supposed to be
     * and returns that state, for further actions.
     * @return The state directly related to the state of the server
     * @throws IOException
     */
    public COMM_STATE processCommandLine() throws IOException {
        if(sso.gameState == SerializableStateObservation.State.START_STATE)
        {
            writeToFile("game is in start state");
            game.remMillis = sso.elapsedTimer;
            return COMM_STATE.START;

        }if(sso.gameState == SerializableStateObservation.State.INIT_STATE)
        {
            writeToFile("game is in init state");
            game.remMillis = sso.elapsedTimer;
            return COMM_STATE.INIT;

        }else if(sso.gameState == SerializableStateObservation.State.ACT_STATE) {
            game.remMillis = sso.elapsedTimer;
            return COMM_STATE.ACT;

        }else if(sso.gameState == SerializableStateObservation.State.CHOOSE_LEVEL) {
            game.remMillis = sso.elapsedTimer;
            return COMM_STATE.CHOOSE;

        }else if(sso.gameState == SerializableStateObservation.State.ABORT_STATE) {
            game.remMillis = sso.elapsedTimer;
            return COMM_STATE.ABORT;

        }else if(sso.gameState == SerializableStateObservation.State.END_STATE) {
            game.remMillis = sso.elapsedTimer;
            return COMM_STATE.ENDED;

        }

        return commState;
    }

    /***
     * Method that interprets the received messages from the server's side.
     * A message can either be a string (in the case of initialization), or
     * a json object containing an encapsulated state observation.
     * This method deserializes the json object into a local state observation
     * instance.
     * @param json Message received from server to be interpreted.
     * @throws IOException
     */
    public void processLine(String json) throws IOException{
        writeToFile("initializing gson");
        try {
            //Gson gson = new GsonBuilder().registerTypeAdapterFactory(new ArrayAdapterFactory()).create();
            Gson gson = new Gson();

            // Debug line
            //fileOutput.write(json);

            // Set the state to "START_STATE" in case the connexion (not game) is in the initialization phase.
            // Happens only on one-time setup
            if (json.equals("START")){
                this.sso.gameState = SerializableStateObservation.State.START_STATE;
                return;
            }

            //writeToFile(json);

            // Else, deserialize the json using GSon
            ElapsedCpuTimer cpu = new ElapsedCpuTimer();
            this.sso = gson.fromJson(json, SerializableStateObservation.class);
            writeToFile("gson initialized " + cpu.elapsedMillis());
        } catch (Exception e){
            e.printStackTrace(fileOutput);
        }

    }

}

