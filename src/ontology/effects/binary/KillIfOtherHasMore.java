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
 * Time: 15:57
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class KillIfOtherHasMore extends Effect
{
    public String resource;
    public int resourceId;
    public int limit;

    public KillIfOtherHasMore(InteractionContent cnt)
    {
        is_kill_effect = true;
        resourceId = -1;
        this.parseParameters(cnt);
        resourceId = VGDLRegistry.GetInstance().getRegisteredSpriteValue(resource);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        applyScore = false;
        //If 'sprite2' has more than a limit of the resource type given, sprite dies.
        if(sprite2.getAmountResource(resourceId) >= limit)
        {
            applyScore = true;
            game.killSprite(sprite1);
        }
    }
}
