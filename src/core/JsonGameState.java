package core;

import com.google.gson.Gson;
import ontology.Types;
import java.util.ArrayList;

/**
 * Created by Jialin Liu on 21/10/2016.
 * CSEE, University of Essex, UK
 * Email: jialin.liu@essex.ac.uk
 * <p/>
 * Respect to Google Java Style Guide:
 * https://google.github.io/styleguide/javaguide.html
 */
public class JsonGameState {
  private double[] scores;
  private Types.WINNER[] winStates;
  private int gameTick;
  private ArrayList<Types.ACTIONS>[] availableActions;
//  private File gameStateFile;
  private byte[] gameStateBytes;
  private int[][] gameStateRGB;

  public JsonGameState(int[][] gameStateRGB, int gameTick, ArrayList<Types.ACTIONS>[] availableActions,
                       double[] scores, Types.WINNER[] winStates) {
    this.gameStateRGB = gameStateRGB;
    this.gameTick = gameTick;
    this.availableActions = availableActions;
    this.scores = scores;
    this.winStates = winStates;
  }

  public JsonGameState(byte[] gameStateBytes, int gameTick, ArrayList<Types.ACTIONS>[] availableActions,
                       double[] scores, Types.WINNER[] winStates) {
    this.gameStateBytes = gameStateBytes;
    this.gameTick = gameTick;
    this.availableActions = availableActions;
    this.scores = scores;
    this.winStates = winStates;
  }


  public double getScores(int playerId) {
    return scores[playerId];
  }

  public Types.WINNER getWinStates(int playerId) {
    return winStates[playerId];
  }

  public int getGameTick(int playerId) {
    return gameTick;
  }

  public ArrayList<Types.ACTIONS> getAvailableActions(int playerId) {
    return this.availableActions[playerId];
  }

//  public File getGameStateFile() {
//    return gameStateFile;
//  }

  public byte[] getGameStateBytes() {
    return this.gameStateBytes;
  }

  public int[][] getGameStateRGB() {
    return this.gameStateRGB;
  }

  public String parseGameState() {
    Gson gson = new Gson();
    return new Gson().toJson(this);
  }
}
