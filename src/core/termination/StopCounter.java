package core.termination;

import core.vgdl.VGDLRegistry;
import core.content.TerminationContent;
import core.game.Game;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 22/10/13
 * Time: 18:54
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */

// Anti termination: this prevents counter terminations from triggering if certain conditions are met.

public class StopCounter extends Termination
{
    //TODO if needed: Theoretically, we could have an array of types here... to be done.
    public String stype1, stype2, stype3;
    public int itype1=-1, itype2=-1, itype3=-1;
    public boolean min = false;

    public StopCounter(){}

    public StopCounter(TerminationContent cnt) throws Exception
    {
        //Parse the arguments.
        this.parseParameters(cnt);
        if(stype1 != null) {
            itype1 = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype1);
            if(itype1 == -1){
        	String[] className = this.getClass().getName().split("\\.");
        	throw new Exception("[" + className[className.length - 1] + "] Undefined sprite " + stype1);
            }
        }
        if(stype2 != null) {
            itype2 = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype2);
            if(itype2 == -1){
        	String[] className = this.getClass().getName().split("\\.");
        	throw new Exception("[" + className[className.length - 1] + "] Undefined sprite " + stype2);
            }
        }
        if(stype3 != null) {
            itype3 = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype3);
            if(itype3 == -1){
        	String[] className = this.getClass().getName().split("\\.");
        	throw new Exception("[" + className[className.length - 1] + "] Undefined sprite " + stype3);
            }
        }
    }

    @Override
    public boolean isDone(Game game)
    {
        boolean ended = super.isFinished(game);
        if(ended)
            return true;

        int countAcum = 0;

        if(itype1 != -1) countAcum += game.getNumSprites(itype1) - game.getNumDisabledSprites(itype1);
        if(itype2 != -1) countAcum += game.getNumSprites(itype2) - game.getNumDisabledSprites(itype2);
        if(itype3 != -1) countAcum += game.getNumSprites(itype3) - game.getNumDisabledSprites(itype3);

        if (min) {
            canEnd = countAcum <= limit;
        }
        else {
            canEnd = countAcum != limit;
        }

        return false;
    }
    
    @Override
	public ArrayList<String> getTerminationSprites() {
		ArrayList<String> result = new ArrayList<String>();
		if(stype1 != null) result.add(stype1);
        if(stype2 != null) result.add(stype2);
        if(stype3 != null) result.add(stype3);
		
		return result;
	}
}
