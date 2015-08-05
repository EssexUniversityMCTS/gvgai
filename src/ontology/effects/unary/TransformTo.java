package ontology.effects.unary;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.Types;
import ontology.avatar.MovingAvatar;
import ontology.effects.Effect;
import ontology.sprites.Resource;

import java.awt.*;
import java.util.Map;
import java.util.Set;

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

    public TransformTo(InteractionContent cnt)
    {
        is_kill_effect = true;
        this.parseParameters(cnt);
        itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        VGDLSprite newSprite = game.addSprite(itype, sprite1.getPosition());
        if(newSprite != null)
        {
            //Orientation
            if(newSprite.is_oriented && sprite1.is_oriented && newSprite.orientation == Types.NONE)
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
            if(sprite1.is_avatar)
            {
                try{
                    game.setAvatar((MovingAvatar) newSprite);
                    game.getAvatar().player = ((MovingAvatar) sprite1).player;
                    game.getAvatar().lastAction = ((MovingAvatar) sprite1).lastAction;
                }catch (ClassCastException e) {}
            }

            game.killSprite(sprite1);
        }
    }
}
