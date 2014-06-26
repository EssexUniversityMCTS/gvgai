package core.termination;

import core.VGDLRegistry;
import core.content.TerminationContent;
import core.game.Game;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 22/10/13
 * Time: 18:52
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class SpriteCounter extends Termination
{
    public String stype;
    public int itype;

    public SpriteCounter(){}

    public SpriteCounter(TerminationContent cnt)
    {
        //Parse the arguments.
        this.parseParameters(cnt);
        itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
    }

    @Override
    public boolean isDone(Game game) {

        boolean ended = super.isFinished(game);
        if(ended)
            return true;

        if(game.getNumSprites(itype) <= limit)
            return true;

        return false;
    }

}
