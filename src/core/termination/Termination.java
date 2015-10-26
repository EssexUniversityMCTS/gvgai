package core.termination;

import core.VGDLFactory;
import core.content.TerminationContent;
import core.game.Game;
import ontology.Types;

import java.awt.event.KeyEvent;
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
        return game.isGameOver();
    }

}
