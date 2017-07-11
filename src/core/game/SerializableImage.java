package core.game;

import ontology.Types;
import tools.com.google.gson.Gson;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Daniel on 27.06.2017.
 */
public class SerializableImage {

    byte[] imageArray;
    public boolean isValidation;
    public float gameScore;
    public int gameTick;
    public Types.WINNER gameWinner;
    public boolean isGameOver;

    public SerializableImage(BufferedImage image, StateObservation s){
        try {
            gameScore = (float) s.getGameScore();
            gameTick = s.getGameTick();
            gameWinner = s.getGameWinner();
            isGameOver = s.isGameOver();
            imageArray = imageToByteArray(image);
        }catch(IOException e){
            System.out.println("Transforming image to byte array failed. Original error: " + e);
        }
    }

    public static byte[] imageToByteArray(BufferedImage image) throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
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
}
