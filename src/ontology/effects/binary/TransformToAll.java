package ontology.effects.binary;

import core.vgdl.VGDLRegistry;
import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import core.logging.Logger;
import core.logging.Message;
import ontology.effects.unary.TransformTo;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 23/10/13
 * Time: 15:21
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class TransformToAll extends TransformTo {

    //This effect transforms all sprites of type stype to stype2.
    // It DOES NOTHING to the sprites that cause the effect (unless they are the specified types stype or stype2)
    public String stypeTo;
    public int itypeTo;

    public TransformToAll(InteractionContent cnt) throws Exception
    {
        super(cnt);
        itypeTo = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stypeTo);
        if(itypeTo == -1){
            throw new Exception("Undefined sprite " + stypeTo);
        }
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        //First, we need to get all sprites of type stype.
        Iterator<VGDLSprite> spriteIt = game.getSpriteGroup(itype);

        if(spriteIt != null) while(spriteIt.hasNext())
        {
            VGDLSprite s = spriteIt.next();
            //Last argument: forces the creation. This could be a parameter of the effect too, if needed.
            VGDLSprite newSprite = game.addSprite(itypeTo, s.getPosition(), true);
            //newSprite inherits things from 's'. Maybe sprite2 gets killed in the process.
            super.transformTo(newSprite, s, sprite2, game);
        }
    }
    
    @Override
    public ArrayList<String> getEffectSprites(){
    	ArrayList<String> result = new ArrayList<String>();
    	if(stype!=null) result.add(stype);
    	
    	return result;
    }
}
