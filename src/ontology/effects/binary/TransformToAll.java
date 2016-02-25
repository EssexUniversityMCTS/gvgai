package ontology.effects.binary;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.Types;
import ontology.avatar.MovingAvatar;
import ontology.effects.Effect;
import ontology.effects.unary.TransformTo;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
    public String stype2;
    public int itype2;

    public TransformToAll(InteractionContent cnt)
    {
        super(cnt);
        itype2 = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype2);
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
            VGDLSprite newSprite = game.addSprite(itype2, s.getPosition(), true);
//            System.out.println("transforming (" + game.getNumSprites(itype) + " : "  + itype + " ) " + s +
//                    " to " + newSprite + " after colliding with " + sprite2.getType());
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
