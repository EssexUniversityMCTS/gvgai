package ontology.effects;

import core.VGDLFactory;
import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 23/10/13
 * Time: 15:20
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public abstract class Effect
{
    public boolean is_kill_effect = false;

    public boolean is_stochastic = false;

    public int scoreChange = 0;

    public double prob = 1;

    public boolean applyScore = true;

    public abstract void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game);

    public void setStochastic()
    {
        if(prob > 0 && prob < 1)
            is_stochastic = true;
    }

    public void parseParameters(InteractionContent content) {
        //parameters from the object.
        VGDLFactory.GetInstance().parseParameters(content,this);
    }
}
