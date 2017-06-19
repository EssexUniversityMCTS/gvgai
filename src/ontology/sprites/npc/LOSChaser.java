package ontology.sprites.npc;

import core.vgdl.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import tools.Direction;
import tools.Vector2d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

// Line of sigh chaser. Chases the target if it is in its line of sight, otherwise acts like random NPC.

public class LOSChaser extends Chaser
{
    public LOSChaser(){}

    public LOSChaser(Vector2d position, Dimension size, SpriteContent cnt)
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
    }

    public void postProcess()
    {
        super.postProcess();
    }

    /**
     * Sets a list with the closest targets (sprites with the type 'stype'), by distance
     * @param game game to access all sprites
     */
    protected void closestTargets(Game game)
    {
        targets.clear();
        double bestDist = Double.MAX_VALUE;

        Iterator<VGDLSprite> spriteIt = game.getSpriteGroup(itype);
        if(spriteIt == null) spriteIt = game.getSubSpritesGroup(itype); //Try subtypes

        if(spriteIt != null) while(spriteIt.hasNext())
        {
            VGDLSprite s = spriteIt.next();
            double distance = this.physics.distance(rect, s.rect);

            //check if I can see this sprite
            boolean canSee = false;

            if (prevAction == Types.DNONE || prevAction == Types.DNIL) {
                break;
            } else if (prevAction.equals(Types.DDOWN)) {
                if ((s.rect.x == rect.x && s.rect.y >= rect.y)) {
                    canSee = true;
                }
            } else if (prevAction.equals(Types.DUP)) {
                if ((s.rect.x == rect.x && s.rect.y <= rect.y)) {
                    canSee = true;
                }
            } else if (prevAction.equals(Types.DLEFT)) {
                if ((s.rect.x <= rect.x && s.rect.y == rect.y)) {
                    canSee = true;
                }
            } else if (prevAction.equals(Types.DRIGHT)) {
                if ((s.rect.x >= rect.x && s.rect.y == rect.y)) {
                    canSee = true;
                }
            }

            if (canSee) {
                if (distance < bestDist) {
                    bestDist = distance;
                    targets.clear();
                    targets.add(s);
                } else if (distance == bestDist) {
                    targets.add(s);
                }
            }
        }
    }


    public VGDLSprite copy()
    {
        LOSChaser newSprite = new LOSChaser();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        LOSChaser targetSprite = (LOSChaser) target;
        targetSprite.fleeing = this.fleeing;
        targetSprite.stype = this.stype;
        targetSprite.itype = this.itype;
        targetSprite.maxDistance = this.maxDistance;
        targetSprite.targets = new ArrayList<VGDLSprite>();
        targetSprite.actions = new ArrayList<Direction>();
        super.copyTo(targetSprite);
    }
    
    @Override
    public ArrayList<String> getDependentSprites(){
    	ArrayList<String> result = new ArrayList<String>();
    	if(stype != null) result.add(stype);
    	
    	return result;
    }

}
