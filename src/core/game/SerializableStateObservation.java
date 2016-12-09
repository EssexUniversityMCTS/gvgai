package core.game;

import com.google.gson.Gson;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 13/11/13
 * Time: 15:37
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class SerializableStateObservation {

    public ArrayList<Types.ACTIONS> availableActions;
    public float gameScore;
    public int gameTick;
    public Types.WINNER gameWinner;
    public boolean isGameOver;
    public Dimension worldDimension;
    public int blockSize;
    public float avatarSpeed;
    public Vector2d avatarOrientation;
    public HashMap<Integer, Integer> avatarResources;
    public Types.ACTIONS avatarLastAction;
    public int avatarType;
    public int avatarHealthPoints;
    public int avatarMaxHealthPoints;
    public int avatarLimitHealthPoints;
    public boolean isAvatarAlive;
    public ArrayList<Observation>[][] observationGrid;
    public TreeSet<Event> eventsHistory;
    public ArrayList<Observation>[] NPCPositions;
    public ArrayList<Observation>[] immovablePositions;
    public ArrayList<Observation>[] movablePositions;
    public ArrayList<Observation>[] resourcesPositions;
    public ArrayList<Observation>[] portalsPositions;
    public ArrayList<Observation>[] fromAvatarSpritesPositions;

    public SerializableStateObservation(StateObservation s)
    {
        availableActions = s.getAvailableActions();
        gameScore = (float) s.getGameScore();
        gameTick = s.getGameTick();
        gameWinner = s.getGameWinner();
        isGameOver = s.isGameOver();
        worldDimension = s.getWorldDimension();
        blockSize = s.getBlockSize();
        avatarSpeed = (float) s.getAvatarSpeed();
        avatarOrientation = s.getAvatarOrientation();
        avatarResources = s.getAvatarResources();
        avatarLastAction = s.getAvatarLastAction();
        avatarType = s.getAvatarType();
        avatarHealthPoints = s.getAvatarHealthPoints();
        avatarMaxHealthPoints = s.getAvatarMaxHealthPoints();
        avatarLimitHealthPoints = s.getAvatarLimitHealthPoints();
        isAvatarAlive = s.isAvatarAlive();
        observationGrid = null; //s.getObservationGrid();
        eventsHistory = s.getEventsHistory();
        NPCPositions = s.getNPCPositions();
        immovablePositions = s.getImmovablePositions();
        movablePositions = s.getMovablePositions();
        resourcesPositions = s.getResourcesPositions();
        portalsPositions = s.getPortalsPositions();
        fromAvatarSpritesPositions = s.getFromAvatarSpritesPositions();
    }

    public Gson serialize(String filename)
    {
        Gson gson;
        gson = new Gson();
        if(filename == null)
        {
            gson.toJson(this);
        }else{
            try{
                gson.toJson(this, new FileWriter(filename));
            }catch (Exception e){}
        }

        return gson;
    }


}
