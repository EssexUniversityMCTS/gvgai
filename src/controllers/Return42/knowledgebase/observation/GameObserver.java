package controllers.Return42.knowledgebase.observation;

import core.game.StateObservation;

public interface GameObserver {
	/**
	 * Will be called before an advance step is performed
	 * 
	 * @param stateObs
	 */
	void preStepObserve(StateObservation stateObs);
	
	/**
	 * Will be called after an advance step is performed. The passed state is a
	 * direct result of advance call on the state object that was passed to
	 * {@link #preStepObserve(StateObservation)} beforehand.
	 * 
	 * @param stateObs
	 */
	void postStepObserve(StateObservation stateObs);
}