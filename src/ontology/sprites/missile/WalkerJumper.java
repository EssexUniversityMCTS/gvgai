package ontology.sprites.missile;

import java.awt.Dimension;

import core.VGDLSprite;
import core.content.SpriteContent;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 21/10/13
 * Time: 18:17
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class WalkerJumper extends Walker
{
    public double probability;

    public WalkerJumper(){}

    public WalkerJumper(Vector2d position, Dimension size, SpriteContent cnt)
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
        probability = 0.1;
        strength = 10;
    }

    public VGDLSprite copy()
    {
        WalkerJumper newSprite = new WalkerJumper();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        WalkerJumper targetSprite = (WalkerJumper) target;
        targetSprite.probability = this.probability;
        super.copyTo(targetSprite);
    }


}
