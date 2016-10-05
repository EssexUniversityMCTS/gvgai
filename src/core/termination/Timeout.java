package core.termination;

import java.util.ArrayList;

import core.content.TerminationContent;
import core.game.Game;
import core.game.GameDescription.TerminationData;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 22/10/13
 * Time: 18:48
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Timeout extends Termination
{
    public boolean use_counter = false;
    public boolean compare = false;
    public String limits = "0";

    public Timeout(){}

    public Timeout(TerminationContent cnt)
    {
        //Parse the arguments.
        this.parseParameters(cnt);
    }

    @Override
    public boolean isDone(Game game)
    {
        boolean ended = super.isFinished(game);
        if(ended) {
            return true;
        }

        if(game.getGameTick() >= limit) {
            countScore(game);

            if (use_counter) {
                //use the master game counter
                if (compare) {
                    //if comparing, the first player wins if they're not equal, the rest win otherwise
                    int first = game.getValueCounter(0);
                    boolean ok = true;
                    for (int i = 1; i < game.getNoCounters(); i++) {
                        if (game.getValueCounter(i) != first) {
                            ok = false;
                        }
                    }
                    if (ok) {
                        win = "False,";
                        for (int i = 1; i < game.getNoPlayers(); i++) {
                            if (i == game.no_players - 1) {
                                win += "True";
                            } else win += "True,";
                        }
                    } else {
                        win = "True,";
                        for (int i = 1; i < game.getNoPlayers(); i++) {
                            if (i == game.no_players - 1) {
                                win += "False";
                            } else win += "False,";
                        }

                    }
                } else {
                    //use the limits, split it and check each counter, idx corresponding to player ID
                    if (game.no_players != game.no_counters) {
                        win = "";
                        for (int i = 0; i < game.no_players; i++) {
                            if (i != game.no_players - 1) win += "False,";
                            else win += "False";
                        }
                    } else {
                        String[] split = limits.split(",");
                        int[] intlimits = new int[split.length];
                        for (int i = 0; i < intlimits.length;i++)
                            intlimits[i] = Integer.parseInt(split[i]);

                        for (int i = 0; i < game.no_players; i++) {
                            win = "";
                            if (game.getValueCounter(i) == intlimits[i]) {
                                win += "True";
                            } else
                                win += "False";
                            if (i != game.no_players - 1) win += ",";
                        }
                    }
                }
            }

            return true;
        }

        return false;
    }
}
