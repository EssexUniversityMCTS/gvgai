package ontology.effects.binary;

import core.VGDLFactory;
import core.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import ontology.effects.Effect;
import ontology.effects.TimeEffect;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/11/13
 * Time: 15:56
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class AddTimer extends TimeEffect
{
    //Name of the effect this TimeEffect inserts in the queue of time effects.
    public String ftype;

    //Effect to be execuced in a number of time steps.
    public Effect timerDelegate;

    public AddTimer() {}

    public AddTimer(InteractionContent cnt)
    {
        this.parseParameters(cnt);

        //We need to build the interaction content for the delegated effect.
        InteractionContent icDelegate = new InteractionContent(cnt.line);
        icDelegate.function = ftype;

        //Create the new effect with the function specified in "ftype"
        timerDelegate = VGDLFactory.GetInstance().createEffect(icDelegate);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        //Adds a timer with the inner effect as delegate.
        TimeEffect tef = new TimeEffect(timerDelegate);
        super.copyTo(tef);
        tef.delegate = timerDelegate;
        tef.itype = -1; //Triggered by time, not by collision. It cannot depend on a particular sprite.
        tef.isNative = false;
        tef.planExecution(game);
        game.addTimeEffect(tef);
    }

    public TimeEffect copy()
    {
        AddTimer newTimer = new AddTimer();
        this.copyTo(newTimer);
        return newTimer;
    }

    public void copyTo(TimeEffect adT)
    {
        AddTimer timer = (AddTimer) adT;
        timer.delegate = this.delegate;
        timer.itype = this.itype;
        timer.isNative = this.isNative;

        super.copyTo(timer);
    }
}
