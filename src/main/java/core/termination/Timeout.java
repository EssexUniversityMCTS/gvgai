package core.termination;

import core.content.TerminationContent;
import core.game.Game;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 22/10/13 Time: 18:48 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Timeout extends Termination {
	public Timeout() {
	}

	public Timeout(TerminationContent cnt) {
		// Parse the arguments.
		parseParameters(cnt);
	}

	@Override
	public boolean isDone(Game game) {
		boolean ended = isFinished(game);
		if (ended)
			return true;

		return game.getGameTick() >= limit;

	}
}
