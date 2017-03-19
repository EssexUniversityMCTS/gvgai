package ontology.sprites;

import java.awt.Dimension;

import core.vgdl.VGDLSprite;
import core.content.SpriteContent;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 24/10/13
 * Time: 10:22
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class ResourcePack extends Resource
{
    public ResourcePack(){}

    public ResourcePack(Vector2d position, Dimension size, SpriteContent cnt)
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
        is_static = true;
    }


    public VGDLSprite copy()
    {
        ResourcePack newSprite = new ResourcePack();
        super.copyTo(newSprite);
        return newSprite;
    }
}
