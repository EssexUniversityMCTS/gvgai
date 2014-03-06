package ontology.effects.unary;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.avatar.MovingAvatar;
import ontology.effects.Effect;
import ontology.sprites.Resource;

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
            if(newSprite.is_oriented && sprite1.is_oriented)
            {
                newSprite.orientation = sprite1.orientation;
            }

            if(sprite1.is_avatar)
            {
                try{
                    game.setAvatar((MovingAvatar) newSprite);
                    game.getAvatar().player = ((MovingAvatar) sprite1).player;
                }catch (ClassCastException e) {}
            }

            game.killSprite(sprite1);
        }
    }
}
