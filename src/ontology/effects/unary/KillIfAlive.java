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
public class KillIfAlive extends Effect {

    public KillIfAlive(InteractionContent cnt)
    {
        is_kill_effect = true;
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game) {
	if(sprite1 == null || sprite2 == null){
            Logger.getInstance().addMessage(new Message(Message.WARNING, "Neither 1st not 2nd sprite can be EOS with KillIfAlive interaction."));
            return;
        }
	
        //boolean variable set to false to indicate the sprite was not transformed
    	if (!game.kill_list.contains(sprite2))
        	game.killSprite(sprite1, false);
    }
}
