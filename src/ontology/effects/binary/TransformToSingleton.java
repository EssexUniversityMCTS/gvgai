package ontology.effects.binary;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.avatar.MovingAvatar;
import ontology.effects.Effect;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 23/10/13
 * Time: 15:21
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class TransformToSingleton extends Effect {

    public String stype; //new type to be transormed to
    public int itype;    //new type to be transormed to

    //true if orientation of sprite 2 should be inherited. Otherwise, orientation is kept.
    public boolean takeOrientation;

    //The other sprite of type stype (if any) is transformed back to stype_other:
    public String stype_other; // type the sprites of type stype are transormed back to
    public int itype_other;    // type the sprites of type stype are transormed back to


    public TransformToSingleton(InteractionContent cnt)
    {
        takeOrientation = false;
        is_kill_effect = true;
        this.parseParameters(cnt);
        itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
        itype_other = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype_other);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        //First, transform all sprites in the game to the itype_other type.
        // (in theory, there should be only 1 or none).
        Iterator<VGDLSprite> itSprites = game.getSpriteGroup(itype);
        if(itSprites != null) while(itSprites.hasNext())
        {
            VGDLSprite sprite = itSprites.next();

            VGDLSprite newSprite = game.addSprite(itype_other, sprite.getPosition());
            if(newSprite != null)
                setSpriteFields(game, newSprite, sprite);
        }

        //Now, make the transformTo normal operation.
        VGDLSprite newSprite = game.addSprite(itype, sprite1.getPosition());
        if(newSprite != null)
        {
            setSpriteFields(game, newSprite, sprite1);

            if(takeOrientation) {
                Vector2d orientation = sprite2.orientation.copy();
                orientation.mul(-1);
                newSprite.is_oriented = true;
                newSprite.orientation = orientation;
            }
        }
    }

    private void setSpriteFields(Game game, VGDLSprite newSprite, VGDLSprite oldSprite)
    {
        //Orientation
        if(newSprite.is_oriented && oldSprite.is_oriented)
        {
            newSprite.orientation = oldSprite.orientation;
        }

        //Last position of the avatar.
        newSprite.lastrect =  new Rectangle(oldSprite.lastrect.x, oldSprite.lastrect.y,
                oldSprite.lastrect.width, oldSprite.lastrect.height);

        //Copy resources
        if(oldSprite.resources.size() > 0)
        {
            Set<Map.Entry<Integer, Integer>> entries = oldSprite.resources.entrySet();
            for(Map.Entry<Integer, Integer> entry : entries)
            {
                int resType = entry.getKey();
                int resValue = entry.getValue();
                newSprite.modifyResource(resType, resValue);
            }
        }


        //Avatar handling (I think considering avatars here is weird...)
        if(oldSprite.is_avatar)
        {
            try{
                game.setAvatar((MovingAvatar) newSprite);
                game.getAvatar().player = ((MovingAvatar) oldSprite).player;
                game.getAvatar().lastAction = ((MovingAvatar) oldSprite).lastAction;
            }catch (ClassCastException e) {}
        }

        game.killSprite(oldSprite);
    }
    
    @Override
    public ArrayList<String> getEffectSprites(){
    	ArrayList<String> result = new ArrayList<String>();
    	if(stype!=null) result.add(stype);
    	if(stype_other!=null) result.add(stype_other);
    	
    	return result;
    }
}
