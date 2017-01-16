package ontology.avatar.oriented;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;

import core.VGDLRegistry;
import core.VGDLSprite;
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
public class AimedAvatar extends ShootAvatar
{

    public double angle_diff = 0.15;

    public AimedAvatar(){}

    public AimedAvatar(Vector2d position, Dimension size, SpriteContent cnt)
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
        stationary = true;
    }

    /**
     * This update call is for the game tick() loop.
     * @param game current state of the game.
     */
    public void update(Game game)
    {
        super.update(game);
        updateUse(game);
        aim(game);
    }

    
    public void aim(Game game)
    {
    	double angle = 0.0;
    	if(Utils.processMovementActionKeys(getKeyHandler().getMask(), getPlayerID()) == Types.DUP) 
    	{
    		angle = -angle_diff;
    	}
    	else if (Utils.processMovementActionKeys(getKeyHandler().getMask(), getPlayerID()) == Types.DDOWN) 
    	{
    		angle = angle_diff;
    	}
    	double new_x_angle = this.orientation.x()*Math.cos(angle)-this.orientation.y()*Math.sin(angle);
    	double new_y_angle = this.orientation.x()*Math.sin(angle)+this.orientation.y()*Math.cos(angle);
    	this.orientation = new Direction(new_x_angle,new_y_angle);
    	this._updateRotation(Math.atan2(this.orientation.y(),this.orientation.x()));
    }

}
