package controllers.Return42.algorithms.deterministic;

import java.awt.Graphics2D;

import controllers.Return42.algorithms.KnowledgebasedAgent;
import controllers.Return42.algorithms.deterministic.puzzleSolver.AStarSearch;
import controllers.Return42.algorithms.deterministic.randomSearch.RandomSearchFactory;
import controllers.Return42.knowledgebase.KnowledgeBase;
import controllers.Return42.util.TimerUtils;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import core.game.StateObservation;

public class CombinedDeterministicAgent extends KnowledgebasedAgent
{
	private DeterministicAgent currentAgent;
	int agentIndex;

    public CombinedDeterministicAgent( KnowledgeBase knowledge, StateObservation state, ElapsedCpuTimer elapsedTimer) {
    	super(knowledge);
    	agentIndex = 0;
    	state = learnFromActions(state);
    	
    	// start with a nil action. We need this for some games (like modality)
    	state.advance( ACTIONS.ACTION_NIL );
    	
    	ElapsedCpuTimer safeTimer = TimerUtils.copyWithLessTime( elapsedTimer, 10 );
    	currentAgent = pickNewAgent( state );
    	currentAgent.useConstructorExtraTime( state, safeTimer );
	}

    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
    {
    	if (stateObs.getGameTick() == 0)
    		return ACTIONS.ACTION_NIL;
    	
    	stateObs = learnFromActions(stateObs);
    	if (currentAgent.didFinish()) {
    		currentAgent = pickNewAgent( stateObs );
    	}

    	ElapsedCpuTimer safeTimer = TimerUtils.copyWithLessTime(elapsedTimer, 7);
		return currentAgent.act( stateObs, safeTimer );
    }
    
	private DeterministicAgent pickNewAgent(StateObservation stateObs) {
		agentIndex++;
		
		// start with 50 iterations solver
		if (agentIndex == 1)
			return new AStarSearch(knowledge, stateObs, 50, false);
		
		// continue with 500 iterations solver
		else if (agentIndex == 2)
			return new AStarSearch(knowledge, stateObs, 500, true);
		
		// alternate between 100 iterations random and 300 iterations solver
		else if (agentIndex % 2 == 0)
			return new WalkAwayAgent( stateObs, 5 );
		else
			return new AStarSearch(knowledge, stateObs, 300, true);
	}

	@Override
    public void draw(Graphics2D g) {
		currentAgent.draw( g );
    }
}