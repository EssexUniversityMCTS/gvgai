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
public class KillIfHasLess extends Effect
{
    public String resource;
    public int resourceId;
    public int limit;

    public KillIfHasLess(InteractionContent cnt) throws Exception
    {
        is_kill_effect = true;
        resourceId = -1;
        this.parseParameters(cnt);
        resourceId = VGDLRegistry.GetInstance().getRegisteredSpriteValue(resource);
        if(resourceId == -1){
            String[] className = this.getClass().getName().split(".");
            throw new Exception("[" + className[className.length - 1] + "] Undefined sprite " + resource);
        }
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
	if(sprite1 == null){
            String[] className = this.getClass().getName().split(".");
            Logger.getInstance().addMessage(new Message(Message.WARNING, "[" + className[className.length - 1] + "] sprite1 is null."));
            return;
        }
        applyScore = false;
        if(sprite1.getAmountResource(resourceId) <= limit)
        {
            //boolean variable set to false to indicate the sprite was not transformed
            game.killSprite(sprite1, false);
            applyScore = true;
        }
    }
}
