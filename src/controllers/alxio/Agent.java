package controllers.alxio;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import java.util.ArrayList;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA. User: ssamot Date: 14/11/13 Time: 21:45 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

    public BFSNode m_root;

    public void init(StateObservation a_gameState) {
        if (m_root != null) {
            m_root = m_root.proceed(lastAction, a_gameState);
            m_root.clean();
        }
        if (m_root == null) {
            m_root = BFSNode.init(a_gameState);
        }
    }

    /**
     * Public constructor with state observation and time due.
     *
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {

        ArrayList<Observation>[][] grid = so.getObservationGrid();

        //Debug.log(4, so.getAvatarPosition().toString());
        Z.init(grid[0].length, grid.length);

        ArrayList<Types.ACTIONS> act = so.getAvailableActions();
        Game.actions = new Types.ACTIONS[act.size()];
        for (int i = 0; i < Game.actions.length; ++i) {
            Game.actions[i] = act.get(i);
        }
        Game.NUM_ACTIONS = Game.actions.length;
    }

    private int lastAction = -1;

//    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
//    {
//        Types.ACTIONS action = Types.ACTIONS.fromVector(Utils.processMovementActionKeys(core.game.Game.ki.getMask()));
//        return action;
//    }
    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     *
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        if (Strategus.FAIL) {
            m_root = null;
            return Types.ACTIONS.ACTION_NIL;
        }
        try {
            init(stateObs);
            lastAction = m_root.search(elapsedTimer);

            if (lastAction == -1) {
                Debug.println("Oops, returning null action.");
                return Types.ACTIONS.ACTION_NIL;
            }

            return Game.actions[lastAction];
        } catch (Exception e) {
            lastAction = -1;
            return Types.ACTIONS.ACTION_NIL;
        }
    }
}
