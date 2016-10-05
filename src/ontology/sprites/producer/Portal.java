package ontology.sprites.producer;

import java.awt.Dimension;
import java.util.ArrayList;

import core.VGDLRegistry;
import core.VGDLSprite;
import core.content.SpriteContent;
import ontology.Types;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 21/10/13
 * Time: 18:23
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Portal extends SpriteProducer
{
    public String stype;
    public int itype;

    public Portal(){}

    public Portal(Vector2d position, Dimension size, SpriteContent cnt)
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
        is_static = true;
        portal = true;
        color = Types.BLUE;
    }

    public void postProcess()
    {
        super.postProcess();
        itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
    }

    public VGDLSprite copy()
    {
        Portal newSprite = new Portal();
        this.copyTo(newSprite);
        return newSprite;
    }

    public void copyTo(VGDLSprite target)
    {
        Portal targetSprite = (Portal) target;
        targetSprite.stype = this.stype;
        targetSprite.itype = this.itype;
        super.copyTo(targetSprite);
    }
    
    @Override
    public ArrayList<String> getDependentSprites(){
    	ArrayList<String> result = new ArrayList<String>();
    	if(stype != null) result.add(stype);
    	
    	return result;
    }
}
