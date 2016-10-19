package ontology.effects.unary;

import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.Types;
import ontology.effects.Effect;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 23/10/13
 * Time: 15:23
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class TurnAround extends Effect
{
    public TurnAround(InteractionContent cnt)
    {
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        sprite1.setRect(sprite1.lastrect);
        sprite1.lastmove = sprite1.cooldown;
        sprite1.physics.activeMovement(sprite1, Types.DDOWN, sprite1.speed);
        sprite1.lastmove = sprite1.cooldown;
        sprite1.physics.activeMovement(sprite1, Types.DDOWN, sprite1.speed);
        game.reverseDirection(sprite1);
        game._updateCollisionDict(sprite1);
    }
}
