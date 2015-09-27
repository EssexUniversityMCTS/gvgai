package ontology.sprites.producer;

import java.awt.Dimension;

import core.VGDLSprite;
import core.content.SpriteContent;
import ontology.Types;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 21/10/13
 * Time: 18:26
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Bomber extends SpawnPoint
{
    public Bomber(){}

    public Bomber(Vector2d position, Dimension size, SpriteContent cnt)
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
        color = Types.ORANGE;
        is_static = false;
        is_oriented = true;
        orientation = Types.RIGHT.copy();
        is_npc = true;
    }


    public VGDLSprite copy()
    {
        Bomber newSprite = new Bomber();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        Bomber targetSprite = (Bomber) target;
        super.copyTo(targetSprite);
    }
}
