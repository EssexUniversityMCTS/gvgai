package sampleLearner;

/**
 * Created by Jialin Liu on 11/08/17.
 * CSEE, University of Essex, UK
 * Email: jialin.liu@essex.ac.uk
 * <p>
 * Respect to Google Java Style Guide:
 * https://google.github.io/styleguide/javaguide.html
 */
public class GameSelector {
    public GameStat[] game_stats_;
    public static double upper_bound;
    public static double lower_bound;
    public int total_plays_;

    public GameSelector(){
        game_stats_ = new GameStat[3];
        for (int i=0; i<3; i++) {
            game_stats_[i] = new GameStat(i);
        }
        total_plays_ = 0;
    }

    public int selectLevel() {
        int selected_level = 0;
        double best_score = game_stats_[0].calculateUCB(total_plays_);
        for (int i=1; i<3; i++) {
            double tmp = game_stats_[i].calculateUCB(total_plays_);
            if (tmp > best_score) {
                best_score = tmp;
                selected_level = i;
            }
        }
        return selected_level;
    }

    public void updateBounds(double score) {
        if (score < lower_bound) {
            lower_bound = score;
        }
        if (score > upper_bound) {
            upper_bound = score;
        }
    }

    public void addScore(int level, double score) {
        this.game_stats_[level].addScoreRecord(score);
        updateBounds(score);
        total_plays_++;
    }

    public int getTotalPlays() {
        return total_plays_;
    }
}
