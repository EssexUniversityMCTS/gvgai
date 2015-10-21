package controllers.Return42.knowledgebase.observation;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import core.game.StateObservation;

public class CombinedGameObserver implements GameObserver {

	List<GameObserver> gameObservers = new LinkedList<>();

	public CombinedGameObserver( GameObserver... observers ) {
		this.gameObservers = new LinkedList<GameObserver>( Arrays.asList( observers ) );
	}

	public void addObserver(GameObserver gameObserver) {
		gameObservers.add(gameObserver);
	}

	@Override
	public void preStepObserve(StateObservation stateObs) {
		for(GameObserver observer : gameObservers){
			observer.preStepObserve(stateObs);
		}
	}

	@Override
	public void postStepObserve(StateObservation stateObs) {
		for (GameObserver observer : gameObservers) {
			observer.postStepObserve(stateObs);
		}
	}

}