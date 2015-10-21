package controllers.Return42.hashing;

import core.game.StateObservation;

public class ResourcePositionHasher implements IGameStateHasher {
	
	public int hash( StateObservation state ) {
        return  ObservationHasher.hash( state.getResourcesPositions() );
	}
}
