package ontology.sprites.missile;

import java.awt.Dimension;

import core.VGDLSprite;
import core.content.SpriteContent;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 21/10/13
 * Time: 18:16
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Walker extends Missile
{
    public boolean airsteering;

    public Walker(){}

    public Walker(Vector2d position, Dimension size, SpriteContent cnt)
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
        airsteering = false;
        is_stochastic = true;
    }

    public VGDLSprite copy()
    {
        Walker newSprite = new Walker();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        Walker targetSprite = (Walker) target;
        targetSprite.airsteering = this.airsteering;
        super.copyTo(targetSprite);
    }
}
