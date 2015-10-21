package controllers.Return42.algorithms.GA.genomes;

import controllers.Return42.GameStateCache;
import ontology.Types;

public interface IGenome {
    public int getDepth();

    public Types.ACTIONS getNextAction();

    public void advance();

    public int getAction(int index);

    public void adapt(Genome other);

    public double getScore(GameStateCache stateObs, GameStateCache oldstate);

    public void addDepth(int n);
}
