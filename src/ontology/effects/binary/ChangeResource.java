package ontology.effects.binary;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.effects.Effect;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/11/13
 * Time: 13:25
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class ChangeResource extends Effect
{
    public String resource;
    public int resourceId;
    public int value;
    public boolean killResource;

    public ChangeResource(InteractionContent cnt)
    {
        value=1;
        resourceId = -1;
        killResource = false;
        this.parseParameters(cnt);
        resourceId = VGDLRegistry.GetInstance().getRegisteredSpriteValue(resource);
        is_kill_effect = killResource;
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game) {
        int numResources = sprite1.getAmountResource(resourceId);
        if(numResources + value <= game.getResourceLimit(resourceId))
        {
            sprite1.modifyResource(resourceId, value);

            if(killResource)
                game.killSprite(sprite2);
        }
    }
}
