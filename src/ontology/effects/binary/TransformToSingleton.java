package ontology.effects.binary;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import core.vgdl.VGDLRegistry;
import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import core.logging.Logger;
import core.logging.Message;
import core.player.Player;
import ontology.Types;
import ontology.avatar.MovingAvatar;
import ontology.effects.Effect;
import tools.Direction;

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


    public TransformToSingleton(InteractionContent cnt) throws Exception
    {
        takeOrientation = false;
        is_kill_effect = true;
        this.parseParameters(cnt);
        itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
        itype_other = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype_other);
        if(itype == -1){
            throw new Exception("Undefined sprite " + stype);
        }
        if(itype_other == -1){
            throw new Exception("Undefined sprite " + stype_other);
        }
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
	if(sprite1 == null || sprite2 == null){
	    Logger.getInstance().addMessage(new Message(Message.WARNING, "Neither the 1st nor 2nd sprite can be EOS with TransformToSingleton interaction."));
	    return;
	}
	
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
                Direction orientation = new Direction(-sprite2.orientation.x(), -sprite2.orientation.y());
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
        boolean transformed = true;
        if(oldSprite.is_avatar)
        {
            try {
                int id = ((MovingAvatar)oldSprite).getPlayerID();
                Player p = game.getAvatar(id).player;
                double score = game.getAvatar(id).getScore();
                Types.WINNER win = game.getAvatar(id).getWinState();
                game.setAvatar((MovingAvatar) newSprite, id);
                game.getAvatar(id).player = p;
                game.getAvatar(id).setKeyHandler(Game.ki);
                game.getAvatar(id).setScore(score);
                game.getAvatar(id).setWinState(win);
                game.getAvatar(id).setPlayerID(id);
                transformed = true;
            } catch (ClassCastException e) {
                transformed = false; // new sprite is not an avatar, don't kill the current one}
            }
        }

        //boolean variable set to true to indicate the sprite was transformed
        game.killSprite(oldSprite, transformed);
    }
    
    @Override
    public ArrayList<String> getEffectSprites(){
    	ArrayList<String> result = new ArrayList<String>();
    	if(stype!=null) result.add(stype);
    	if(stype_other!=null) result.add(stype_other);
    	
    	return result;
    }
}
