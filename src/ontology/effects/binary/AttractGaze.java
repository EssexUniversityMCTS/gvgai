package ontology.effects.binary;

import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.effects.Effect;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/11/13
 * Time: 15:56
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class AttractGaze extends Effect
{
    public AttractGaze(InteractionContent cnt)
    {
        this.parseParameters(cnt);
        setStochastic();
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        if(sprite1.is_oriented && sprite2.is_oriented)
        {
            if(game.getRandomGenerator().nextDouble() < prob)
                sprite1.orientation = sprite2.orientation.copy();
        }
    }
}
