package ontology.sprites.missile;

import java.awt.Dimension;

import core.vgdl.VGDLSprite;
import core.content.SpriteContent;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 21/10/13
 * Time: 18:19
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class ErraticMissile extends Missile
{
    public ErraticMissile(){}

    public ErraticMissile(Vector2d position, Dimension size, SpriteContent cnt)
    {
        //Init the sprite
        this.init(position, size);

        //Specific class default parameter values.
        loadDefaults();

        //Parse the arguments.
        this.parseParameters(cnt);

        System.out.println("WARNING: ErraticMissile.java, this class must set prob value, " +
          "and is_stochastic must be adjusted according to the value in the parameters is_stochastic=(>0 && <1)");
    }

    protected void loadDefaults()
    {
        super.loadDefaults();
    }

    public VGDLSprite copy()
    {
        ErraticMissile newSprite = new ErraticMissile();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        ErraticMissile targetSprite = (ErraticMissile) target;
        super.copyTo(targetSprite);
    }
}
