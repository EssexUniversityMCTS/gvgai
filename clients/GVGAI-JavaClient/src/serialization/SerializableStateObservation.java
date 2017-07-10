package serialization;

import utils.Termination;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Daniel
 * Date: 16/03/17
 * Time: 21:54
 */
public class SerializableStateObservation {

    public enum Phase {
        START, INIT, ACT, ABORT, END, FINISH
    }

    // Game variables
    public int[] spriteOrder;
    public boolean[] singletons;
    public Integer[][] iSubTypesArray;
    public HashMap<Character, ArrayList<String>> charMapping;
    public ArrayList<Termination> terminations;
    public int[] resources_limits;
    public Color[] resources_colors;
    public boolean is_stochastic;
    public int num_sprites;
    public int nextSpriteID;

    /**
     * Indicates the state of the protocol
     */
    public Phase phase;

    /**
     * Indicates if the game being played is validation or training.
     */
    public boolean isValidation;

    /**
     * Game Phase
     */
    public float gameScore;
    public int gameTick;
    public Types.WINNER gameWinner;
    public boolean isGameOver;
    public double[] worldDimension;
    public int blockSize;

    /**
     * Avatar Phase
     */
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
    public ArrayList<Types.ACTIONS> availableActions;
    public HashMap<Integer, Integer> avatarResources;

    /**
     * Observations of the world.
     */
    public Observation[][][] observationGrid;
    public Observation[][] NPCPositions;
    public Observation[][] immovablePositions;
    public Observation[][] movablePositions;
    public Observation[][] resourcesPositions;
    public Observation[][] portalsPositions;
    public Observation[][] fromAvatarSpritesPositions;

    //Default constructor.
    public SerializableStateObservation() {}


}

