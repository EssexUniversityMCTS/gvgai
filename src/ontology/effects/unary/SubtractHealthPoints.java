package ontology.effects.unary;

import core.VGDLRegistry;
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
public class SubtractHealthPoints extends Effect
{
    public int value; //healthpoints removed from sprite1
    public int limit; //kills sprite1 when less or equal to this value (default=0).

    public SubtractHealthPoints(InteractionContent cnt)
    {
        is_kill_effect = true;
        limit = 0;
        value = 1;
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        sprite1.healthPoints -= value;
        if(sprite1.healthPoints <= limit)
        {
            game.killSprite(sprite1);
        }
    }
}
