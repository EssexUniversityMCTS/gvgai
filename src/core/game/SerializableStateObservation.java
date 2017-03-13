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
import java.util.Map;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 13/11/13
 * Time: 15:37
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
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
    public Dimension worldDimension;
    public int blockSize;
    public float avatarSpeed;
    public Vector2d avatarOrientation;
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

    public SerializableStateObservation(StateObservation s)
    {
        gameState = State.INIT_STATE;
        elapsedTimer = 0;
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
        observationGrid = s.getObservationGrid();
        NPCPositions = s.getNPCPositions();
        immovablePositions = s.getImmovablePositions();
        movablePositions = s.getMovablePositions();
        resourcesPositions = s.getResourcesPositions();
        portalsPositions = s.getPortalsPositions();
        fromAvatarSpritesPositions = s.getFromAvatarSpritesPositions();
    }

    public String serialize(String filename)
    {
        Gson gson;
        String message = "";
        gson = new Gson();
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

    public String toString() {
        StringBuffer sb = new StringBuffer();

        //gameScore
        sb.append("{\"gameScore\":");
        sb.append(gameScore);
        sb.append(",");
        //gameTick
        sb.append("\"gameTick\":");
        sb.append(gameTick);
        sb.append(",");
        //gameWinner
        sb.append("\"gameWinner\":");
        sb.append(gameWinner);
        sb.append(",");
        //isGameOver
        sb.append("\"isGameOver\":");
        sb.append(isGameOver);
        sb.append(",");
        //worldDimension
        sb.append("\"worldDimension\":");
        sb.append(worldDimension);
        sb.append(",");
        //blockSize
        sb.append("\"blockSize\":");
        sb.append(blockSize);
        sb.append(",");
        //avatarSpeed
        sb.append("\"avatarSpeed\":");
        sb.append(avatarSpeed);
        sb.append(",");
        //avatarOrientation
        sb.append("\"avatarOrientation\":");
        sb.append(avatarOrientation);
        sb.append(",");
        //avatarLastAction
        sb.append("\"avatarLastAction\":");
        sb.append(avatarLastAction);
        sb.append(",");
        //avatarType
        sb.append("\"avatarType\":");
        sb.append(avatarType);
        sb.append(",");
        //avatarHealthPoints
        sb.append("\"avatarHealthPoints\":");
        sb.append(avatarHealthPoints);
        sb.append(",");
        //avatarMaxHealthPoints
        sb.append("\"avatarMaxHealthPoints\":");
        sb.append(avatarMaxHealthPoints);
        sb.append(",");
        //avatarLimitHealthPoints
        sb.append("\"avatarLimitHealthPoints\":");
        sb.append(avatarLimitHealthPoints);
        sb.append(",");
        //isAvatarAlive
        sb.append("\"isAvatarAlive\":");
        sb.append(isAvatarAlive);
        sb.append(",");

        //availableActions
        sb.append("\"availableActions\":[");
        for (Types.ACTIONS action :availableActions) {
            sb.append("\""+action+"\",");
        }
        sb.append("],");

//        HashMap<Integer,Integer> avatarResources;
        //avatarResources
        sb.append("\"avatarResources\":[");
        if (avatarResources==null) {
            sb.append("[]],");
        } else {
            String str = "";
            for (Map.Entry<Integer, Integer> entry : avatarResources.entrySet()) {
                Integer key = entry.getKey();
                Integer value = entry.getValue();
                str += "[" + key + "," + value + "],";
            }
            if (str.endsWith(",")) {
                str = str.substring(0, str.length() - 1);
            }
            sb.append(str);
            sb.append("],");
        }
        //NPCpositions
        sb.append("\"NPCpositions\":[");
        if (NPCPositions==null) {
            sb.append("[]],");
        } else {
            String str = "";
            for (int i = 0; i < this.NPCPositions.length; i++) {
                for (Observation npc : NPCPositions[i]) {
                    str += npc.toString() + ",";
                }
            }
            if (str.endsWith(",")) {
                str = str.substring(0, str.length() - 1);
            }
            sb.append(str);
            sb.append("],");
        }

        //immovablePositions
        sb.append("\"immovablePositions\":[");
        if (immovablePositions==null) {
            sb.append("[]],");
        } else {
            String str = "";
            for (int i = 0; i < this.immovablePositions.length; i++) {
                for (Observation npc : immovablePositions[i]) {
                    str += npc.toString() + ",";
                }
            }
            if (str.endsWith(",")) {
                str = str.substring(0, str.length() - 1);
            }
            sb.append(str);
            sb.append("],");
        }

        //movablePositions
        sb.append("\"movablePositions\":[");
        if (movablePositions==null) {
            sb.append("[]],");
        } else {
            String str = "";
            for (int i = 0; i < this.movablePositions.length; i++) {
                for (Observation npc : movablePositions[i]) {
                    str += npc.toString() + ",";
                }
            }
            if (str.endsWith(",")) {
                str = str.substring(0, str.length() - 1);
            }
            sb.append(str);
            sb.append("],");
        }

        //resourcesPositions
        sb.append("\"resourcesPositions\":[");
        if (resourcesPositions==null) {
            sb.append("[]],");
        } else {
            String str = "";
            for (int i = 0; i < this.resourcesPositions.length; i++) {
                for (Observation npc : resourcesPositions[i]) {
                    str += npc.toString() + ",";
                }
            }
            if (str.endsWith(",")) {
                str = str.substring(0, str.length() - 1);
            }
            sb.append(str);
            sb.append("],");
        }

        //portalsPositions
        sb.append("\"portalsPositions\":[");
        if (portalsPositions==null) {
            sb.append("[]],");
        } else {
            String str = "";
            for (int i = 0; i < this.portalsPositions.length; i++) {
                for (Observation npc : portalsPositions[i]) {
                    str += npc.toString() + ",";
                }
            }
            if (str.endsWith(",")) {
                str = str.substring(0, str.length() - 1);
            }
            sb.append(str);
            sb.append("],");
        }

        //fromAvatarSpritesPositions
        sb.append("\"fromAvatarSpritesPositions\":[");
        if (fromAvatarSpritesPositions==null) {
            sb.append("[]]}");
        } else {
            String str = "";
            for (int i = 0; i < this.fromAvatarSpritesPositions.length; i++) {
                for (Observation npc : fromAvatarSpritesPositions[i]) {
                    str += npc.toString() + ",";
                }
            }
            if (str.endsWith(",")) {
                str = str.substring(0, str.length() - 1);
            }
            sb.append(str);
            sb.append("]}");
        }
        return sb.toString();
    }

}
