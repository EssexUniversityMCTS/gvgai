import ontology.Types;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Daniel
 * Date: 16/03/17
 * Time: 21:54
 */
public class SerializableStateObservation {

    public enum State{
        INIT_STATE, ACT_STATE, END_STATE
    }

    public long elapsedTimer;
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
    public State gameState;

    public ArrayList<Types.ACTIONS> availableActions;
    public HashMap<Integer, Integer> avatarResources;
    public ArrayList<Observation>[][] observationGrid;
    //public TreeSet<Event> eventsHistory;
    public ArrayList<Observation>[] NPCPositions;
    public ArrayList<Observation>[] immovablePositions;
    public ArrayList<Observation>[] movablePositions;
    public ArrayList<Observation>[] resourcesPositions;
    public ArrayList<Observation>[] portalsPositions;
    public ArrayList<Observation>[] fromAvatarSpritesPositions;

    public SerializableStateObservation() {}

}

