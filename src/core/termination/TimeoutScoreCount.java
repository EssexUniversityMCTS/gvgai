package core.termination;

import core.content.TerminationContent;
import core.game.Game;

public class TimeoutScoreCount extends Termination
{
    public TimeoutScoreCount(){}

    public TimeoutScoreCount(TerminationContent cnt)
    {
        //Parse the arguments.
        this.parseParameters(cnt);
    }

    @Override
    public boolean isDone(Game game) {
        boolean ended = super.isFinished(game);
        if (ended || game.getGameTick() >= limit ) {
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
                double s = game.getAvatar(i).getScore();
                if (s == maxScore && s != 0) {
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
