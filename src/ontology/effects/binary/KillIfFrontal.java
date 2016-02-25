package ontology.effects.binary;

import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.Types;
import ontology.effects.Effect;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/11/13
 * Time: 15:57
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class KillIfFrontal extends Effect
{

    public KillIfFrontal(InteractionContent cnt)
    {
        is_kill_effect = true;
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        //Kills the sprite, only if they are going in opposite directions or sprite1 is static.
        Vector2d firstDir = sprite1.lastDirection();
        Vector2d otherDir = sprite2.lastDirection();

        firstDir.normalise();
        otherDir.normalise();

        //If the sum of the two vectors (normalized) is (0.0), directions are opposite.
        Vector2d sumDir = new Vector2d(firstDir.x + otherDir.x, firstDir.y + otherDir.y);

        if( firstDir.equals(Types.NONE) || (sumDir.equals(Types.NONE)))
        {
            game.killSprite(sprite1);
        }

    }
}
