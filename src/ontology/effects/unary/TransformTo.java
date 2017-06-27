package ontology.effects.unary;

import java.awt.Rectangle;
import java.util.ArrayList;
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

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 23/10/13
 * Time: 15:21
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class TransformTo extends Effect {

    //TODO: Theoretically, we could have an array of types here... to be done.
    public String stype;
    public int itype;
    //Indicates if the second sprite should be killed.
    public boolean killSecond = false;
    public boolean forceOrientation = false;

    public TransformTo(InteractionContent cnt) throws Exception
    {
        is_kill_effect = true;
        this.parseParameters(cnt);
        itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
        if(itype == -1){
            throw new Exception("Undefined sprite " + stype);
        }
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
	if(sprite1 == null){
	    Logger.getInstance().addMessage(new Message(Message.WARNING, "1st sprite can't be EOS with TransformTo interaction."));
	    return;
	}
	
    	if (!sprite1.is_disabled()){
    		VGDLSprite newSprite = game.addSprite(itype, sprite1.getPosition());
    		transformTo(newSprite, sprite1,  sprite2,  game);
    	}
    }

    protected void transformTo(VGDLSprite newSprite, VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        if(newSprite != null)
        {
            //Orientation
            if(forceOrientation || newSprite.is_oriented && sprite1.is_oriented && newSprite.orientation.equals(Types.DNONE))
            {
                newSprite.orientation = sprite1.orientation;
            }

            //Last position of the avatar.
            newSprite.lastrect =  new Rectangle(sprite1.lastrect.x, sprite1.lastrect.y,
                    sprite1.lastrect.width, sprite1.lastrect.height);

            //Copy resources
            if(sprite1.resources.size() > 0)
            {
                Set<Map.Entry<Integer, Integer>> entries = sprite1.resources.entrySet();
                for(Map.Entry<Integer, Integer> entry : entries)
                {
                    int resType = entry.getKey();
                    int resValue = entry.getValue();
                    newSprite.modifyResource(resType, resValue);
                }
            }


            //Avatar handling.
            boolean transformed = true;
            if(sprite1.is_avatar)
            {
                try{
                    int id = ((MovingAvatar)sprite1).getPlayerID();
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
                }catch (ClassCastException e) {
                    transformed = false; // new sprite is not an avatar, don't kill the current one
                }
            }

            //Health points should be copied too.
            newSprite.healthPoints = sprite1.healthPoints;

            //boolean variable in method call set to true
            //to indicate the sprite was transformed
            game.killSprite(sprite1, transformed);

            if(killSecond && sprite2 != null)
                game.killSprite(sprite2, true);
        }
    }

    @Override
    public ArrayList<String> getEffectSprites(){
    	ArrayList<String> result = new ArrayList<String>();
    	if(stype!=null) result.add(stype);
    	
    	return result;
    }
}
