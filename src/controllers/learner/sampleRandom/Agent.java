package controllers.learner.sampleRandom;

import core.JsonGameState;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import core.player.AbstractLearner;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.DataFormatException;

/**
 * Created by Jialin Liu on 21/10/2016.
 * CSEE, University of Essex, UK
 * Email: jialin.liu@essex.ac.uk
 * <p/>
 * Respect to Google Java Style Guide:
 * https://google.github.io/styleguide/javaguide.html
 */
public class Agent extends AbstractLearner {
  private Random rdm = new Random();

  public Agent(String stateObsStr, ElapsedCpuTimer elapsedTimer){
  }


  @Override
  public Types.ACTIONS act(String stateObsStr, ElapsedCpuTimer elapsedTimer) {
    JsonGameState stateObs = deserialiseGameState(stateObsStr);
    ArrayList<Types.ACTIONS> avalaibleActions = stateObs.getAvailableActions(this.getPlayerID());
    int idxAction = this.rdm.nextInt(avalaibleActions.size());
//
//    try {
//      convertRgbToPng(stateObs.getGameStateRGB());
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
    try {
      convertBytesToPng(stateObs.getGameStateBytes());
    } catch (IOException e) {
      e.printStackTrace();
    } catch (DataFormatException e) {
      e.printStackTrace();
    }
    return avalaibleActions.get(idxAction);
  }

  @Override
  public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
    return null;
  }

  @Override
  public Types.ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer) {
    return null;
  }
}
