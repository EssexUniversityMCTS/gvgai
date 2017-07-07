package serialization;

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

    // Optional, helper method to convert a byte array to PNG format
    public void convertBytesToPng(byte[] pixels) throws IOException, DataFormatException {
        InputStream in = new ByteArrayInputStream(decompress(pixels));
        BufferedImage bImageFromConvert = ImageIO.read(in);

        ImageIO.write(bImageFromConvert, "png", new File(
                "./gamestateByBytes.png"));

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

}

