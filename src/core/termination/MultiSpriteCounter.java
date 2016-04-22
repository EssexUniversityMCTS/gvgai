package core.termination;

import java.util.ArrayList;

import core.VGDLRegistry;
import core.content.TerminationContent;
import core.game.Game;
import core.game.GameDescription.TerminationData;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 22/10/13
 * Time: 18:54
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class MultiSpriteCounter extends Termination
{
    //TODO if needed: Theoretically, we could have an array of types here... to be done.
    public String stype1, stype2;
    public int itype1=-1, itype2=-1;
    public boolean min = false;

    public MultiSpriteCounter(){}

    public MultiSpriteCounter(TerminationContent cnt)
    {
        //Parse the arguments.
        this.parseParameters(cnt);
        if(stype1 != null) itype1 = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype1);
        if(stype2 != null) itype2 = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype2);
    }

    @Override
    public boolean isDone(Game game)
    {
        boolean ended = super.isFinished(game);
        if(ended)
            return true;

         int countAcum = 0;

        if(itype1 != -1) countAcum += game.getNumSprites(itype1);
        if(itype2 != -1) countAcum += game.getNumSprites(itype2);

        if(countAcum == limit)
            return true;

        if(min && countAcum > limit)
            return true; //If the limit is a lower bound in what's required.

        return false;
    }
    
    @Override
	public ArrayList<String> getTerminationSprites() {
		ArrayList<String> result = new ArrayList<String>();
		if(stype1 != null) result.add(stype1);
		if(stype2 != null) result.add(stype2);
		
		return result;
	}
}
