package core.game;

import core.competition.CompetitionParameters;
import tools.com.google.gson.Gson;
import ontology.Types;
import tools.ElapsedCpuTimer;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 13/11/13
 * Time: 15:37
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class SerializableStateObservation {
    public enum Phase {
        START, INIT, ACT, ABORT, END
    }

    // State Observation variables
    public byte[] imageArray;
    public boolean isValidation;
    public float gameScore;
    public int gameTick;
    public Types.WINNER gameWinner;
    public boolean isGameOver;
    public double[] worldDimension;
    public int blockSize;
    public int noOfPlayers;
    public float avatarSpeed;
    public double[] avatarOrientation;
    public double[] avatarPosition;
    public Types.ACTIONS avatarLastAction;
    public int avatarType;
    public int avatarHealthPoints;
    public int avatarMaxHealthPoints;
    public int avatarLimitHealthPoints;
    public boolean isAvatarAlive;
    public Phase phase;

    public ArrayList<Types.ACTIONS> availableActions;
    public HashMap<Integer, Integer> avatarResources;
    public Observation[][][] observationGrid;
    public Observation[][] NPCPositions;
    public Observation[][] immovablePositions;
    public Observation[][] movablePositions;
    public Observation[][] resourcesPositions;
    public Observation[][] portalsPositions;
    public Observation[][] fromAvatarSpritesPositions;

    public SerializableStateObservation(StateObservation s, Boolean both){
        try {
            if (!both) {
                // Fill in the persistent variables (Score, tick)
                buildGameData(s);

                // Create the image bytearray
                imageArray = imageToByteArray();
            } else {
                // Fill in the persistent variables (Score, tick)
                buildGameData(s);

                // Create the image bytearray
                imageArray = imageToByteArray();

                // Fill in the simple data variables
                buildDataVariables(s);

                // Fill in the data array lists
                buildDataArraylists(s);
            }
        }catch(IOException e){
            System.out.println("Transforming image to byte array failed. Original error: " + e);
        }
    }

    public SerializableStateObservation(StateObservation s)
    {
        // Fill in the persistent variables (Score, tick)
        buildGameData(s);

        // Fill in the simple data variables
        buildDataVariables(s);

        // Fill in the data array lists
        buildDataArraylists(s);
    }

    private void buildGameData(StateObservation s){
        setPhase(s.getGameState());
        availableActions = s.getAvailableActions();
        gameScore = (float) s.getGameScore();
        gameTick = s.getGameTick();
        gameWinner = s.getGameWinner();
        isGameOver = s.isGameOver();
    }

    private void buildDataVariables(StateObservation s){
        worldDimension = new double[2];
        worldDimension[0] = s.getWorldDimension().getWidth();
        worldDimension[1] = s.getWorldDimension().getHeight();

        blockSize = s.getBlockSize();
        avatarSpeed = (float) s.getAvatarSpeed();

        avatarOrientation = new double[2];
        avatarOrientation[0] = s.getAvatarOrientation().x;
        avatarOrientation[1] = s.getAvatarOrientation().y;

        avatarPosition = new double[2];
        avatarPosition[0] = s.getAvatarPosition().x;
        avatarPosition[1] = s.getAvatarPosition().y;

        noOfPlayers = s.getNoPlayers();

        avatarResources = s.getAvatarResources();
        avatarLastAction = s.getAvatarLastAction();
        avatarType = s.getAvatarType();
        avatarHealthPoints = s.getAvatarHealthPoints();
        avatarMaxHealthPoints = s.getAvatarMaxHealthPoints();
        avatarLimitHealthPoints = s.getAvatarLimitHealthPoints();
        isAvatarAlive = s.isAvatarAlive();
    }

    private void buildDataArraylists(StateObservation s){
        ElapsedCpuTimer ect = new ElapsedCpuTimer();

        // Create a row to be used for translation from ArrayList to array
        ArrayList<Observation> row;

        /*
        * The following block is a sequence of iterative attributions
        * that render the game-information holding array objects
        * to be sent and interpreted by the agent.
        */

        // Observation grid
        if (s.getObservationGrid()!=null) {
            observationGrid = new Observation[s.getObservationGrid().length][s.getObservationGrid()[0].length][];

            for (int i = 0; i < s.getObservationGrid().length; i++) {
                for (int j = 0; j < s.getObservationGrid()[i].length; j++) {
                    row = s.getObservationGrid()[i][j];
                    if (row == null) {
                        observationGrid[i][j] = new Observation[0];
                    } else {
                        observationGrid[i][j] = row.toArray(new Observation[row.size()]);
                    }
                }
            }
        }

        // NPC positions
        if (s.getNPCPositions()!=null) {
            NPCPositions = new Observation[s.getNPCPositions().length][];

            for (int i = 0; i < s.getNPCPositions().length; i++) {
                row = s.getNPCPositions()[i];
                NPCPositions[i] = row.toArray(new Observation[row.size()]);
            }
        }

        // Immovable positions
        if (s.getImmovablePositions()!=null) {
            immovablePositions = new Observation[s.getImmovablePositions().length][];

            for (int i = 0; i < s.getImmovablePositions().length; i++) {
                row = s.getImmovablePositions()[i];
                immovablePositions[i] = row.toArray(new Observation[row.size()]);
            }
        }

        // Movable positions
        if(s.getMovablePositions()!=null) {
            movablePositions = new Observation[s.getMovablePositions().length][];

            for (int i = 0; i < s.getMovablePositions().length; i++) {
                row = s.getMovablePositions()[i];
                movablePositions[i] = row.toArray(new Observation[row.size()]);
            }
        }

        // Resource position
        if(s.getResourcesPositions()!=null) {
            resourcesPositions = new Observation[s.getResourcesPositions().length][];

            for (int i = 0; i < s.getResourcesPositions().length; i++) {
                row = s.getResourcesPositions()[i];
                resourcesPositions[i] = row.toArray(new Observation[row.size()]);
            }
        }

        // Portal position
        if(s.getPortalsPositions()!=null) {
            portalsPositions = new Observation[s.getPortalsPositions().length][];

            for (int i = 0; i < s.getPortalsPositions().length; i++) {
                row = s.getPortalsPositions()[i];
                portalsPositions[i] = row.toArray(new Observation[row.size()]);
            }
        }

        // Avatar sprite position
        if(s.getFromAvatarSpritesPositions()!=null) {
            fromAvatarSpritesPositions = new Observation[s.getFromAvatarSpritesPositions().length][];

            for (int i = 0; i < s.getFromAvatarSpritesPositions().length; i++) {
                row = s.getFromAvatarSpritesPositions()[i];
                fromAvatarSpritesPositions[i] = row.toArray(new Observation[row.size()]);
            }
        }
        //System.out.println(ect.elapsedMillis() + " ms taken to build SSO");
    }



    public static byte[] imageToByteArray() throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(ImageIO.read(new File(CompetitionParameters.SCREENSHOT_PATH)), "png", output);
        return output.toByteArray();
    }

    /***
     * This method serializes this class into a cohesive json object, using GSon,
     * and optionally saves the converted object to a given file.
     * @param filename Name of the file to save the serialization to (optional)
     * @return
     */
    public String serialize(String filename)
    {
        String message = "";
        Gson gson = new Gson();
        if(filename == null)
        {
            message = gson.toJson(this);
        }else{
            try{
                message = gson.toJson(this);
                gson.toJson(this, new FileWriter(filename));
            }catch (Exception e){}
        }

        return message;
    }

    public void setPhase(Types.GAMESTATES currentGameState) {
        if (currentGameState.equals(Types.GAMESTATES.INIT_STATE)) {
            phase = Phase.INIT;
        } else if (currentGameState.equals(Types.GAMESTATES.ACT_STATE)) {
            phase = Phase.ACT;
        } else if (currentGameState.equals(Types.GAMESTATES.ABORT_STATE)) {
            phase = Phase.ABORT;
        } else if (currentGameState.equals(Types.GAMESTATES.END_STATE)) {
            phase = Phase.END;
        } else {
            phase = Phase.START;
        }
    }

    @Override
    public String toString() {
        String observation = "ObservationGrid{\n";
        if (observationGrid != null) {
            for (int i = 0; i < observationGrid.length; i++) {
                for (int j = 0; j < observationGrid[i].length; j++) {
                    for (Observation obs : observationGrid[i][j]) {
                        observation += obs.toString();
                    }
                }
            }
        }
        observation += "}";

        return "SerializableStateObservation{" +
                "imageArray=" + Arrays.toString(imageArray) +
                ", isValidation=" + isValidation +
                ", gameScore=" + gameScore +
                ", gameTick=" + gameTick +
                ", gameWinner=" + gameWinner +
                ", isGameOver=" + isGameOver +
                ", worldDimension=" + Arrays.toString(worldDimension) +
                ", blockSize=" + blockSize +
                ", noOfPlayers=" + noOfPlayers +
                ", avatarSpeed=" + avatarSpeed +
                ", avatarOrientation=" + Arrays.toString(avatarOrientation) +
                ", avatarPosition=" + Arrays.toString(avatarPosition) +
                ", avatarLastAction=" + avatarLastAction +
                ", avatarType=" + avatarType +
                ", avatarHealthPoints=" + avatarHealthPoints +
                ", avatarMaxHealthPoints=" + avatarMaxHealthPoints +
                ", avatarLimitHealthPoints=" + avatarLimitHealthPoints +
                ", isAvatarAlive=" + isAvatarAlive +
                ", phase=" + phase +
                ", availableActions=" + availableActions +
                ", avatarResources=" + avatarResources +
                ", observationGrid=" + Arrays.toString(observationGrid) +
                ", NPCPositions=" + Arrays.toString(NPCPositions) +
                ", immovablePositions=" + Arrays.toString(immovablePositions) +
                ", movablePositions=" + Arrays.toString(movablePositions) +
                ", resourcesPositions=" + Arrays.toString(resourcesPositions) +
                ", portalsPositions=" + Arrays.toString(portalsPositions) +
                ", fromAvatarSpritesPositions=" + Arrays.toString(fromAvatarSpritesPositions) +
                "}\n" + observation;
    }
}
