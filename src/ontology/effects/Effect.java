package ontology.effects;

import java.util.ArrayList;

import core.VGDLFactory;
import core.VGDLSprite;
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

    //Change of the score this effect makes.
    public String scoreChange = "0";

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
     * Executes the effect
     *
     * @param sprite1 first sprite of the collision
     * @param sprite2 second sprite of the collision
     * @param game    reference to the game object with the current state.
     */
    public abstract void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game);

    public void setStochastic() {
        if (prob > 0 && prob < 1)
            is_stochastic = true;
    }

    public void parseParameters(InteractionContent content) {
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
        int s = playerID < scores.length ? Integer.parseInt(scores[playerID]) : Integer.parseInt(scores[0]);
        return s;
    }
    
    public ArrayList<String> getEffectSprites(){
    	return new ArrayList<String>();
    }

}
