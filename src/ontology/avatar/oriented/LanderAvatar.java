package ontology.avatar.oriented;

import java.awt.Dimension;

import core.content.SpriteContent;
import core.game.Game;
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
public class LanderAvatar extends OrientedAvatar
{

    public double angle_diff = 0.3;

    public LanderAvatar(){}

    public LanderAvatar(Vector2d position, Dimension size, SpriteContent cnt)
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
        speed=5;
        orientation = Types.DNONE;
    }

    /**
     * This update call is for the game tick() loop.
     * @param game current state of the game.
     */
    public void update(Game game)
    {
        super.update(game);
        aim(game);
        move(game);
    }
    
    public void applyMovement(Game game, Direction action)
    {
    	//this.physics.passiveMovement(this);
    	if (physicstype != 0)
    		super.updatePassive();
    }

    
    public void aim(Game game)
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
    
    public void move(Game game)
    {
    	Direction facing = new Direction(0,0);

    	if (Utils.processMovementActionKeys(getKeyHandler().getMask(), getPlayerID()) == Types.DUP) 
    	{
    		facing = new Direction(Math.cos(this.rotation), Math.sin(this.rotation));
    		this.physics.activeMovement(this, facing, speed);
    	}
    }

}
