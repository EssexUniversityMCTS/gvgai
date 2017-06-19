package ontology.sprites;

import java.awt.Dimension;

import core.vgdl.VGDLSprite;
import core.content.SpriteContent;
import ontology.Types;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 21/10/13
 * Time: 18:04
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Conveyor extends VGDLSprite
{
    public Conveyor(){}

    public Conveyor(Vector2d position, Dimension size, SpriteContent cnt)
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
        color = Types.BLUE;
        jump_strength = 1;
        draw_arrow = true;
        is_oriented = true;
    }


    public VGDLSprite copy()
    {
        Conveyor newSprite = new Conveyor();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        Conveyor targetSprite = (Conveyor) target;
        super.copyTo(targetSprite);
    }
}
