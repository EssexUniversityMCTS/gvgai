package ontology.effects.unary;

import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import core.logging.Logger;
import core.logging.Message;
import ontology.effects.Effect;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 23/10/13
 * Time: 15:21
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class WrapAround extends Effect {

    public double offset;

    public WrapAround(InteractionContent cnt)
    {
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game) {
	if(sprite1 == null){
	    Logger.getInstance().addMessage(new Message(Message.WARNING, "1st sprite can't be EOS with WrapAround interaction."));
	    return;
	}
	
        if(sprite1.orientation.x() > 0)
        {
            sprite1.rect.x = (int) (offset * sprite1.rect.width);
        }
        else if(sprite1.orientation.x() < 0)
        {
            sprite1.rect.x = (int) (game.getScreenSize().width - sprite1.rect.width * (1+offset));
        }
        else if(sprite1.orientation.y() > 0)
        {
            sprite1.rect.y = (int) (offset * sprite1.rect.height);
        }
        else if(sprite1.orientation.y() < 0)
        {
            sprite1.rect.y = (int) (game.getScreenSize().height- sprite1.rect.height * (1+offset));
        }

        sprite1.lastmove = 0;
    }
}
