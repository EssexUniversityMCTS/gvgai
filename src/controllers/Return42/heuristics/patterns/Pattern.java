package controllers.Return42.heuristics.patterns;

import controllers.Return42.GameStateCache;
import ontology.Types;

public interface Pattern {
    public boolean appliesToGame(GameStateCache state);
    public double applies(GameStateCache state);
    public Types.ACTIONS getAction();
}
