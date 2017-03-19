package ontology.sprites;

import java.awt.Color;
import java.awt.Dimension;

import core.vgdl.VGDLRegistry;
import core.vgdl.VGDLSprite;
import core.content.SpriteContent;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 21/10/13
 * Time: 18:28
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Resource extends Passive
{
    public int value;
    public int limit;
    public int resource_type;
    public String resource_name;

    public Resource(){}

    public Resource(Vector2d position, Dimension size, SpriteContent cnt)
    {
        //Init the sprite
        this.init(position, size);

        //Specific class default parameter values.
        loadDefaults();

        //Resources are a bit special, we need the resource name
        resource_name = cnt.identifier;

        //Parse the arguments.
        this.parseParameters(cnt);

    }

    public void postProcess()
    {
        super.postProcess();
        resource_type = VGDLRegistry.GetInstance().getRegisteredSpriteValue(resource_name);
    }

    protected void loadDefaults()
    {
        super.loadDefaults();
        limit = 2;
        value = 1;
        color = Color.YELLOW;
        resource_type = -1;
        is_resource = true;
    }

    public VGDLSprite copy()
    {
        Resource newSprite = new Resource();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        Resource targetSprite = (Resource) target;
        targetSprite.limit = this.limit;
        targetSprite.value = this.value;
        targetSprite.resource_type = this.resource_type;
        targetSprite.resource_name = this.resource_name;
        super.copyTo(targetSprite);
    }

}
