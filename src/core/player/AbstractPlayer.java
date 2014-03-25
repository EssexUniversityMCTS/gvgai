package core.player;

import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 13:42
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public abstract class AbstractPlayer {

    /**
     * File where the actions played in a given game are stored.
     */
    private String actionFile;

    /**
     * Writer for the actions file.
     */
    private BufferedWriter writer;

    /**
     * Set this variable to FALSE to avoid logging the actions to a file.
     */
    private static final boolean SHOULD_LOG = true;

    /**
     * Last action executed by this agent.
     */
    private Types.ACTIONS lasAction = null;


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player. The action returned must be contained in the
     * actions accessible from stateObs.getAvailableActions(), or no action
     * will be applied.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public abstract Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer);


    /**
     * This function sets up the controller to save the actions executed in a given game.
     * @param actionFile file to save the actions to.
     * @param randomSeed Seed for the sampleRandom generator of the game to be played.
     */
    final public void setup(String actionFile, int randomSeed) {
        this.actionFile = actionFile;

        try {
            if(this.actionFile!=null && SHOULD_LOG)
            {
                writer = new BufferedWriter(new FileWriter(new File(this.actionFile)));
                writer.write(randomSeed + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the agent, writing actions to file.
     */
    final public void teardown() {
        try {
            if(writer!=null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Logs a single action
     * @param action the action to log.
     */
    final public void logAction(Types.ACTIONS action) {

        lasAction = action;
        if(writer!=null && SHOULD_LOG) {
            try {
                writer.write(action.toString() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Gets the last action executed by this controller.
     * @return the last action
     */
    public Types.ACTIONS getLastAction()
    {
        return lasAction;
    }

    /**
     * Gets the player the control to draw something on the screen.
     * It can be used for debug purposes.
     * @param g Graphics device to draw to.
     */
    public void draw(Graphics2D g)
    {
        //Overwrite this method in your controller to draw on the screen.
        //This method should be left empty in this class.
    }


}
