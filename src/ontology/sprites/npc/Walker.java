package ontology.sprites.npc;

import java.awt.*;
import java.util.Random;

import core.vgdl.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.sprites.missile.Missile;
import tools.Direction;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 21/10/13
 * Time: 18:16
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Walker extends VGDLSprite
{
    public boolean airsteering;

    public Walker(){}

    public Walker(Vector2d position, Dimension size, SpriteContent cnt)
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

        double d;
    	if (this.airsteering || this.lastDirection().x == 0){
    		if (this.orientation.x() > 0){
    			d = 1;
    		}
    		else if (this.orientation.x() < 0){
    			d = -1;
    		}
    		else{
    		    int[] choices = new int[]{-1,1};
    			d = choices[game.getRandomGenerator().nextInt(choices.length)];
    		}
    		Direction dir = new Direction(d,0);
    		this.orientation = dir.copy();
    		this.physics.activeMovement(this, dir, this.max_speed);
    	}

    	this.speed = max_speed;
        on_ground = false;
    }

    protected void loadDefaults()
    {
        super.loadDefaults();
        speed = 5;
        max_speed = 5;
        is_oriented = true;
        airsteering = false;
        is_stochastic = true;
        on_ground = false;
    }

    public VGDLSprite copy()
    {
        Walker newSprite = new Walker();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        Walker targetSprite = (Walker) target;
        targetSprite.airsteering = this.airsteering;
        super.copyTo(targetSprite);
    }
}
