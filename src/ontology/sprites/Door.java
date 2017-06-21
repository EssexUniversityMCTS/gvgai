package ontology.sprites;

import java.awt.Dimension;

import core.vgdl.VGDLSprite;
import core.content.SpriteContent;
import tools.Vector2d;

/**
 * Created by diego on 17/02/14.
 */
public class Door extends Immovable
{
    public Door() {}

    public Door(Vector2d position, Dimension size, SpriteContent cnt)
    {
        //Init the sprite
        this.init(position, size);

        //Specific class default parameter values.
        loadDefaults();

        //Parse the arguments.
        this.parseParameters(cnt);
    }

    public void postProcess()
    {
        super.postProcess();
    }

    protected void loadDefaults()
    {
        super.loadDefaults();
        portal = true;
    }

    public VGDLSprite copy()
    {
        Door newSprite = new Door();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        Door targetSprite = (Door) target;
        super.copyTo(targetSprite);
    }


}
