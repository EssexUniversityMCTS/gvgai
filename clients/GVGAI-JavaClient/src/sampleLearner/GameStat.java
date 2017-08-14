package sampleLearner;

/**
 * Created by Jialin Liu on 11/08/17.
 * CSEE, University of Essex, UK
 * Email: jialin.liu@essex.ac.uk
 * <p>
 * Respect to Google Java Style Guide:
 * https://google.github.io/styleguide/javaguide.html
 */
public class GameStat {
    public double alpha = 2;
    public int level_;
    public int plays_;
    public double mean_score_;

    public GameStat (int level) {
        this.level_ = level;
        this.plays_ = 0;
        mean_score_ = 0;
    }

    public double getMeanScore() {
        return mean_score_;
    }

    public void addScoreRecord(double score) {
        mean_score_ = (mean_score_*plays_ + score) / (plays_+1);
        plays_++;
    }

    public double calculateUCB(int total_times) {
        return fitness(mean_score_) + Math.sqrt(alpha*Math.log(total_times)/plays_);
    }

    public double fitness(double score) {
        return (GameSelector.upper_bound - score)
            / ( GameSelector.upper_bound -  GameSelector.lower_bound);
    }
}

