package controllers.singlePlayer.sampleOLMCTSMacro;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

    public int ROLLOUT_DEPTH = 10; //NUMBER OF MACRO-ACTIONS
    private int MACRO_ACTION_LENGTH = 3; //LENGTH OF EACH MACRO-ACTION

    public int num_actions;
    public Types.ACTIONS[] actions;

    private int m_actionsLeft;
    private int m_lastMacroAction;
    private boolean m_throwTree;


    protected SingleMCTSPlayer mctsPlayer;


    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        m_actionsLeft = 0;
        m_lastMacroAction = -1;
        m_throwTree = true;

        //Get the actions in a static array.
        ArrayList<Types.ACTIONS> act = so.getAvailableActions(true);
        actions = new Types.ACTIONS[act.size()];
        for(int i = 0; i < actions.length; ++i)
        {
            actions[i] = act.get(i);
        }
        num_actions = actions.length;

        //Create the player.

        mctsPlayer = getPlayer(so, elapsedTimer);
    }

    public SingleMCTSPlayer getPlayer(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        return new SingleMCTSPlayer(new Random(), num_actions, actions, MACRO_ACTION_LENGTH, ROLLOUT_DEPTH);
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        int nextAction;

        int gameTick = stateObs.getGameTick();
        //a_timeDue -=30;
        if(gameTick == 0)
        {
            mctsPlayer.init(stateObs, m_throwTree);

            //Game just started, determine a macro-action.
            int action = mctsPlayer.run(elapsedTimer);

            m_lastMacroAction = action;
            m_throwTree = true;
            nextAction = action;
            m_actionsLeft = MACRO_ACTION_LENGTH-1;

        }else{

            prepareGameCopy(stateObs);

            if(m_actionsLeft > 0) //In the middle of the macro action.
            {
                mctsPlayer.init(stateObs, m_throwTree);

                mctsPlayer.run(elapsedTimer);
                nextAction = m_lastMacroAction;
                m_actionsLeft--;
                m_throwTree = false;


            }else if(m_actionsLeft == 0)        //Finishing a macro-action
            {

                int action = mctsPlayer.run(elapsedTimer);
                nextAction = m_lastMacroAction;
                m_lastMacroAction = action;
                m_actionsLeft = MACRO_ACTION_LENGTH-1;
                m_throwTree = true;

            }else{
                throw new RuntimeException("This should not be happening: " + m_actionsLeft);
            }
        }

        return actions[nextAction];
    }


    public void prepareGameCopy(StateObservation stateObs)
    {
        if(m_lastMacroAction != -1)
        {
            int first = MACRO_ACTION_LENGTH - m_actionsLeft - 1;
            for(int i = first; i < MACRO_ACTION_LENGTH; ++i)
            {
                stateObs.advance(actions[m_lastMacroAction]);
            }
        }
    }

}
