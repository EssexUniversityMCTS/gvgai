package utils;

import serialization.SerializableStateObservation;
import serialization.Types.LEARNING_SSO_TYPE;
import utils.com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Constructor;

/**
 *  -----  DO NOT MODIFY THIS CLASS -----
 */


/**
 * Created by Daniel on 04/03/2017.
 */
public class ClientComm {

    /**
     * Handles writing to pipes and files.
     */
    private IO io;

    /**
     * Phase information
     */
    public SerializableStateObservation sso;

    /**
     * Variable to store the player's agent information
     */
    public AbstractPlayer player;

    /**
     * Global timer.
     */
    private ElapsedCpuTimer global_ect;

    /**
     * Special character to separate message ID from actual message
     */
    public static String TOKEN_SEP = "#";

    /**
     * If true, all messages sent to server are also printed to the log file
     */
    private boolean LOG = false;

    /**
     * Last Message ID received.
     */
    private long lastMessageId;

    /**
     * Name of the agent to run
     */
    private String agentName;

    /**
     * Type of last required sso
     */
    private LEARNING_SSO_TYPE lastSsoType = LEARNING_SSO_TYPE.JSON;

    /**
     * Creates the client.
     */
    public ClientComm(String agentName) {

        io = CompetitionParameters.USE_SOCKETS ? new IOSocket(CompetitionParameters.SOCKET_PORT) : new IOPipe();
        sso = new SerializableStateObservation();
        this.agentName = agentName;
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
            io.logStackTrace(e);
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
            line = io.readLine();

            // Process the line
            processLine(line);
            //io.writeToFile("line: " + line);

            if(sso.phase == SerializableStateObservation.Phase.START)
            {
                //io.writeToFile(lastMessageId + "#in start");
                this.start();

            }if(sso.phase == SerializableStateObservation.Phase.INIT)
            {

                //io.writeToFile(lastMessageId + "#in init");
                this.init();

            }else if(sso.phase == SerializableStateObservation.Phase.ACT) {
                this.act();

            }else if( (sso.phase == SerializableStateObservation.Phase.ABORT) ||
                (sso.phase == SerializableStateObservation.Phase.END) ){

//                io.writeToFile(lastMessageId + "#in result");
                this.result();

            }else if(sso.phase == SerializableStateObservation.Phase.FINISH) {
                line = null; //That's it.

            } else {
                io.writeToServer(lastMessageId, "null", LOG);
            }

        }
    }


    /***
     * Method that interprets the received messages from the server's side.
     * A message can either be a string (in the case of initialization), or
     * a json object containing an encapsulated state observation.
     * This method deserializes the json object into a local state observation
     * instance.
     * @param msg Message received from server to be interpreted.
     * @throws IOException
     */
    public void processLine(String msg) throws IOException{

        try {
            //Separate ID and message:
            if (msg == null) {
                System.err.println("ClientComm: msg==null");
            }
            String message[] = msg.split(TOKEN_SEP);
            if (message.length < 2)
                return;

            lastMessageId = Integer.parseInt(message[0]);
            String json = message[1];

            //io.writeToFile("message received " + lastMessageId + "#" + json);

            Gson gson = new Gson();

            // Set the state to "START" in case the connexion (not game) is in the initialization phase.
            // Happens only on one-time setup
            if (json.equals("START")) {
                this.sso.phase = SerializableStateObservation.Phase.START;
                return;
            }

            if (json.equals("FINISH")) {
                this.sso.phase = SerializableStateObservation.Phase.FINISH;
                return;
            }

            // Else, deserialize the json using GSon
            this.sso = gson.fromJson(json, SerializableStateObservation.class);

            // If expect image
            if (lastSsoType == LEARNING_SSO_TYPE.IMAGE || lastSsoType == LEARNING_SSO_TYPE.BOTH) {
                if ((sso.phase != SerializableStateObservation.Phase.INIT
                    && sso.phase != SerializableStateObservation.Phase.ABORT)) {
                    // If an image has been received, then save its PNG equivalent
                    sso.convertBytesToPng(sso.imageArray);
                }
            }
            // Used for debugging
//            io.writeToFile(sso.toString());
        } catch (Exception e){
            io.logStackTrace(e);
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

        System.out.println("Starting to play [OK]");

        //Starts the agent (calls the constructor).
        startAgent();

        if(ect.exceededMaxTime())
        {
            io.writeToServer(lastMessageId, "START_FAILED", LOG);
        }else {
            //io.writeToFile("start done");
            io.writeToServer(lastMessageId, "START_DONE" + TOKEN_SEP + player.lastSsoType, LOG);
        }

    }

    private void startAgent()
    {
        try{
            Class<? extends AbstractPlayer> controllerClass = Class.forName(agentName).asSubclass(AbstractPlayer.class);
            Constructor controllerArgsConstructor = controllerClass.getConstructor();
            player = (AbstractPlayer) controllerArgsConstructor.newInstance();
            this.lastSsoType = player.lastSsoType;
        }catch (Exception e)
        {
            io.writeToFile(e.toString());
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
        this.lastSsoType = player.lastSsoType;
        if(ect.exceededMaxTime())
        {
            io.writeToServer(lastMessageId, "INIT_FAILED", LOG);
        }else {
            io.writeToServer(lastMessageId, "INIT_DONE"+TOKEN_SEP + player.lastSsoType, LOG);
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
        if (action == null || action == "") {
            action = "ACTION_NIL";
        }
        this.lastSsoType = player.lastSsoType;
        if(ect.exceededMaxTime()) {
//            System.out.println("spent:"+ect.elapsedMillis() + ">" + CompetitionParameters.ACTION_TIME_DISQ);
            if (ect.elapsedNanos() > CompetitionParameters.ACTION_TIME_DISQ*1000000.0) {
                io.writeToServer(lastMessageId, "END_OVERSPENT", LOG);
            } else {
                //Overspent.
                io.writeToServer(lastMessageId, "ACTION_NIL", LOG);
            }
        } else {
            io.writeToServer(lastMessageId, action + TOKEN_SEP + player.lastSsoType, LOG);
        }
    }

    /**
     * Manages the aresult sent to the agent. The time limit for this call will be TOTAL_LEARNING_TIME
     * or EXTRA_LEARNING_TIME if current global time is beyond TOTAL_LEARNING_TIME.
     * The agent is assumed to return the next level to play. It will be ignored if
     *    a) All training levels have not been played yet (in which case the starting sequence 0-1-2 continues).
     *    b) It's outside the range [0,4] (in which case we play one at sampleRandom)
     *    c) or we are in the validation phase (in which case the starting sequence 3-4 continues).
     */
    private void result()
    {
        ElapsedCpuTimer ect = new ElapsedCpuTimer();

        if(!global_ect.exceededMaxTime()) {
            ect = global_ect.copy();
        }
        else {
            ect.setMaxTimeMillis(CompetitionParameters.EXTRA_LEARNING_TIME);
        }
        // Submit result and wait for next level.
        int nextLevel = player.result(sso, ect.copy());
        this.lastSsoType = player.lastSsoType;
//        io.writeToFile("result timers: global: " + global_ect.elapsedSeconds()  + "(" + global_ect.exceededMaxTime() + ")" +
//                ", local: " + ect.elapsedSeconds() + "(" + ect.exceededMaxTime() + ")" );

//        System.out.println("result timers: global: " + global_ect.elapsedSeconds()  + "(" + global_ect.exceededMaxTime() + ")" +
//                ", local: " + ect.elapsedSeconds() + "(" + ect.exceededMaxTime() + ")" );

        if(ect.exceededMaxTime())
        {
            io.writeToServer(lastMessageId, "END_OVERSPENT", LOG);

        }else {

            if(global_ect.exceededMaxTime())
            {
                String end_message = sso.isValidation ? "END_VALIDATION" : "END_TRAINING";
                //Note this is okay, TOTAL_LEARNING_TIME is over, within the rules
                io.writeToServer(lastMessageId, end_message, LOG);
            }else {
                io.writeToServer(lastMessageId, nextLevel + TOKEN_SEP + player.lastSsoType, LOG);
            }
        }
    }


}
