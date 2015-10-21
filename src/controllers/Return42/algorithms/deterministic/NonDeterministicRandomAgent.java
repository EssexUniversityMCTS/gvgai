package controllers.Return42.algorithms.deterministic;

import ontology.Types;
import tools.ElapsedCpuTimer;
import controllers.Return42.algorithms.KnowledgebasedAgent;
import controllers.Return42.algorithms.deterministic.randomSearch.RandomSearch;
import controllers.Return42.algorithms.deterministic.randomSearch.RandomSearchFactory;
import controllers.Return42.algorithms.melee.MeleeAgent;
import controllers.Return42.knowledgebase.KnowledgeBase;
import controllers.Return42.util.StateObservationUtils;
import controllers.Return42.util.TimerUtils;
import core.game.StateObservation;

public class NonDeterministicRandomAgent extends KnowledgebasedAgent
{
	private final RandomSearch randomSearch;
	private final MeleeAgent melee;

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public NonDeterministicRandomAgent(KnowledgeBase knowledge, StateObservation so, ElapsedCpuTimer elapsedTimer) {
    	super(knowledge);
    	
    	this.melee = new MeleeAgent(so, elapsedTimer);
    	this.randomSearch = RandomSearchFactory.buildForLevel(knowledge, learnFromActions(so), 50, false );
	}


	/**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
    {
        if (StateObservationUtils.isPlayerCloseToNpc( stateObs, knowledge, 3 ) ) {
        	randomSearch.clearAllPlans();
        	return melee.act(stateObs, elapsedTimer);
        }
    	
		ElapsedCpuTimer safeTimer = TimerUtils.copyWithLessTime(elapsedTimer, 3);
		return randomSearch.act( learnFromActions( stateObs ), safeTimer );
    }
}