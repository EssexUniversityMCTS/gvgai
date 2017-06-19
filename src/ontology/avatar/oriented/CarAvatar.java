package ontology.avatar.oriented;

import java.awt.Dimension;

import core.content.SpriteContent;
import core.game.Game;
import core.vgdl.VGDLSprite;
import ontology.Types;
import tools.Direction;
import tools.Utils;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 22/10/13
 * Time: 18:10
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class CarAvatar extends OrientedAvatar
{

    public double angle_diff = 0.15;
    public double facing = 0;

    public CarAvatar(){}

    public CarAvatar(Vector2d position, Dimension size, SpriteContent cnt)
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
        speed=0;
    }

    /**
     * This update call is for the game tick() loop.
     * @param game current state of the game.
     */
    public void updateAvatar(Game game, boolean requestInput, boolean[] actionMask)
    {
        super.updateAvatar(game, requestInput, actionMask);
        updateUse(game);
        aim();
        move();
    }
    
    public void applyMovement(Game game, Direction action)
    {
    	//this.physics.passiveMovement(this);
    	if (physicstype != 0)
    		super.updatePassive();
    }

    
    public void aim()
    {
    	double angle = this.rotation;

    	if(Utils.processMovementActionKeys(getKeyHandler().getMask(), getPlayerID()) == Types.DLEFT) 
    	{
    		angle -= angle_diff;
    	}
    	else if (Utils.processMovementActionKeys(getKeyHandler().getMask(), getPlayerID()) == Types.DRIGHT) 
    	{
    		angle += angle_diff;
    	}
    	this._updateRotation(angle);
    }
    
    public void move()
    {
    	if (Utils.processMovementActionKeys(getKeyHandler().getMask(), getPlayerID()) == Types.DUP) 
    	{
    		//this.orientation = new Direction(0,0);
    		facing = 0;
    	}
    	else if (Utils.processMovementActionKeys(getKeyHandler().getMask(), getPlayerID()) == Types.DDOWN) 
    	{
    		//this.orientation = new Direction(0,0);
    		facing = 1;
    	}
    	Direction direx =  new Direction(Math.cos(this.rotation+(facing*Math.toRadians(180))), Math.sin(this.rotation+(facing*Math.toRadians(180))));
    	this.physics.activeMovement(this, direx, 5);
    }

    public VGDLSprite copy()
    {
        CarAvatar newSprite = new CarAvatar();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        CarAvatar targetSprite = (CarAvatar) target;
        targetSprite.facing = this.facing;
        targetSprite.angle_diff = this.angle_diff;
        super.copyTo(targetSprite);
    }

}
