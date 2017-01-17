package ontology.sprites.missile;

import java.awt.Dimension;
import java.util.Random;

import core.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import tools.Direction;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 21/10/13
 * Time: 18:16
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Walker extends Missile
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
    
    public static int getRandom(int[] array) {
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
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
    			d = getRandom(new int[]{-1,1});
    		}
    		Direction dir = new Direction(d,0);
    		this.physics.activeMovement(this, dir, this.speed);
    	}
    	//super.update(game);
    	
    }

    protected void loadDefaults()
    {
        super.loadDefaults();
        airsteering = false;
        is_stochastic = true;
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
