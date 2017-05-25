package ontology.effects.binary;

import core.vgdl.VGDLRegistry;
import core.vgdl.VGDLSprite;
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
    public boolean subtract;

    public KillIfOtherHasMore(InteractionContent cnt)
    {
        is_kill_effect = true;
        resourceId = -1;
        subtract = false;
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
            //boolean variable set to false to indicate the sprite was not transformed
            game.killSprite(sprite1, false);
            if (subtract)
                sprite2.subtractResource(resourceId, limit);

        }
    }
}
