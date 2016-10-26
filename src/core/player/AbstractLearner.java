package core.player;

import com.google.gson.Gson;
import core.JsonGameState;
import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Created by Jialin Liu on 21/10/2016.
 * CSEE, University of Essex, UK
 * Email: jialin.liu@essex.ac.uk
 * <p/>
 * Respect to Google Java Style Guide:
 * https://google.github.io/styleguide/javaguide.html
 */
public abstract class AbstractLearner extends Player {

  public JsonGameState deserialiseGameState(String stateObsStr) {
    JsonGameState stateObs = new Gson().fromJson(stateObsStr, JsonGameState.class);
    return stateObs;
  }

  @Override
  public Types.ACTIONS act(String stateObsStr, ElapsedCpuTimer elapsedTimer) {
    return Types.ACTIONS.ACTION_NIL;
  }


  @Override
  public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
    System.err.println("You should not call this");
    return null;
  }

  public void convertRgbToPng(int[][] pixels) throws IOException {

    int height = pixels.length;
    int width = pixels[0].length;
    final BufferedImage image =
        new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

//    System.out.println( width + " " + height);
    for (int y = 0; y < height; ++y) {
      for (int x = 0; x < width; ++x) {
//        System.out.println(x + "," + y + "="+pixels[y][x]);
        image.setRGB(x, y, pixels[y][x]);
      }
    }

    ImageIO.write(image, "png", new File(
        "./gamestate.png"));
  }

  public void convertBytesToPng(byte[] pixels) throws IOException, DataFormatException {
    InputStream in = new ByteArrayInputStream(decompress(pixels));
    BufferedImage bImageFromConvert = ImageIO.read(in);

    ImageIO.write(bImageFromConvert, "png", new File(
        "./gamestateByBytes.png"));

  }

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
