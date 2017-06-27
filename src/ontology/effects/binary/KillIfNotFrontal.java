package ontology.effects.binary;

import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import core.logging.Logger;
import core.logging.Message;
import ontology.Types;
import ontology.effects.Effect;
import tools.Direction;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/11/13
 * Time: 15:57
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class KillIfNotFrontal extends Effect
{

    public KillIfNotFrontal(InteractionContent cnt)
    {
        is_kill_effect = true;
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
	if(sprite1 == null || sprite2 == null){
	    Logger.getInstance().addMessage(new Message(Message.WARNING, "Neither the 1st nor 2nd sprite can be EOS with KillIfNotFrontal interaction."));
	    return;
	}
	
        //Kills the sprite, only if they are going in opposite directions or sprite1 is static.
        Vector2d firstV = sprite1.lastDirection();
        Vector2d otherV = sprite2.lastDirection();

        firstV.normalise();
        otherV.normalise();

        //If the sum of the two vectors (normalized) is (0.0), directions are opposite.
        Direction sumDir = new Direction(firstV.x + otherV.x, firstV.y + otherV.y);
        Direction firstDir = new Direction(firstV.x, firstV.y);

        applyScore=false;
        if( firstDir.equals(Types.DNONE) || !(sumDir.equals(Types.DNONE)))
        {
            //boolean variable set to false to indicate the sprite was not transformed
            applyScore=true;
            game.killSprite(sprite1, false);
        }

    }
}
