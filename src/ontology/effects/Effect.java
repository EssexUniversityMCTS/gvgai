package ontology.effects;

import java.util.ArrayList;

import core.vgdl.VGDLFactory;
import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 23/10/13
 * Time: 15:20
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public abstract class Effect{

    //Indicates if this effect kills any sprite
    public boolean is_kill_effect = false;

    //Indicates if this effect has some random element.
    public boolean is_stochastic = false;

    // indicates whether the interactions of this effect should be carried out sequentially or simultaneously
    public boolean sequential = false;

    //Change of the score this effect makes.
    public String scoreChange = "0";

    //Count something
    public boolean count = true;
    public String counter = "0";
    
    //Count something else
    public boolean countElse = true;
    public String counterElse = "0";

    //Probabilty for stochastic effects.
    public double prob = 1;

    //Indicates if this effects changes the score.
    public boolean applyScore = true;

    //Indicates the number of repetitions of this effect. This affects how many times this
    // effect is taken into account at each step. This is useful for chain effects (i.e. pushing
    // boxes in a chain - thecitadel, enemycitadel).
    public int repeat = 1;

    /**
     * 'Unique' hashcode for this effect
     */
    public long hashCode;

    /**
     * Indicates if this effect is enabled or not (default: true)
     */
    public boolean enabled;

    /**
     * Indicates if the effect wishes to take into account all sprites of the second type at once.
     */
    public boolean inBatch = false;

    /**
     * Executes the effect
     *
     * @param sprite1 first sprite of the collision
     * @param sprite2 second sprite of the collision
     * @param game    reference to the game object with the current state.
     */
    public abstract void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game);


    /**
     * Executes the effect to all second sprites at once.
     *
     * @param sprite1       first sprite of the collision
     * @param sprite2list   list of all second sprites of the collision
     * @param game          reference to the game object with the current state.
     * @return the number of sprites considered in the collision
     */
    public int executeBatch(VGDLSprite sprite1, ArrayList<VGDLSprite> sprite2list, Game game) {return -1;}


    public void setStochastic() {
        if (prob > 0 && prob < 1)
            is_stochastic = true;
    }

    public void parseParameters(InteractionContent content) {

        enabled=true;
        //parameters from the object.
        VGDLFactory.GetInstance().parseParameters(content, this);
        hashCode = content.hashCode;
    }

    /**
     * Determine score change for specific player
     * @param playerID - player affected
     * @return - score change
     */
    public int getScoreChange(int playerID) {
        String[] scores = scoreChange.split(",");
        return playerID < scores.length ? Integer.parseInt(scores[playerID]) : Integer.parseInt(scores[0]);
    }

    public int getCounter(int idx) {
        String[] scores = counter.split(",");
        return idx < scores.length ? Integer.parseInt(scores[idx]) : Integer.parseInt(scores[0]);
    }
    
    public int getCounterElse(int idx) {		
    	String[] scores = counterElse.split(",");		
    	return idx < scores.length ? Integer.parseInt(scores[idx]) : Integer.parseInt(scores[0]);		
    }
    
    public ArrayList<String> getEffectSprites(){
    	return new ArrayList<String>();
    }

}
