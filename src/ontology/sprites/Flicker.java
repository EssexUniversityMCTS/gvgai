package ontology.sprites;

import java.awt.Dimension;

import core.vgdl.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 17/10/13
 * Time: 12:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Flicker extends VGDLSprite
{
    public int limit;

    public int age;

    public Flicker(){}

    public Flicker(Vector2d position, Dimension size, SpriteContent cnt)
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
        limit = 1;
        age = 0;
        color = Types.RED;
    }

    public void update(Game game)
    {
        super.update(game);

        if(age > limit)
            //boolean variable set to false to indicate the sprite was not transformed
            game.killSprite(this, false);
        age++;

    }

    public VGDLSprite copy()
    {
        Flicker newSprite = new Flicker();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        Flicker targetSprite = (Flicker) target;
        targetSprite.limit = this.limit;
        targetSprite.age = this.age;
        super.copyTo(targetSprite);
    }
}
