package ontology.sprites.npc;

import core.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import tools.Utils;
import tools.Vector2d;
import tools.pathfinder.Node;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 21/10/13
 * Time: 18:14
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class PathAltChaser extends AlternateChaser
{
    private Vector2d lastKnownTargetPosition;

    public PathAltChaser(){}

    public PathAltChaser(Vector2d position, Dimension size, SpriteContent cnt)
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
        fleeing = false;
        targets = new ArrayList<VGDLSprite>();
        actions = new ArrayList<Vector2d>();
        lastKnownTargetPosition = null;
    }

    public void postProcess()
    {
        super.postProcess();
        //Define actions here.
    }

    public void update(Game game)
    {
        actions.clear();

        //passive moment.
        super.updatePassive();

        //Get the closest targets
        closestTargets(game);

        Vector2d act = Types.NONE;
        if(!fleeing && targets.size() > 0)
        {
            //If there's a target, get the path to it and take the first action.
            VGDLSprite target = targets.get(0);
            ArrayList<Node> path = game.getPath(this.getPosition(), target.getPosition());

            if(path==null && lastKnownTargetPosition!=null)
            {
                //System.out.println("Recalculating to " + lastKnownTargetPosition);
                path = game.getPath(this.getPosition(), lastKnownTargetPosition);
            }else{
                lastKnownTargetPosition = target.getPosition().copy();
            }

            if(path!=null && path.size()>0)
            {
                //lastKnownTargetPosition = target.getPosition().copy();
                act = path.get(0).comingFrom;
            }

        }else
        {
            for(VGDLSprite target : targets)
            {
                //Update the list of actions that moves me towards each target
                // (this includes fleeing)
                movesToward(target);
            }

            //Choose randomly an action among the ones that allows me to chase.
            if(actions.size() == 0)
            {
                //unless, no actions really take me closer to anybody!
                act = (Vector2d) Utils.choice(Types.BASEDIRS,game.getRandomGenerator());
            }else{
                act = Utils.choice(actions,game.getRandomGenerator());
            }
        }

        //Apply the action to move.
        this.physics.activeMovement(this, act, this.speed);
    }





    public VGDLSprite copy()
    {
        PathAltChaser newSprite = new PathAltChaser();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        PathAltChaser targetSprite = (PathAltChaser) target;
        targetSprite.fleeing = this.fleeing;
        targetSprite.targets = new ArrayList<VGDLSprite>();
        targetSprite.actions = new ArrayList<Vector2d>();
        targetSprite.lastKnownTargetPosition = lastKnownTargetPosition != null ?
                        lastKnownTargetPosition.copy() : null;
        super.copyTo(targetSprite);
    }

}
