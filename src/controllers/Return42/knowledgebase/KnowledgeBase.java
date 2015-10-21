package controllers.Return42.knowledgebase;

import controllers.Return42.algorithms.GameClassification;
import controllers.Return42.knowledgebase.observation.CombinedGameObserver;
import controllers.Return42.knowledgebase.observation.EffectObserver;
import controllers.Return42.knowledgebase.observation.GameObserver;
import controllers.Return42.knowledgebase.observation.NPCSpawnerObserver;
import controllers.Return42.knowledgebase.observation.ScoreObserver;
import controllers.Return42.knowledgebase.observation.TypeInformationObserver;
import controllers.Return42.knowledgebase.observation.WalkableSpaceObserver;
import core.game.StateObservation;

/**
 * Facade class for simplified use of the KnowldeBase 
 */
public class KnowledgeBase {

	private final GameInformation gameInformation;
	private final GameClassification gameClassification;
	private final WalkableSpaceGenerator walkableSpaceGenerator;
	private final ScoreHeightMapGenerator scoreHeightMapGenerator;
	private final DistanceMapGenerator distanceMapGenerator;
	private final ScoreObserver scoreObserver;
	private final GameObserver observer;
	private final NPCSpawnerObserver npcSpawnerObserver;
	private final EffectObserver effectObserver;
	
	public KnowledgeBase() {
		this.gameInformation = new GameInformation();
		this.gameClassification = new GameClassification();

		WalkableSpaceObserver walkableSpaceObserver = new WalkableSpaceObserver( gameInformation );
		scoreObserver = new ScoreObserver( gameInformation );
		TypeInformationObserver typeInformationObserver = new TypeInformationObserver( gameInformation );
		npcSpawnerObserver = new NPCSpawnerObserver(gameInformation);
		effectObserver = new EffectObserver();
		
		this.observer = new CombinedGameObserver( 
				walkableSpaceObserver,
				scoreObserver,
				typeInformationObserver,
				npcSpawnerObserver,
				effectObserver
		);

		this.walkableSpaceGenerator = new WalkableSpaceGenerator( gameInformation, walkableSpaceObserver);
		this.scoreHeightMapGenerator = new ScoreHeightMapGenerator( gameInformation, walkableSpaceGenerator, scoreObserver, walkableSpaceObserver );
		this.distanceMapGenerator = new DistanceMapGenerator( walkableSpaceGenerator );
	}
	
	public void initForGame( StateObservation state ) {
		gameInformation.init( state );
		gameClassification.init( state );
	}

	public GameObserver getObserver() {
		return observer;
	}
	
	public GameClassification getGameClassification() {
		return gameClassification;
	}
	
	public GameInformation getGameInformation() {
		return gameInformation;
	}
	
	public WalkableSpaceGenerator getWalkableSpaceGenerator() {
		return walkableSpaceGenerator;
	}
	
	public ScoreHeightMapGenerator getScoreHeightMapGenerator() {
		return scoreHeightMapGenerator;
	}

	public DistanceMapGenerator getDistanceMapGenerator() {
		return distanceMapGenerator;
	}

	public ScoreObserver getScoreObserver() {
		return scoreObserver;
	}
	
	public NPCSpawnerObserver getNPCSpawnerObserver() {
		return npcSpawnerObserver;
	}
	
	public EffectObserver getEffectObserver() {
		return effectObserver;
	}
}
