package ontology.effects.unary;

import core.vgdl.VGDLRegistry;
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
public class KillIfHasMore extends Effect
{
    public String resource;
    public int resourceId;
    public int limit;

    public KillIfHasMore(InteractionContent cnt) throws Exception
    {
        is_kill_effect = true;
        resourceId = -1;
        this.parseParameters(cnt);
        resourceId = VGDLRegistry.GetInstance().getRegisteredSpriteValue(resource);
        if(resourceId == -1){
            throw new Exception("Undefined sprite " + resource);
        }
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
	if(sprite1 == null){
	    Logger.getInstance().addMessage(new Message(Message.WARNING, "1st sprite can't be EOS with KillIfHasMore interaction."));
	    return;
	}
	
        applyScore = false;
        if(sprite1.getAmountResource(resourceId) >= limit)
        {
            //boolean variable set to false to indicate the sprite was not transformed
            applyScore = true;
            game.killSprite(sprite1, false);
        }
    }
}
