package core.game;

import tools.com.google.gson.Gson;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.io.FileWriter;
import java.util.ArrayList;
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

    public boolean isValidation;
    public float gameScore;
    public int gameTick;
    public Types.WINNER gameWinner;
    public boolean isGameOver;
    public double[] worldDimension;
    public int blockSize;
    public float avatarSpeed;
    public double[] avatarOrientation;
    public Types.ACTIONS avatarLastAction;
    public int avatarType;
    public int avatarHealthPoints;
    public int avatarMaxHealthPoints;
    public int avatarLimitHealthPoints;
    public boolean isAvatarAlive;
    public Phase phase;

    public ArrayList<Types.ACTIONS> availableActions;
    public HashMap<Integer, Integer> avatarResources;
    public Observation[][][] observationGridArray;
    public Observation[][] NPCPositionsArray;
    public Observation[][] immovablePositionsArray;
    public Observation[][] movablePositionsArray;
    public Observation[][] resourcesPositionsArray;
    public Observation[][] portalsPositionsArray;
    public Observation[][] fromAvatarSpritesPositionsArray;

    public SerializableStateObservation(StateObservation s)
    {
        setPhase(s.getGameState());
        availableActions = s.getAvailableActions();
        gameScore = (float) s.getGameScore();
        gameTick = s.getGameTick();
        gameWinner = s.getGameWinner();
        isGameOver = s.isGameOver();

        worldDimension = new double[2];
        worldDimension[0] = s.getWorldDimension().getWidth();
        worldDimension[1] = s.getWorldDimension().getHeight();

        blockSize = s.getBlockSize();
        avatarSpeed = (float) s.getAvatarSpeed();

        avatarOrientation = new double[2];
        avatarOrientation[0] = s.getAvatarOrientation().x;
        avatarOrientation[1] = s.getAvatarOrientation().y;

        avatarResources = s.getAvatarResources();
        avatarLastAction = s.getAvatarLastAction();
        avatarType = s.getAvatarType();
        avatarHealthPoints = s.getAvatarHealthPoints();
        avatarMaxHealthPoints = s.getAvatarMaxHealthPoints();
        avatarLimitHealthPoints = s.getAvatarLimitHealthPoints();
        isAvatarAlive = s.isAvatarAlive();

        // Create a row to be used for translation from ArrayList to array
        ArrayList<Observation> row;

        ElapsedCpuTimer ect = new ElapsedCpuTimer();

        /*
        * The following block is a sequence of iterative attributions
        * that render the game-information holding array objects
        * to be sent and interpreted by the agent.
        */

        // Observation grid
        if (s.getObservationGrid()!=null) {
            observationGridArray = new Observation[s.getObservationGrid().length][s.getObservationGrid()[0].length][];

            for (int i = 0; i < s.getObservationGrid().length; i++) {
                for (int j = 0; j < s.getObservationGrid()[i].length; j++) {
                    row = s.getObservationGrid()[i][j];
                    observationGridArray[i][j] = row.toArray(new Observation[row.size()]);
                }
            }
        }

        // NPC positions
        if (s.getNPCPositions()!=null) {
            NPCPositionsArray = new Observation[s.getNPCPositions().length][];

            for (int i = 0; i < s.getNPCPositions().length; i++) {
                row = s.getNPCPositions()[i];
                NPCPositionsArray[i] = row.toArray(new Observation[row.size()]);
            }
        }

        // Immovable positions
        if (s.getImmovablePositions()!=null) {
            immovablePositionsArray = new Observation[s.getImmovablePositions().length][];

            for (int i = 0; i < s.getImmovablePositions().length; i++) {
                row = s.getImmovablePositions()[i];
                immovablePositionsArray[i] = row.toArray(new Observation[row.size()]);
            }
        }

        // Movable positions
        if(s.getMovablePositions()!=null) {
            movablePositionsArray = new Observation[s.getMovablePositions().length][];

            for (int i = 0; i < s.getMovablePositions().length; i++) {
                row = s.getMovablePositions()[i];
                movablePositionsArray[i] = row.toArray(new Observation[row.size()]);
            }
        }

        // Resource position
        if(s.getResourcesPositions()!=null) {
            resourcesPositionsArray = new Observation[s.getResourcesPositions().length][];

            for (int i = 0; i < s.getResourcesPositions().length; i++) {
                row = s.getResourcesPositions()[i];
                resourcesPositionsArray[i] = row.toArray(new Observation[row.size()]);
            }
        }

        // Portal position
        if(s.getPortalsPositions()!=null) {
            portalsPositionsArray = new Observation[s.getPortalsPositions().length][];

            for (int i = 0; i < s.getPortalsPositions().length; i++) {
                row = s.getPortalsPositions()[i];
                portalsPositionsArray[i] = row.toArray(new Observation[row.size()]);
            }
        }

        // Avatar sprite position
        if(s.getFromAvatarSpritesPositions()!=null) {
            fromAvatarSpritesPositionsArray = new Observation[s.getFromAvatarSpritesPositions().length][];

            for (int i = 0; i < s.getFromAvatarSpritesPositions().length; i++) {
                row = s.getFromAvatarSpritesPositions()[i];
                fromAvatarSpritesPositionsArray[i] = row.toArray(new Observation[row.size()]);
            }
        }

        //System.out.println(ect.elapsedMillis() + " ms taken to build SSO");
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
}
