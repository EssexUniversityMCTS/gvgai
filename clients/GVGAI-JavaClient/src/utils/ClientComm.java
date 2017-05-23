package utils;

import agents.random.Agent;
import com.google.gson.Gson;
import serialization.SerializableStateObservation;

import java.io.*;

/**
 *  -----  DO NOT MODIFY THIS CLASS -----
 */


/**
 * Created by Daniel on 04/03/2017.
 */
public class ClientComm {

    private IO io;

    /**
     * State information
     */
    public SerializableStateObservation sso;

    /**
     * Variable to store the player's agent information
     */
    public Agent player;

    /**
     * Global timer.
     */
    private ElapsedCpuTimer global_ect;


    /**
     * Creates the client.
     */
    public ClientComm() {
        io = new IO();
        sso = new SerializableStateObservation();
    }


    /**
     * Creates communication buffers and starts listening.
     */
    public void startComm()
    {
        io.initBuffers();
        try {
            listen();
        } catch (Exception e) {
            System.out.println(e);
            io.writeToFile(e.toString());
        }
    }

    /***
     * Method that perpetually listens for messages from the server.
     * With the use of additional helper methods, this function interprets
     * messages and represents the core response-generation methodology of the agent.
     * @throws IOException
     */
    private void listen() throws IOException {
        String line = "";

        // Continuously listen for messages
        while (line != null) {

            // Read a line from System.in and save it as a String
            line = io.input.readLine();
            //io.writeToFile(line);

            // Process the line
            processLine(line);


            if(sso.gameState == SerializableStateObservation.State.START_STATE)
            {
                this.start();

            }if(sso.gameState == SerializableStateObservation.State.INIT_STATE)
            {

                this.init();

            }else if(sso.gameState == SerializableStateObservation.State.ACT_STATE) {

                this.act();

            }else if( (sso.gameState == SerializableStateObservation.State.ABORT_STATE) ||
                      (sso.gameState == SerializableStateObservation.State.END_STATE) ){

                this.result();

            } else {
                io.writeToServer("null");
            }

        }
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
        //io.writeToFile("initializing gson");
        try {
            //Gson gson = new GsonBuilder().registerTypeAdapterFactory(new ArrayAdapterFactory()).create();
            Gson gson = new Gson();

            // Set the state to "START_STATE" in case the connexion (not game) is in the initialization phase.
            // Happens only on one-time setup
            if (json.equals("START")){
                this.sso.gameState = SerializableStateObservation.State.START_STATE;
                return;
            }

            //io.writeToFile(json);

            // Else, deserialize the json using GSon
            ElapsedCpuTimer cpu = new ElapsedCpuTimer();
            this.sso = gson.fromJson(json, SerializableStateObservation.class);
            //io.writeToFile("gson initialized " + cpu.elapsedMillis());
        } catch (Exception e){
            e.printStackTrace(io.fileOutput);
        }

    }


    /// Specific state functions

    /**
     * Manages the start of the communication. It starts the whole process, and sets up the timer for the whole run.
     */
    private void start()
    {
        //This marks the start of the learning time.
        global_ect = new ElapsedCpuTimer();
        global_ect.setMaxTimeMillis(CompetitionParameters.TOTAL_LEARNING_TIME);

        ElapsedCpuTimer ect = new ElapsedCpuTimer();
        ect.setMaxTimeMillis(CompetitionParameters.START_TIME);

        //Starts the agent.
        player = new Agent();

        if(ect.exceededMaxTime())
        {
            io.writeToServer("START_FAILED");
        }else {
            //io.writeToFile("start done");
            io.writeToServer("START_DONE");
        }

    }


    /**
     * Manages the init of a game played.
     */
    private void init()
    {
        ElapsedCpuTimer ect = new ElapsedCpuTimer();
        ect.setMaxTimeMillis(CompetitionParameters.INITIALIZATION_TIME);

        // Perform level-entry initialization here
        player.init(sso, ect.copy());
        //io.writeToFile("init done");

        if(ect.exceededMaxTime())
        {
            io.writeToServer("INIT_FAILED");
        }else {
            io.writeToServer("INIT_DONE");
        }
    }


    /**
     * Manages the action request for an agent. The agent is requested for an action,
     * which is sent back to the server
     */
    private void act()
    {
        ElapsedCpuTimer ect = new ElapsedCpuTimer();
        ect.setMaxTimeMillis(CompetitionParameters.ACTION_TIME);

        // Save the player's action in a string
        String action = player.act(sso, ect.copy()).toString();
        //io.writeToFile("init done");

        if(ect.exceededMaxTime())
        {
            //Overspent. Server (MovingAvatar) will take care of disqualifications.
            io.writeToServer("ACTION_NIL");
        }else {
            io.writeToServer(action);
        }
    }



    /**
     * Manages the aresult sent to the agent. The time limit for this call will be TOTAL_LEARNING_TIME
     * or EXTRA_LEARNING_TIME if current global time is beyond TOTAL_LEARNING_TIME.
     * The agent is assumed to return the next level to play. It will be ignored if
     *    a) All training levels have not been played yet (in which case the starting sequence 0-1-2 continues).
     *    b) It's outside the range [0,4] (in which case we play one at random)
     *    c) or we are in the validation phase (in which case the starting sequence 3-4 continues).
     */
    private void result()
    {
        ElapsedCpuTimer ect = new ElapsedCpuTimer();

        if(!global_ect.exceededMaxTime())
            ect = global_ect.copy();
        else
            ect.setMaxTimeMillis(CompetitionParameters.EXTRA_LEARNING_TIME);

        // Submit result and wait for next level.
        int nextLevel = player.result(sso, ect.copy());
        //io.writeToFile("init done");

        if(ect.exceededMaxTime())
        {
            io.writeToServer("END_OVERSPENT");
        }else {

            if(global_ect.exceededMaxTime())
            {
                //Note this is okay, TOTAL_LEARNING_TIME is over, within the rules
                io.writeToServer("END_TRAINING");
            }else {
                io.writeToServer(nextLevel + "");
            }
        }
    }


}

