package ontology.effects.unary;

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
public class AddHealthPoints extends Effect
{
    public int value; //healthpoints added from sprite1
    public boolean killSecond = false;

    public AddHealthPoints(InteractionContent cnt)
    {
        value = 1;
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        if(sprite1.healthPoints + value < sprite1.limitHealthPoints) {
            sprite1.healthPoints += value;

            if (sprite1.healthPoints > sprite1.maxHealthPoints)
                sprite1.maxHealthPoints = sprite1.healthPoints;

            if(killSecond && sprite2 != null)
                game.killSprite(sprite2);
        }
    }
}
