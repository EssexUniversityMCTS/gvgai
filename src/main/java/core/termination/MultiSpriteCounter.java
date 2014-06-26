package core.termination;

import core.VGDLRegistry;
import core.content.TerminationContent;
import core.game.Game;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 22/10/13 Time: 18:54 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class MultiSpriteCounter extends Termination {
	// TODO: Theoretically, we could have an array of types here... to be done.
	public String stype1;
	public String stype2;
	public int itype1 = -1;
	public int itype2 = -1;

	public MultiSpriteCounter() {
	}

	public MultiSpriteCounter(TerminationContent cnt) {
		// Parse the arguments.
		parseParameters(cnt);
		if (null != stype1)
			itype1 = VGDLRegistry.GetInstance()
					.getRegisteredSpriteValue(stype1);
		if (null != stype2)
			itype2 = VGDLRegistry.GetInstance()
					.getRegisteredSpriteValue(stype2);
	}

	@Override
	public boolean isDone(Game game) {
		boolean ended = isFinished(game);
		if (ended)
			return true;

		int countAcum = 0;

		if (-1 != itype1)
			countAcum += game.getNumSprites(itype1);
		if (-1 != itype2)
			countAcum += game.getNumSprites(itype2);

		return countAcum == limit;

	}
}
