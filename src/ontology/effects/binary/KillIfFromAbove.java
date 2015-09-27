package ontology.effects.binary;

import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.effects.Effect;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/11/13
 * Time: 15:57
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class KillIfFromAbove extends Effect
{

    public KillIfFromAbove(InteractionContent cnt)
    {
        is_kill_effect = true;
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        //Kills the sprite, only if the other one is higher and moving down.
        boolean otherHigher = sprite1.lastrect.getMinY() > sprite2.lastrect.getMinY();
        boolean goingDown = sprite2.rect.getMinY() > sprite2.lastrect.getMinY();

        if (otherHigher && goingDown){
            game.killSprite(sprite1);
        }
    }
}
