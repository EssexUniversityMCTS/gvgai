package ontology.effects.binary;

import java.util.Collection;

import core.VGDLFactory;
import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.effects.Effect;
import tools.Utils;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/11/13
 * Time: 15:56
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class TeleportToExit extends Effect
{

    public TeleportToExit(InteractionContent cnt)
    {
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        int destinationId = VGDLFactory.GetInstance().requestFieldValueInt(sprite2, "itype");

        Collection<VGDLSprite> sprites = game.getSprites(destinationId).values();

        if(sprites.size() > 0)
        {
            VGDLSprite destination = (VGDLSprite) Utils.choice(sprites.toArray(), game.getRandomGenerator());
            sprite1.setRect(destination.rect);
            sprite1.lastmove = 0;

            if(destination.is_oriented)
            {
                sprite1.orientation = destination.orientation.copy();
            }
        }else{
            //If there is no exit... kill the sprite
            game.killSprite(sprite1);
        }
    }
}
