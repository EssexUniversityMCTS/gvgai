package ontology.sprites;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import ontology.physics.ContinuousPhysics;
import ontology.physics.GridPhysics;
import tools.Direction;
import tools.Vector2d;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 21/10/13
 * Time: 18:31
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class AttachedTo extends VGDLSprite
{
    public String stype;

    private VGDLSprite origin; //sprite this is attached to

    public int itype;

    public AttachedTo(){}

    public AttachedTo(Vector2d position, Dimension size, SpriteContent cnt)
    {
        //Init the sprite
        this.init(position, size);

        //Specific class default parameter values.
        loadDefaults();

        //Parse the arguments.
        this.parseParameters(cnt);
    }

    public void postProcess()
    {
        super.postProcess();
        itype = -1;
        if(stype != null) {
            itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
        }
    }

    public void update(Game game)
    {
        super.update(game);
        //update position based on origin
        Rectangle r = lastrect;
        Vector2d v = origin.lastDirection();
        v.normalise();

        int gridsize = 1;
        if(physicstype_id == Types.PHYSICS_GRID)
        {
            GridPhysics gp = (GridPhysics)(physics);
            gridsize = gp.gridsize.width;
        }else if(physicstype_id == Types.PHYSICS_CONT)
        {
            GridPhysics gp = (ContinuousPhysics)(physics);
            gridsize = gp.gridsize.width;
        }

        _updatePos(new Direction(v.x, v.y), (int) (origin.speed * gridsize));
        if(physicstype_id == Types.PHYSICS_CONT)
        {
            speed = origin.speed;
            orientation = origin.orientation;
        }

        lastrect = new Rectangle(r);
    }

    public void setOrigin(VGDLSprite s) {
        origin = s;
    }

    public VGDLSprite copy()
    {
        AttachedTo newSprite = new AttachedTo();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        AttachedTo targetSprite = (AttachedTo) target;
        targetSprite.origin = this.origin;
        super.copyTo(targetSprite);
    }
    
    @Override
    public ArrayList<String> getDependentSprites(){
    	ArrayList<String> result = new ArrayList<String>();
    	if(stype != null) result.add(stype);
    	
    	return result;
    }
}
