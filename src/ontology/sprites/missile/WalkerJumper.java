package ontology.sprites.missile;

import java.awt.Dimension;

import core.VGDLSprite;
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

    public void update(Game game)
    {
    	super.updatePassive();
    	
    	if (this.lastDirection().x == 0)
    	{
    		if (this.probability > Math.random())
    		{
    			Direction dd = new Direction(0,-this.strength);
    			this.physics.activeMovement(this, dd, this.speed);
    		}
    	}
    	super.update(game);
    	
    }
    
    protected void loadDefaults()
    {
        super.loadDefaults();
        probability = 0.1;
        strength = 10;
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
