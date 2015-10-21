package controllers.Return42;

import java.awt.Graphics2D;

import controllers.Return42.algorithms.AlgorithmPicker;
import controllers.Return42.knowledgebase.KnowledgeBase;
import controllers.Return42.util.CachingStateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;
import core.game.StateObservation;
import core.player.AbstractPlayer;

public class Agent extends AbstractPlayer {

    private final KnowledgeBase knowledge;
    private AbstractPlayer current;

    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        this.knowledge = new KnowledgeBase();
        AlgorithmPicker picker = new AlgorithmPicker();

        StateObservation state = decrorateState(stateObs);
        knowledge.initForGame( state );
        
        current = picker.pickAlgorithm( knowledge, state, elapsedTimer );
    }

    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
    	StateObservation state = decrorateState(stateObs);
        return current.act(state, elapsedTimer);
    }
    
    private StateObservation decrorateState( StateObservation state ) {
    	return new CachingStateObservation( state );
    }
    
    @Override
    public void draw(Graphics2D g) {
    	current.draw(g);
    }
}