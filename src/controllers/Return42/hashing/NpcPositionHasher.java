package controllers.Return42.hashing;

import core.game.StateObservation;

public class NpcPositionHasher implements IGameStateHasher {
	
	public int hash( StateObservation state ) {
        return  ObservationHasher.hash( state.getNPCPositions() );
	}
}
