package controllers.Return42.algorithms;

import controllers.Return42.knowledgebase.KnowledgeBase;
import controllers.Return42.knowledgebase.KnowledgeBaseUpdatingStateObservation;
import core.game.StateObservation;
import core.player.AbstractPlayer;

public abstract class KnowledgebasedAgent extends AbstractPlayer {

	protected final KnowledgeBase knowledge;
	
	public KnowledgebasedAgent( KnowledgeBase knowledge ) {
		this.knowledge = knowledge;
	}
	
	public StateObservation learnFromActions( StateObservation state ) {
		return new KnowledgeBaseUpdatingStateObservation(state, knowledge.getObserver());
	}
}
