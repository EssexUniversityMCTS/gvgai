package serialization;

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
        START, INIT, ACT, ABORT, END
    }

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
    public float avatarSpeed;
    public double[] avatarOrientation;
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
    public ArrayList<Observation>[][] observationGrid;
    public ArrayList<Observation>[] NPCPositions;
    public ArrayList<Observation>[] immovablePositions;
    public ArrayList<Observation>[] movablePositions;
    public ArrayList<Observation>[] resourcesPositions;
    public ArrayList<Observation>[] portalsPositions;
    public ArrayList<Observation>[] fromAvatarSpritesPositions;

    //Default constructor.
    public SerializableStateObservation() {}

}

