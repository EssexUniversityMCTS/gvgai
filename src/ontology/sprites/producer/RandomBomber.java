package ontology.sprites.producer;

import java.awt.Dimension;

import core.vgdl.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import tools.Direction;
import tools.Utils;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 21/10/13
 * Time: 18:26
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class RandomBomber extends SpawnPoint
{
    public RandomBomber(){}

    public RandomBomber(Vector2d position, Dimension size, SpriteContent cnt)
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
        orientation = Types.DRIGHT.copy();
        is_npc = true;
        is_stochastic = true;
        speed = 1.0;
    }

    public void update(Game game)
    {
        Direction act = (Direction) Utils.choice(Types.DBASEDIRS, game.getRandomGenerator());
        this.physics.activeMovement(this, act, this.speed);
        super.update(game);
    }

    public VGDLSprite copy()
    {
        RandomBomber newSprite = new RandomBomber();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        RandomBomber targetSprite = (RandomBomber) target;
        super.copyTo(targetSprite);
    }
}
