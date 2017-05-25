package ontology.sprites.npc;

import java.awt.Dimension;

import core.vgdl.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import tools.Direction;
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

    /**
     * Overwritting intersects to check if we are on ground.
     * @return true if it directly intersects with sp (as in the normal case), but additionally checks for on_ground condition.
     */
    public boolean intersects (VGDLSprite sp)
    {
        return this.groundIntersects(sp);
    }


    public void update(Game game)
    {
    	super.updatePassive();
    	
    	if (on_ground && this.probability > Math.random())
        {
            Direction dd = new Direction(0,-this.jump_strength);
            this.orientation = new Direction (this.orientation.x(),0.0);
            this.physics.activeMovement(this, dd, this.speed);

            Direction temp = new Direction (0,-1);
            lastmove = cooldown; //need this to force this movement.
            this._updatePos(temp, 5);

        }else{
            this.physics.activeMovement(this, this.orientation, this.speed);
        }

        on_ground = false;
    }
    
    protected void loadDefaults()
    {
        super.loadDefaults();
        probability = 0.1;
        jump_strength = 1;
        max_speed = 5.0;
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
