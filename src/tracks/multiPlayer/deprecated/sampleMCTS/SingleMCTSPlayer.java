package tracks.multiPlayer.deprecated.sampleMCTS;

import java.util.Random;

import core.game.StateObservationMulti;
import ontology.Types;
import tools.ElapsedCpuTimer;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 07/11/13
 * Time: 17:13
 */
public class SingleMCTSPlayer
{
    /**
     * Root of the tree.
     */
    public SingleTreeNode m_root;

    int[] NUM_ACTIONS;
    Types.ACTIONS[][] actions;

    /**
     * Random generator.
     */
    public Random m_rnd;

    public int iters = 0, num = 0;
    public int id, oppID, no_players;

    /**
     * Creates the MCTS player with a sampleRandom generator object.
     * @param a_rnd sampleRandom generator object.
     */
    public SingleMCTSPlayer(Random a_rnd, int[] NUM_ACTIONS, Types.ACTIONS[][] actions, int id, int oppID, int no_players)
    {
        m_rnd = a_rnd;
        this.NUM_ACTIONS = NUM_ACTIONS;
        this.actions = actions;
        this.id = id;
        this.oppID = oppID;
        this.no_players = no_players;
        m_root = new SingleTreeNode(a_rnd, NUM_ACTIONS, actions, id, oppID, no_players);
    }

    /**
     * Inits the tree with the new observation state in the root.
     * @param a_gameState current state of the game.
     */
    public void init(StateObservationMulti a_gameState)
    {
        //Set the game observation to a newly root node.
        m_root = new SingleTreeNode(m_rnd, NUM_ACTIONS, actions, id, oppID, no_players);
        m_root.state = a_gameState;

    }

    /**
     * Runs MCTS to decide the action to take. It does not reset the tree.
     * @param elapsedTimer Timer when the action returned is due.
     * @return the action to execute in the game.
     */
    public int run(ElapsedCpuTimer elapsedTimer)
    {
        //Do the search within the available time.
        m_root.mctsSearch(elapsedTimer);
        System.out.println(elapsedTimer.remainingTimeMillis());

        iters += SingleTreeNode.totalIters;
        num ++;

        //Determine the best action to take and return it.
        int action = m_root.mostVisitedAction();
        System.out.println(elapsedTimer.remainingTimeMillis());
        //int action = m_root.bestAction();
        return action;
    }

}
