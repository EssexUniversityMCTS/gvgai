package controllers.Return42.hashing;

import core.game.StateObservation;

public interface IGameStateHasher {
	public int hash( StateObservation state );
}
