package core.termination;

import java.util.ArrayList;

import core.VGDLRegistry;
import core.content.TerminationContent;
import core.game.Game;
import core.game.GameDescription.TerminationData;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 22/10/13
 * Time: 18:52
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class SpriteCounter extends Termination
{
    public String stype;
    public int itype;
    public boolean count_score = false;

    public SpriteCounter(){}

    public SpriteCounter(TerminationContent cnt)
    {
        //Parse the arguments.
        this.parseParameters(cnt);
        itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
    }

    @Override
    public boolean isDone(Game game) {

        boolean ended = super.isFinished(game);
        if(ended)
            return true;

        if(game.getNumSprites(itype) - game.getNumDisabledSprites(itype) <= limit) {
            if (count_score) {
                double maxScore = game.getAvatar(0).getScore();
                for (int i = 1; i < game.no_players; i++) {
                    double score = game.getAvatar(i).getScore();
                    if (score > maxScore) {
                        maxScore = score;
                    }
                }
                //give win to player/s with most number of points, rest lose
                win = "";
                for (int i = 0; i < game.no_players; i++) {
                    if (game.getAvatar(i).getScore() == maxScore) {
                        win += "True";
                    } else {
                        win += "False";
                    }
                    if (i != game.no_players - 1) {
                        win += ",";
                    }
                }
            }
            return true;
        }

        return false;
    }

	@Override
	public ArrayList<String> getTerminationSprites() {
		ArrayList<String> result = new ArrayList<String>();
		result.add(stype);
		
		return result;
	}

}
