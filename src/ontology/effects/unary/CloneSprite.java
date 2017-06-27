package ontology.effects.unary;

import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import core.logging.Logger;
import core.logging.Message;
import ontology.effects.Effect;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 23/10/13
 * Time: 15:21
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class CloneSprite extends Effect {

    public CloneSprite(InteractionContent cnt)
    {
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game) {
	if(sprite1 == null){
	    Logger.getInstance().addMessage(new Message(Message.WARNING, "1st sprite can't be EOS with CloneSprite interaction."));
	    return;
	}
	
        int itype = sprite1.getType();
        Vector2d pos = sprite1.getPosition();
        game.addSprite(itype, pos);
    }
}
