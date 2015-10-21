package controllers.Return42.knowledgebase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import controllers.Return42.hashing.ResourcePositionHasher;
import controllers.Return42.hashing.StaticPositionHasher;
import controllers.Return42.knowledgebase.observation.ScoreObserver;
import controllers.Return42.knowledgebase.observation.ScoreProperty;
import controllers.Return42.knowledgebase.observation.WalkableSpace;
import controllers.Return42.knowledgebase.observation.WalkableSpaceObserver;
import controllers.Return42.util.BiKey;
import controllers.Return42.util.Position;
import controllers.Return42.util.StateObservationUtils;
import tools.Vector2d;
import core.game.Observation;
import core.game.StateObservation;

/**
 * 
 * @author Frederik Buss-Joraschek
 *
 */
public class ScoreHeightMapGenerator extends Observable implements Observer {
	public static final double SCORE_FACTOR = 40.0;
	private WalkableSpaceGenerator walkableSpaceGenerator;
	private ScoreObserver scoreObserver;
	
	private final static int HIGHEST_TYPE_ID_SAFETY_MARGIN = 10; // Defines how pessimistic the system should be when guessing the highest possible type id
	
	
	// Resources Hash -> World Hash -> HeightMap
	private Map<BiKey<Integer, Integer>, double[][]> scoreHeightMaps;
	private StaticPositionHasher staticPositionHasher;
	private ResourcePositionHasher resourcePositionHasher;
	private GameInformation gameInformation;
	
	public ScoreHeightMapGenerator( GameInformation gameInformation, WalkableSpaceGenerator walkableSpaceGenerator, ScoreObserver scoreObserver, WalkableSpaceObserver walkableSpaceObserver) {
		this.walkableSpaceGenerator = walkableSpaceGenerator;
		this.scoreObserver = scoreObserver;
		this.staticPositionHasher = new StaticPositionHasher();
		this.resourcePositionHasher = new ResourcePositionHasher();
		this.gameInformation = gameInformation;
		scoreObserver.addObserver(this);
		walkableSpaceObserver.addObserver(this);
		
		
		scoreHeightMaps = new HashMap<>();
	}
	
	public double[][] getHeightMap(StateObservation stateObs){
		int resourcesHashCode = stateObs.getAvatarResources().hashCode();
		final int prime = 47;
		int hash = staticPositionHasher.hash(stateObs);
		hash = hash * prime + resourcePositionHasher.hash(stateObs);
		hash = hash * prime + StateObservationUtils.getAvatarType(stateObs);

		BiKey<Integer, Integer> key = new BiKey<>(resourcesHashCode, hash);
		double[][] heightMap = scoreHeightMaps.get(key);
		if(heightMap != null){
			return heightMap;
		}
		
		TypeInformation typeInformation = gameInformation.getTypeInformation();
		int avatarId = StateObservationUtils.getAvatarType(stateObs);
		
		ArrayList<Observation>[][] observationGrid = stateObs.getObservationGrid();
		heightMap = new double[observationGrid.length][observationGrid[0].length];
		
		Map<Integer, ScoreProperty> scoringTypesMap = scoreObserver.getScoringTypes(stateObs.getAvatarResources(),avatarId );
		double scores[] = new double[typeInformation.getHighestTypeId() + 1 + HIGHEST_TYPE_ID_SAFETY_MARGIN];
		for(int i = 0; i < typeInformation.getHighestTypeId() + 1; i++){
			ScoreProperty entry = scoringTypesMap.get(i);
			SpriteType spriteType = typeInformation.getType(i);
			if(spriteType != SpriteType.TYPE_STATIC && spriteType != SpriteType.TYPE_RESOURCE && spriteType != SpriteType.TYPE_PORTAL ){
				continue;
			}
			if (entry == null || entry.needsExploration()) {
				scores[i] = 1;
			} else if (entry.getScore() > 0){
				scores[i] = entry.getScore();
			}
		}
		
		LinkedList<Position> initalPositions = new LinkedList<Position>();
		WalkableSpace[][] walkableSpace = walkableSpaceGenerator.getWalkableSpace(stateObs, avatarId);
		
		searchInitalPositions(stateObs.getImmovablePositions(), initalPositions, scores, heightMap);
		searchInitalPositions(stateObs.getResourcesPositions(), initalPositions, scores, heightMap);
		searchInitalPositions(stateObs.getPortalsPositions(), initalPositions, scores, heightMap);
		
		ValuesSpreader spreader = new ValuesSpreader(walkableSpace);
		spreader.spreadValues(heightMap, initalPositions);

		scoreHeightMaps.put(key, heightMap);
		return heightMap;
	}

	public void searchInitalPositions(ArrayList<Observation>[] positions, List<Position> openPositions, double[] scores, double[][] heightMap) {
		if (positions != null) {
			for (ArrayList<Observation> elementsOfType : positions) {
				if (elementsOfType.size() > 0) {
					double score =  scores[elementsOfType.get(0).itype];
					if(score > 0){
						for (Observation obs : elementsOfType) {
							Vector2d gridPosition = gameInformation.gamePositionToGridPosition(obs.position);
							int x = (int) gridPosition.x;
							int y = (int) gridPosition.y;
							heightMap[x][y] = score * SCORE_FACTOR;
							openPositions.add(new Position(x, y));
						}
					}
				}
			}
		}
	}
	
	@Override
	public void update(Observable o, Object arg) {
		scoreHeightMaps.clear();
		setChanged();
		notifyObservers();
	}
}
