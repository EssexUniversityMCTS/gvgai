package ontology.effects;

import core.vgdl.VGDLRegistry;
import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 23/10/13
 * Time: 15:20
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class TimeEffect extends Effect implements Comparable<TimeEffect> {


    /**
     * timer for the effect, -1 by default.
     * Indicates every how many steps this effects is triggered.
     * Set in VGDL.
     */
    public int timer = -1;

    /**
     * Indicates the next time step when this effect will be automatically triggered without collisions.
     * It is set by planExecution().
     */
    public int nextExecution = -1;

    /**
     * Indicates if the effect should be repeated periodically ad infinitum
     */
    public boolean repeating = false;

    /**
     * itype of the sprite that suffers the effects of the delegated Effect.
     */
    public int itype;

    /**
     * True if this is a time effect defined in VGDL using TIME.
     * False if this is defined by an AddTimer effect
     */
    public boolean isNative = true;

    /**
     * The effect itself, that is triggered by this.
     * it's a unary effect (the second sprite is always TIME).
     */
    public Effect delegate;

    public TimeEffect() {
    }

    public TimeEffect(InteractionContent ic, Effect delegate) {
        this.parseParameters(ic);
        this.delegate = delegate;

        if (ic.object1.equalsIgnoreCase("TIME")) //Depends on where TIME is in the effect.
            this.itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(ic.object2[0]);
        else
            this.itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(ic.object1);

        if (nextExecution != -1)
            planExecution(null);
    }

    public TimeEffect(Effect delegate) {
        this.delegate = delegate;
        this.itype = -1;

        if (nextExecution != -1)
            planExecution(null);
    }


    /**
     * Executes the effect
     *
     * @param sprite1 first sprite of the collision
     * @param sprite2 second sprite of the collision
     * @param game    reference to the game object with the current state.
     */
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game) {
        //If the time effect is not native, we cannot guarantee that the sprite will be there.
        delegate.execute(sprite1, sprite2, game);
        if (repeating)
            planExecution(game);
    }

    public void planExecution(Game game) {
        int base = game == null ? 0 : game.getGameTick();
        nextExecution = base + timer;
    }

    @Override
    public int compareTo(TimeEffect o) {

        if (this == o)
            return 0;

        if (this.nextExecution < o.nextExecution) return -1;   //'this' executes first.
        if (this.nextExecution > o.nextExecution) return 1;   //'this' executes second.
        return -1; //by default, with the same ordering.
    }

    public TimeEffect copy() {
        TimeEffect tef = new TimeEffect();
        tef.is_kill_effect = this.is_kill_effect;
        tef.is_stochastic = this.is_stochastic;
        tef.scoreChange = this.scoreChange;
        tef.prob = this.prob;
        tef.applyScore = this.applyScore;
        tef.repeat = this.repeat;
        tef.timer = this.timer;
        tef.nextExecution = this.nextExecution;
        tef.itype = this.itype;
        tef.repeating = this.repeating;
        tef.delegate = this.delegate;
        tef.isNative = this.isNative;
        tef.enabled = this.enabled;

        return tef;
    }


    public void copyTo(TimeEffect tef)
    {
        tef.is_kill_effect = this.is_kill_effect;
        tef.is_stochastic = this.is_stochastic;
        tef.scoreChange = this.scoreChange;
        tef.prob = this.prob;
        tef.applyScore = this.applyScore;
        tef.repeat = this.repeat;

        tef.timer = this.timer;
        tef.nextExecution = this.nextExecution;
        tef.itype = this.itype;
        tef.repeating = this.repeating;
        tef.enabled = this.enabled;
    }

}
