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
public class MultiSpriteCounterSubTypes extends Termination
{
    //TODO if needed: Theoretically, we could have an array of types here... to be done.
    public String stype1;
    public int itype1=-1;
    public int subTypesNum=-1;

    public MultiSpriteCounterSubTypes(){}

    public MultiSpriteCounterSubTypes(TerminationContent cnt) throws Exception
    {
        //Parse the arguments.
        this.parseParameters(cnt);
        if(stype1 != null){
            itype1 = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype1);
            if(itype1 == -1){
        	String[] className = this.getClass().getName().split("\\.");
        	throw new Exception("[" + className[className.length - 1] + "] Undefined sprite " + stype1);
            }
        }
    }

    @Override
    public boolean isDone(Game game)
    {
        //END Condition: there must be in the game:
        //  * exactly limit amount of sprites of type stype1
        //  * exactly subTypesNum number of subtypes of stype1

        boolean ended = super.isFinished(game);
        if(ended)
            return true;

        int countAcum = 0;

        if(itype1 != -1) countAcum += game.getNumSprites(itype1) - game.getNumDisabledSprites(itype1);

        if(countAcum == limit && canEnd) {

            ArrayList<Integer> subtypes = game.getSubTypes(itype1);
            int countAcumSubTypes = 0;
            for (Integer subtype : subtypes) {

                int count = (game.getNumSprites(subtype) - game.getNumDisabledSprites(subtype));
                if(count > 0)
                {
                    if(game.getSpriteGroup(subtype) != null) //This avoids non-terminal types
                    {
                        countAcumSubTypes += count > 0 ? 1 : 0;
                    }
                }
            }

            if(countAcumSubTypes == subTypesNum) {
                countScore(game);
                return true;
            }
        }


        return false;
    }
    
    @Override
	public ArrayList<String> getTerminationSprites() {
		ArrayList<String> result = new ArrayList<String>();
		if(stype1 != null) result.add(stype1);
		return result;
	}
}
