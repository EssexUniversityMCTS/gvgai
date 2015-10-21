package controllers.Return42.heuristics.action;

import ontology.Types.ACTIONS;
import tools.Vector2d;
import controllers.Return42.knowledgebase.GameInformation;
import controllers.Return42.knowledgebase.KnowledgeBase;
import controllers.Return42.knowledgebase.ScoreHeightMapGenerator;
import controllers.Return42.util.Util;
import core.game.StateObservation;

public class HeightMapHeuristic implements ActionHeuristic {

	private final ScoreHeightMapGenerator scoreHeightMapGenerator;
	private final GameInformation gameInformation;
	
	int biggestWorldEdgeSize;

	public HeightMapHeuristic( KnowledgeBase knowledge ) {
		this.scoreHeightMapGenerator = knowledge.getScoreHeightMapGenerator();
		this.gameInformation = knowledge.getGameInformation();
	}

	@Override
	public double evaluate(StateObservation state, ACTIONS action) {	
		Vector2d newPosition = Util.translateAvatar(state, action, false);
		Vector2d gridPosition = gameInformation.gamePositionToGridPosition(newPosition);
		double[][] heightMap = scoreHeightMapGenerator.getHeightMap(state);

		double score = heightMap[(int)gridPosition.x][(int)gridPosition.y];
		return score;
	}

}
