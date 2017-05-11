package core.game;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
    public Types.GAMESTATES gameState;

    public ArrayList<Types.ACTIONS> availableActions;
    public HashMap<Integer, Integer> avatarResources;
//    public ArrayList<Observation>[][] observationGrid;
    public Observation[][][] observationGridArray;
    public Observation[][] NPCPositionsArray;
    public Observation[][] immovablePositionsArray;
    public Observation[][] movablePositionsArray;
    public Observation[][] resourcesPositionsArray;
    public Observation[][] portalsPositionsArray;
    public Observation[][] fromAvatarSpritesPositionsArray;
    //public TreeSet<Event> eventsHistory;
//    public ArrayList<Observation>[] NPCPositions;
//    public ArrayList<Observation>[] immovablePositions;
//    public ArrayList<Observation>[] movablePositions;
//    public ArrayList<Observation>[] resourcesPositions;
//    public ArrayList<Observation>[] portalsPositions;
//    public ArrayList<Observation>[] fromAvatarSpritesPositions;

    public SerializableStateObservation(StateObservation s)
    {
        gameState = s.getGameState();
        elapsedTimer = 0;
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
//        observationGrid = s.getObservationGrid();

        // Create a row to be used for translation from ArrayList to array
        ArrayList<Observation> row;

        ElapsedCpuTimer ect = new ElapsedCpuTimer();

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

//        NPCPositions = s.getNPCPositions();
        // NPC positions
        if (s.getNPCPositions()!=null) {
            NPCPositionsArray = new Observation[s.getNPCPositions().length][];

            for (int i = 0; i < s.getNPCPositions().length; i++) {
                row = s.getNPCPositions()[i];
                NPCPositionsArray[i] = row.toArray(new Observation[row.size()]);
            }
        }

//        immovablePositions = s.getImmovablePositions();
        // Immovable positions
        if (s.getImmovablePositions()!=null) {
            immovablePositionsArray = new Observation[s.getImmovablePositions().length][];

            for (int i = 0; i < s.getImmovablePositions().length; i++) {
                row = s.getImmovablePositions()[i];
                immovablePositionsArray[i] = row.toArray(new Observation[row.size()]);
            }
        }

//        movablePositions = s.getMovablePositions();
        // Movable positions
        if(s.getMovablePositions()!=null) {
            movablePositionsArray = new Observation[s.getMovablePositions().length][];

            for (int i = 0; i < s.getMovablePositions().length; i++) {
                row = s.getMovablePositions()[i];
                movablePositionsArray[i] = row.toArray(new Observation[row.size()]);
            }
        }

//        resourcesPositions = s.getResourcesPositions();
        // Resource position
        if(s.getResourcesPositions()!=null) {
            resourcesPositionsArray = new Observation[s.getResourcesPositions().length][];

            for (int i = 0; i < s.getResourcesPositions().length; i++) {
                row = s.getResourcesPositions()[i];
                resourcesPositionsArray[i] = row.toArray(new Observation[row.size()]);
            }
        }

//        portalsPositions = s.getPortalsPositions();
        // Portal position
        if(s.getPortalsPositions()!=null) {
            portalsPositionsArray = new Observation[s.getPortalsPositions().length][];

            for (int i = 0; i < s.getPortalsPositions().length; i++) {
                row = s.getPortalsPositions()[i];
                portalsPositionsArray[i] = row.toArray(new Observation[row.size()]);
            }
        }

//        fromAvatarSpritesPositions = s.getFromAvatarSpritesPositions();
        // Avatar sprite position
        if(s.getFromAvatarSpritesPositions()!=null) {
            fromAvatarSpritesPositionsArray = new Observation[s.getFromAvatarSpritesPositions().length][];

            for (int i = 0; i < s.getFromAvatarSpritesPositions().length; i++) {
                row = s.getFromAvatarSpritesPositions()[i];
                fromAvatarSpritesPositionsArray[i] = row.toArray(new Observation[row.size()]);
            }
        }

        System.out.println(ect.elapsedMillis() + " taken");
    }

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
//        //NPCpositions
//        sb.append("\"NPCpositions\":[");
//        if (NPCPositions==null) {
//            sb.append("[]],");
//        } else {
//            String str = "";
//            for (int i = 0; i < this.NPCPositions.length; i++) {
//                for (Observation npc : NPCPositions[i]) {
//                    str += npc.toString() + ",";
//                }
//            }
//            if (str.endsWith(",")) {
//                str = str.substring(0, str.length() - 1);
//            }
//            sb.append(str);
//            sb.append("],");
//        }

        //immovablePositions
        sb.append("\"immovablePositions\":[");
//        if (immovablePositions==null) {
//            sb.append("[]],");
//        } else {
//            String str = "";
//            for (int i = 0; i < this.immovablePositions.length; i++) {
//                for (Observation npc : immovablePositions[i]) {
//                    str += npc.toString() + ",";
//                }
//            }
//            if (str.endsWith(",")) {
//                str = str.substring(0, str.length() - 1);
//            }
//            sb.append(str);
//            sb.append("],");
//        }

//        //movablePositions
//        sb.append("\"movablePositions\":[");
//        if (movablePositions==null) {
//            sb.append("[]],");
//        } else {
//            String str = "";
//            for (int i = 0; i < this.movablePositions.length; i++) {
//                for (Observation npc : movablePositions[i]) {
//                    str += npc.toString() + ",";
//                }
//            }
//            if (str.endsWith(",")) {
//                str = str.substring(0, str.length() - 1);
//            }
//            sb.append(str);
//            sb.append("],");
//        }
//
//        //resourcesPositions
//        sb.append("\"resourcesPositions\":[");
//        if (resourcesPositions==null) {
//            sb.append("[]],");
//        } else {
//            String str = "";
//            for (int i = 0; i < this.resourcesPositions.length; i++) {
//                for (Observation npc : resourcesPositions[i]) {
//                    str += npc.toString() + ",";
//                }
//            }
//            if (str.endsWith(",")) {
//                str = str.substring(0, str.length() - 1);
//            }
//            sb.append(str);
//            sb.append("],");
//        }
//
//        //portalsPositions
//        sb.append("\"portalsPositions\":[");
//        if (portalsPositions==null) {
//            sb.append("[]],");
//        } else {
//            String str = "";
//            for (int i = 0; i < this.portalsPositions.length; i++) {
//                for (Observation npc : portalsPositions[i]) {
//                    str += npc.toString() + ",";
//                }
//            }
//            if (str.endsWith(",")) {
//                str = str.substring(0, str.length() - 1);
//            }
//            sb.append(str);
//            sb.append("],");
//        }
//
//        //fromAvatarSpritesPositions
//        sb.append("\"fromAvatarSpritesPositions\":[");
//        if (fromAvatarSpritesPositions==null) {
//            sb.append("[]]}");
//        } else {
//            String str = "";
//            for (int i = 0; i < this.fromAvatarSpritesPositions.length; i++) {
//                for (Observation npc : fromAvatarSpritesPositions[i]) {
//                    str += npc.toString() + ",";
//                }
//            }
//            if (str.endsWith(",")) {
//                str = str.substring(0, str.length() - 1);
//            }
//            sb.append(str);
//            sb.append("]}");
//        }
        return sb.toString();
    }

}
