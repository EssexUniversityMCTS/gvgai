package core.termination;

import core.content.TerminationContent;
import core.game.Game;
import ontology.Types;
import ontology.avatar.MovingAvatar;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 22/10/13
 * Time: 18:48
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class TimeoutMultiScore extends Termination
{
    public TimeoutMultiScore(){}

    public TimeoutMultiScore(TerminationContent cnt)
    {
        //Parse the arguments.
        this.parseParameters(cnt);
    }

    @Override
    public boolean isDone(Game game) {
        boolean ended = super.isFinished(game);
        if (ended || game.getGameTick() >= limit ) {
            double maxScore = game.getAvatar(0).getScore();
            int id = 0;
            for (int i = 1; i < game.no_players; i++) {
                double score = game.getAvatar(i).getScore();
                if (score > maxScore) {
                    id = i;
                    maxScore = score;
                }
            }
            //give win to player/s with most number of points, rest lose
            win = "";
            for (int i = 0; i < game.no_players; i++) {
                if (i == id) {
                    win += "True";
                } else {
                    win += "False";
                }
                if (i != game.no_players - 1) {
                    win += ",";
                }
            }
            return true;
        }
        return false;
    }
}
