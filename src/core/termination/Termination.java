package core.termination;

import java.util.ArrayList;

import core.VGDLFactory;
import core.content.TerminationContent;
import core.game.Game;
import core.game.GameDescription;
import ontology.Types;
/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 22/10/13
 * Time: 18:47
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public abstract class Termination {

    public boolean win;
    public int limit;

    public void parseParameters(TerminationContent content)
    {
        VGDLFactory.GetInstance().parseParameters(content,this);
    }

    public abstract boolean isDone(Game game);

    public boolean isFinished(Game game)
    {
        //It's finished if the player pressed ESCAPE or the game is over..
        return game.isGameOver();
    }

    /**
     * Get all sprites that are used to check the termination condition
     * @return all termination condition sprites
     */
    public ArrayList<String> getTerminationSprites(){
    	return new ArrayList<String>();
    }
}
