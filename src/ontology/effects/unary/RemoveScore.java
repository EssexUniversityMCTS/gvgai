package ontology.effects.unary;

import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.avatar.MovingAvatar;
import ontology.effects.Effect;

public class RemoveScore extends Effect {

    //Indicates if the second sprite should be killed.
    public boolean killSecond = false;

    public RemoveScore(InteractionContent cnt)
    {
        this.parseParameters(cnt);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        if(sprite1.is_avatar) {
            MovingAvatar a = (MovingAvatar)sprite1;
            a.setScore(0);
            if(killSecond && sprite2 != null)
                game.killSprite(sprite2, true);
        }
    }

}
