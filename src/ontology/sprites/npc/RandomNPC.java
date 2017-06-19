package ontology.sprites.npc;

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
 * Time: 18:08
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class RandomNPC extends VGDLSprite
{
    //Number of consecutive moves the sprite performs.
    public int cons;

    protected int counter;

    protected Direction prevAction;

    public RandomNPC(){}

    public RandomNPC(Vector2d position, Dimension size, SpriteContent cnt)
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
        speed = 1;
        cons = 0;
        is_npc = true;
        is_stochastic = true;
        counter = cons;
        prevAction = Types.DNONE;
    }

    protected Direction getRandomMove(Game game)
    {
        if(counter < cons)
        {
            //Apply previous action (repeat cons times).
            counter++;
            return prevAction.copy();
        }else{
            //Determine a new action
            Direction act = (Direction) Utils.choice(Types.DBASEDIRS, game.getRandomGenerator());
            prevAction = act.copy();
            counter=0;
            return act;
        }
    }

    public void update(Game game)
    {
        super.updatePassive();
        Direction act = getRandomMove(game);
        this.physics.activeMovement(this, act, this.speed);
    }



    public VGDLSprite copy()
    {
        RandomNPC newSprite = new RandomNPC();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        RandomNPC targetSprite = (RandomNPC) target;
        targetSprite.cons = this.cons;
        targetSprite.prevAction = this.prevAction;
        targetSprite.counter = this.counter;
        super.copyTo(targetSprite);
    }
}
