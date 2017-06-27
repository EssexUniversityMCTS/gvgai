package ontology.sprites.npc;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import core.vgdl.VGDLRegistry;
import core.vgdl.VGDLSprite;
import core.content.SpriteContent;
import core.game.Game;
import ontology.Types;
import tools.Direction;
import tools.Utils;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 21/10/13
 * Time: 18:14
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class AlternateChaser extends RandomNPC
{
    public boolean fleeing;
    public String stype1;
    public String stype2;
    public int[] itype1;
    public int[] itype2;

    ArrayList<VGDLSprite> targets;
    ArrayList<Direction> actions;

    public AlternateChaser(){}

    public AlternateChaser(Vector2d position, Dimension size, SpriteContent cnt)
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
        actions = new ArrayList<Direction>();
    }

    public void postProcess()
    {
        super.postProcess();
        //Define actions here.
        String[] stypes1 = stype1.split(",");
        itype1 = new int[stypes1.length];
        String[] stypes2 = stype1.split(",");
        itype2 = new int[stypes2.length];

        for (int i = 0; i<stypes1.length; i++) {
            itype1[i] = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stypes1[i]);
        }

        for (int i = 0; i<stypes2.length; i++) {
            itype2[i] = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stypes2[i]);
        }

    }

    public void update(Game game)
    {
        actions.clear();

        //passive moment.
        super.updatePassive();

        //Get the closest targets
        closestTargets(game, false);
        for(VGDLSprite target : targets)
        {
            //Update the list of actions that moves me towards each target
            movesToward(target);
        }

        //Choose randomly an action among the ones that allows me to chase.
        Direction act;
        if(actions.size() == 0)
        {
            //unless, no actions really take me closer to anybody!
            act = (Direction) Utils.choice(Types.DBASEDIRS,game.getRandomGenerator());
        }else{
            act = Utils.choiceDir(actions, game.getRandomGenerator());
        }

        //Apply the action to move.
        this.physics.activeMovement(this, act, this.speed);
    }

    protected void movesToward(VGDLSprite target)
    {
        double distance = this.physics.distance(rect, target.rect);
        for(Direction act : Types.DBASEDIRS)
        {
            //Calculate the distance if I'd apply this move.
            Rectangle r = new Rectangle(this.rect);
            r.translate((int)act.x(), (int)act.y());
            double newDist = this.physics.distance(r, target.rect);

            //depending on getting me closer/farther, if I'm fleeing/chasing, add move:
            if(fleeing && distance<newDist)
                actions.add(act);
            if(!fleeing && distance>newDist)
                actions.add(act);
        }
    }

    /**
     * Sets a list with the closest targets (sprites with the type 'stype'), by distance
     * @param game game to access all sprites
     * @param randomTarget if true, a random target is added to 'targets'. Otherwise, only the closest one is.
     */
    protected void closestTargets(Game game, boolean randomTarget)
    {
        targets.clear();
        double bestDist = Double.MAX_VALUE;

        int targetSpriteId = -1;
        int numChasing = 0;
        for (int i = 0; i < itype1.length; i++)
            numChasing += game.getNumSprites(itype1[i]);
        int numFleeing = 0;
        for (int i = 0; i < itype2.length; i++)
            numChasing += game.getNumSprites(itype2[i]);

        if(numChasing > numFleeing)
        {
            targetSpriteId = itype1[new Random().nextInt(itype1.length)];
            fleeing = false;
        }else if(numFleeing > numChasing)
        {
            targetSpriteId = itype2[new Random().nextInt(itype2.length)];
            fleeing = true;
        }

        if(targetSpriteId != -1)
        {
            Iterator<VGDLSprite> spriteIt = game.getSubSpritesGroup(targetSpriteId);
            if(spriteIt != null) while(spriteIt.hasNext())
            {
                VGDLSprite s = spriteIt.next();

                if(randomTarget)
                {
                    targets.add(s);
                }else{
                    double distance = this.physics.distance(rect, s.rect);
                    if(distance < bestDist)
                    {
                        bestDist = distance;
                        targets.clear();
                        targets.add(s);
                    }else if(distance == bestDist){
                        targets.add(s);
                    }
                }
            }
        }

        if(randomTarget)
        {
            VGDLSprite sel = targets.get(game.getRandomGenerator().nextInt(targets.size()));
            targets.clear();
            targets.add(sel);
        }
    }


    public VGDLSprite copy()
    {
        AlternateChaser newSprite = new AlternateChaser();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        AlternateChaser targetSprite = (AlternateChaser) target;
        targetSprite.fleeing = this.fleeing;
        targetSprite.stype1 = this.stype1;
        targetSprite.stype2 = this.stype2;
        targetSprite.itype1 = this.itype1.clone();
        targetSprite.itype2 = this.itype2.clone();
        targetSprite.targets = new ArrayList<VGDLSprite>();
        targetSprite.actions = new ArrayList<Direction>();
        super.copyTo(targetSprite);
    }

    @Override
    public ArrayList<String> getDependentSprites(){
    	ArrayList<String> result = new ArrayList<String>();
        String[] stypes1 = stype1.split(",");
        String[] stypes2 = stype1.split(",");

    	if(stypes1.length > 0) {
    	    for (String s : stypes1)
    	        result.add(s);
        }
    	if(stypes2.length > 0) {
            for (String s : stypes2)
                result.add(s);
        }
    	
    	return result;
    }
    
}
