package ontology.sprites.npc;

import java.awt.Dimension;

import core.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import tools.Utils;
import tools.Vector2d;

/**
 * Created by Diego on 24/02/14.
 */
public class RandomAltChaser extends AlternateChaser{

    public double epsilon;

    public RandomAltChaser(){}

    public RandomAltChaser(Vector2d position, Dimension size, SpriteContent cnt)
    {
        //Init the sprite
        this.init(position, size);

        //Specific class default parameter values.
        loadDefaults();

        //Parse the arguments.
        this.parseParameters(cnt);
    }

    protected void loadDefaults()
    {
        super.loadDefaults();
        epsilon = 0.0;
    }

    public void postProcess()
    {
        super.postProcess();
    }

    public void update(Game game)
    {
        double roll = game.getRandomGenerator().nextDouble();
        if(roll < epsilon)
        {
            //do a sampleRandom move.
            super.updatePassive();
            Vector2d act = (Vector2d) Utils.choice(Types.BASEDIRS, game.getRandomGenerator());
            this.physics.activeMovement(this, act, this.speed);
        }else
        {
            super.update(game);
        }
    }

    public VGDLSprite copy()
    {
        RandomAltChaser newSprite = new RandomAltChaser();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        RandomAltChaser targetSprite = (RandomAltChaser) target;
        targetSprite.epsilon = this.epsilon;
        super.copyTo(targetSprite);
    }

}
