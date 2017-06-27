package ontology.effects.binary;

import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import core.logging.Logger;
import core.logging.Message;
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
	if(sprite1 == null || sprite2 == null){
	    Logger.getInstance().addMessage(new Message(Message.WARNING, "Neither the 1st nor 2nd sprite can be EOS with KillIfFromAbove interaction."));
	    return;
	}
	
        //Kills the sprite, only if the other one is higher and moving down.
        boolean otherHigher = sprite1.lastrect.getMinY() > (sprite2.lastrect.getMinY()+(sprite2.rect.height/2));
        boolean goingDown = sprite2.rect.getMinY() > sprite2.lastrect.getMinY();

        applyScore=false;
        if (otherHigher && goingDown){
            applyScore=true;
            //boolean variable set to false to indicate the sprite was not transformed
            game.killSprite(sprite1, false);
        }
    }
}
