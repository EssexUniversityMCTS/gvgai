package serialization;

import utils.CompetitionParameters;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

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

    /**
     * Holds the data for an image of the game
     */
    public byte[] imageArray;

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
    public SerializableStateObservation() {
        this.imageArray = null;
        this.phase = null;
        this.isValidation = true;
        this.gameScore = 0;
        this.gameTick = 0;
        this.gameWinner = null;
        this.isGameOver = false;
        this.worldDimension = null;
        this.blockSize = 0;
        this.noOfPlayers = 0;
        this.avatarSpeed = 0;
        this.avatarOrientation = null;
        this.avatarPosition = null;
        this.avatarLastAction = null;
        this.avatarType = 0;
        this.avatarHealthPoints = 0;
        this.avatarMaxHealthPoints = 0;
        this.avatarLimitHealthPoints = 0;
        this.isAvatarAlive = true;
        this.availableActions = null;
        this.avatarResources = null;
        this.observationGrid = null;
        this.NPCPositions = null;
        this.immovablePositions = null;
        this.movablePositions = null;
        this.resourcesPositions = null;
        this.portalsPositions = null;
        this.fromAvatarSpritesPositions = null;
    }

    // Optional, helper method to convert a byte array to PNG format
    public void convertBytesToPng(byte[] pixels) throws IOException, DataFormatException {
        if (pixels != null) {
            InputStream in = new ByteArrayInputStream(pixels);
            BufferedImage bImageFromConvert = ImageIO.read(in);

            ImageIO.write(bImageFromConvert, "PNG", new File(
                CompetitionParameters.SCREENSHOT_FILENAME));
        } else {
            System.err.println("SerializableStateObservation: convertBytesToPng: pixels is null.");
        }
    }

    // Helper method to decompress a byte array. Used by convertBytesToPng
    public byte[] decompress(byte[] data) throws IOException, DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();
        return output;
    }

    /**
     * Returns the actions that are available in this game for
     * the avatar.
     * @return the available actions.
     */
    public ArrayList<Types.ACTIONS> getAvailableActions()
    {
        return this.availableActions;
    }

    /**
     * Returns a list of observations of sprites created by the avatar (usually, by applying the
     * action Types.ACTIONS.ACTION_USE). As there can be sprites of different type, each entry in
     * the array corresponds to a sprite type. Every ArrayList contains a list of objects of
     * type Observation. Each Observation holds the position, unique id and sprite id
     * of that particular sprite.
     *
     * @return Observations of sprites the avatar created.
     */
    public Observation[][] getFromAvatarSpritesPositions() {
        return fromAvatarSpritesPositions;
    }

    /**
     * Returns the bytearray that the latest screenshot of the game consists of.
     * @return an array of bytes that compose a .png image.
     */
    public byte[] getImageArray() {
        return imageArray;
    }

    /**
     * Returns the current phase of the game, as stated in the enum above.
     * The phase of the game represents the state that the game is in,
     * and is used as a denominator to determine what the server expects
     * next as a response. For example, an action is expected in the ACT
     * state.
     * @return The game state under the form of a Phase-type element.
     */
    public Phase getPhase() {
        return phase;
    }

    /**
     * Returns whether the game is in validation mode.
     * @return a boolean that expresses whether the game is in validation mode.
     */
    public boolean isValidation() {
        return isValidation;
    }

    /**
     * Gets the score of the game at this observation.
     * @return score of the game.
     */
    public float getGameScore() {
        return gameScore;
    }

    /**
     * Returns the game tick of this particular observation.
     * @return the game tick.
     */
    public int getGameTick() {
        return gameTick;
    }

    /**
     * Indicates if there is a game winner in the current observation.
     * Possible values are Types.WINNER.PLAYER_WINS, Types.WINNER.PLAYER_LOSES and
     * Types.WINNER.NO_WINNER.
     * @return the winner of the game.
     */
    public Types.WINNER getGameWinner() {
        return gameWinner;
    }

    /**
     * Indicates if the game is over or if it hasn't finished yet.
     * @return true if the game is over.
     */
    public boolean isGameOver() {
        return isGameOver;
    }

    /**
     * Returns the world dimensions, in pixels.
     * @return the world dimensions, in pixels.
     */
    public double[] getWorldDimension() {
        return worldDimension;
    }

    /**
     * Indicates how many pixels form a block in the game.
     * @return how many pixels form a block in the game.
     */
    public int getBlockSize() {
        return blockSize;
    }

    /**
     * Returns the number of players in the game.
     */
    public int getNoOfPlayers() {
        return noOfPlayers;
    }

    /**
     * Returns the speed of the avatar. If the game is finished, we cannot guarantee that
     * this speed reflects the real speed of the avatar (the avatar itself could be
     * destroyed). If game finished, this returns 0.
     * @return orientation of the avatar, or 0 if game is over.
     */
    public float getAvatarSpeed() {
        return avatarSpeed;
    }

    /**
     * Returns the orientation of the avatar. If the game is finished, we cannot guarantee that
     * this orientation reflects the real orientation of the avatar (the avatar itself could be
     * destroyed). If game finished, this returns Types.NIL.
     * @return orientation of the avatar, or Types.NIL if game is over.
     */
    public double[] getAvatarOrientation() {
        return avatarOrientation;
    }

    /**
     * Returns the position of the avatar. If the game is finished, we cannot guarantee that
     * this position reflects the real position of the avatar (the avatar itself could be
     * destroyed). If game finished, this returns Types.NIL.
     * @return position of the avatar, or Types.NIL if game is over.
     */
    public double[] getAvatarPosition() {
        return avatarPosition;
    }

    /**
     * Returns the avatar's last move. At the first game cycle, it returns ACTION_NIL.
     * Note that this may NOT be the same as the last action given by the agent, as it may
     * have overspent in the last game cycle.
     * @return the action that was executed in the real game in the last cycle. ACTION_NIL
     * is returned in the very first game step.
     */
    public Types.ACTIONS getAvatarLastAction() {
        return avatarLastAction;
    }

    /**
     * Returns the avatar's type. In case it has multiple types, it returns the most specific one.
     * @return the itype of the avatar.
     */
    public int getAvatarType() {
        return avatarType;
    }

    /**
     * Returns the health points of the avatar. A value of 0 doesn't necessarily
     * mean that the avatar is dead (could be that no health points are in use in that game).
     * @return a numeric value, the amount of remaining health points.
     */
    public int getAvatarHealthPoints() {
        return avatarHealthPoints;
    }

    /**
     * Returns the maximum amount of health points.
     * @return the maximum amount of health points the avatar ever had.
     */
    public int getAvatarMaxHealthPoints() {
        return avatarMaxHealthPoints;
    }

    /**
     * Returns the limit of health points this avatar can have.
     * @return the limit of health points the avatar can have.
     */
    public int getAvatarLimitHealthPoints() {
        return avatarLimitHealthPoints;
    }

    /**
     * returns true if the avatar is alive.
     * @return true if the avatar is alive.
     */
    public boolean isAvatarAlive() {
        return isAvatarAlive;
    }

    /**
     * Returns the resources in the avatar's possession. As there can be resources of different
     * nature, each entry is a key-value pair where the key is the resource ID, and the value is
     * the amount of that resource type owned. It should be assumed that there might be other resources
     * available in the game, but the avatar could have none of them.
     * If the avatar has no resources, an empty HashMap is returned.
     * @return resources owned by the avatar.
     */
    public HashMap<Integer, Integer> getAvatarResources() {
        return avatarResources;
    }

    /**
     * Returns a grid with all observations in the level, accessible by the x,y coordinates
     * of the grid. Each grid cell has a width and height of getBlockSize() pixels. Each cell
     * contains a list with all observations in that position. Note that the same observation
     * may occupy more than one grid cell.
     * @return the grid of observations
     */
    public Observation[][][] getObservationGrid() {
        return observationGrid;
    }

    /**
     * Returns a list of observations of NPC in the game. As there can be
     * NPCs of different type, each entry in the array corresponds to a sprite type.
     * Every ArrayList contains a list of objects of type Observation.
     * Each Observation holds the position, unique id and
     * sprite id of that particular sprite.
     *
     * @return Observations of NPCs in the game.
     */
    public Observation[][] getNPCPositions() {
        return NPCPositions;
    }

    /**
     * Returns a list of observations of immovable sprites in the game. As there can be
     * immovable sprites of different type, each entry in the array corresponds to a sprite type.
     * Every ArrayList contains a list of objects of type Observation.
     * Each Observation holds the position, unique id and
     * sprite id of that particular sprite.
     *
     * @return Observations of immovable sprites in the game.
     */
    public Observation[][] getImmovablePositions() {
        return immovablePositions;
    }

    /**
     * Returns a list of observations of sprites that move, but are NOT NPCs in the game.
     * As there can be movable sprites of different type, each entry in the array
     * corresponds to a sprite type. Every ArrayList contains a list of objects of type
     * Observation. Each Observation holds the position,
     * unique id and sprite id of that particular sprite.
     *
     * @return Observations of movable, not NPCs, sprites in the game.
     */
    public Observation[][] getMovablePositions() {
        return movablePositions;
    }

    /**
     * Returns a list of observations of resources in the game. As there can be
     * resources of different type, each entry in the array corresponds to a sprite type.
     * Every ArrayList contains a list of objects of type Observation.
     * Each Observation holds the position, unique id and
     * sprite id of that particular sprite.
     *
     * @return Observations of resources in the game.
     */
    public Observation[][] getResourcesPositions() {
        return resourcesPositions;
    }

    /**
     * Returns a list of observations of portals in the game. As there can be
     * portals of different type, each entry in the array corresponds to a sprite type.
     * Every ArrayList contains a list of objects of type Observation. Each Observation
     * holds the position, unique id and sprite id of that particular sprite.
     *
     * @return Observations of portals in the game.
     */
    public Observation[][] getPortalsPositions() {
        return portalsPositions;
    }

    @Override
    public java.lang.String toString() {
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
                "imageArray=" + java.util.Arrays.toString(imageArray) +
                ", phase=" + phase +
                ", isValidation=" + isValidation +
                ", gameScore=" + gameScore +
                ", gameTick=" + gameTick +
                ", gameWinner=" + gameWinner +
                ", isGameOver=" + isGameOver +
                ", worldDimension=" + java.util.Arrays.toString(worldDimension) +
                ", blockSize=" + blockSize +
                ", noOfPlayers=" + noOfPlayers +
                ", avatarSpeed=" + avatarSpeed +
                ", avatarOrientation=" + java.util.Arrays.toString(avatarOrientation) +
                ", avatarPosition=" + java.util.Arrays.toString(avatarPosition) +
                ", avatarLastAction=" + avatarLastAction +
                ", avatarType=" + avatarType +
                ", avatarHealthPoints=" + avatarHealthPoints +
                ", avatarMaxHealthPoints=" + avatarMaxHealthPoints +
                ", avatarLimitHealthPoints=" + avatarLimitHealthPoints +
                ", isAvatarAlive=" + isAvatarAlive +
                ", availableActions=" + availableActions +
                ", avatarResources=" + avatarResources +
                ", observationGrid=" + java.util.Arrays.toString(observationGrid) +
                ", NPCPositions=" + java.util.Arrays.toString(NPCPositions) +
                ", immovablePositions=" + java.util.Arrays.toString(immovablePositions) +
                ", movablePositions=" + java.util.Arrays.toString(movablePositions) +
                ", resourcesPositions=" + java.util.Arrays.toString(resourcesPositions) +
                ", portalsPositions=" + java.util.Arrays.toString(portalsPositions) +
                ", fromAvatarSpritesPositions=" + java.util.Arrays.toString(fromAvatarSpritesPositions) +
                "}\n" + observation;
    }
}

