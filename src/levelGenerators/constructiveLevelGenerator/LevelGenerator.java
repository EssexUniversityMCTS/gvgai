package levelGenerators.constructiveLevelGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import core.game.GameDescription;
import core.game.GameDescription.SpriteData;
import core.game.GameDescription.TerminationData;
import core.generator.AbstractLevelGenerator;
import levelGenerators.constructiveLevelGenerator.LevelData.Point;
import tools.ElapsedCpuTimer;
import tools.GameAnalyzer;
import tools.Utils;

public class LevelGenerator extends AbstractLevelGenerator{
	private GameAnalyzer gameAnalyzer;
	private Random random;
	private int minSize;
	private int maxSize;
	private double maxCoverPercentage;
	private double coverTradeOffPercentage;
	private double shuffleDirectionPercentage;
	private double levelSizeRandomPercentage;
	private double levelSizeMaxPercentage;
	private double randomnessPercentage;
	private LevelData generatedLevel;
	
	public LevelGenerator(GameDescription game, ElapsedCpuTimer elpasedTimer){
		gameAnalyzer = new GameAnalyzer(game);
		random = new Random();
		
		shuffleDirectionPercentage = 0.2;
		
		minSize = 4;
		maxSize = 18;
		levelSizeRandomPercentage = 0.75;
		levelSizeMaxPercentage = 1.5;
		
		maxCoverPercentage = 0.614;
		randomnessPercentage = 0.1;
		coverTradeOffPercentage = 0.5;
	}
	
	private LevelCoverData getPercentagesCovered(GameDescription game){
		LevelCoverData data = new LevelCoverData();
		
		int numberOfCreationalObjects = 0;
		for(SpriteData sprite:game.getAllSpriteData()){
			if(gameAnalyzer.checkIfSpawned(sprite.name) == 0){
				numberOfCreationalObjects += 1;
			}
		}
		
		double solidValue = 0;
		double harmfulValue = 0;
		double collectableValue = 0;
		double otherValue = 0;
		double totalValue = 0;
		for(SpriteData sprite:game.getAllSpriteData()){
			double value = gameAnalyzer.checkIfSpawned(sprite.name) * 
					(gameAnalyzer.getPriorityNumber(sprite.name) + 1);
			if(gameAnalyzer.getSolidSprites().contains(sprite.name)){
				solidValue += value;
			}
			if(gameAnalyzer.getHarmfulSprites().contains(sprite.name)){
				harmfulValue += value;
			}
			if(gameAnalyzer.getOtherSprites().contains(sprite.name)){
				otherValue += value;
			}
			if(gameAnalyzer.getCollectableSprites().contains(sprite.name)){
				collectableValue += value;
			}
			totalValue += value;
		}
		
		data.levelPercentage = (coverTradeOffPercentage + (1 - coverTradeOffPercentage) / (numberOfCreationalObjects + 1)) * 
				((collectableValue + 1) / (harmfulValue + collectableValue + 1)) * maxCoverPercentage;
		data.levelPercentage = Utils.noise(data.levelPercentage, randomnessPercentage, random.nextDouble());
		
		if(solidValue > 0){
			data.solidPercentage = Math.max(Utils.noise(solidValue / totalValue, randomnessPercentage, random.nextDouble()), 0);
		}
		if(harmfulValue > 0){
			data.harmfulPercentage = Math.max(Utils.noise(harmfulValue / totalValue, randomnessPercentage, random.nextDouble()), 0);
		}
		if(collectableValue > 0){
			data.collectablePercentage = Math.max(Utils.noise(collectableValue / totalValue, randomnessPercentage, random.nextDouble()), 0);
		}
		if(otherValue > 0){
			data.otherPercentage = Math.max(Utils.noise(otherValue / totalValue, randomnessPercentage, random.nextDouble()), 0);
		}
		
		totalValue = data.solidPercentage + data.harmfulPercentage + data.collectablePercentage + data.otherPercentage;
		data.solidPercentage /= totalValue;
		data.harmfulPercentage /= totalValue;
		data.collectablePercentage /= totalValue;
		data.otherPercentage /= totalValue;
		
		return data;
	}
	
	private boolean placeSolid(LevelData level, int x, int y, String solid){
		if(!level.checkConnectivity(x, y)){
			return false;
		}
		level.set(x, y, solid);
		return true;
	}
	
	private void buildLevelLayout(LevelData level, LevelCoverData coverPercentage){
		ArrayList<String> solidSprites = gameAnalyzer.getSolidSprites();
		if(solidSprites.size() > 0){
			String randomSolid = solidSprites.get(random.nextInt(solidSprites.size()));
			for(int x=0; x<level.getWidth(); x++){
				level.set(x, 0, randomSolid);
				level.set(x, level.getHeight() - 1, randomSolid);
			}
			
			for(int y=0; y<level.getHeight(); y++){
				level.set(0, y, randomSolid);
				level.set(level.getWidth() - 1, y, randomSolid);
			}
			
			double solidNumber = coverPercentage.levelPercentage * coverPercentage.solidPercentage * 
					getArea(level);
			
			while(solidNumber > 0){
				ArrayList<Point> freePositions = level.getAllFreeSpots();
				Point randomPoint = freePositions.get(random.nextInt(freePositions.size()));
				solidNumber -= 1;
				if(!placeSolid(level, randomPoint.x, randomPoint.y, randomSolid)){
					continue;
				}
				int length = 2 + random.nextInt(3);
				ArrayList<Point> directions = new ArrayList<Point>(Arrays.asList(new Point[]{new Point(1,0), new Point(-1,0), new Point(0,-1), new Point(0,1)}));
				while(length > 0){
					if(random.nextDouble() < shuffleDirectionPercentage){
						Collections.shuffle(directions);
					}
					int i=0;
					for(i=0; i<directions.size(); i++){
						Point newPoint = new Point(randomPoint.x + directions.get(i).x, randomPoint.y + directions.get(i).y);
						if(level.get(newPoint.x, newPoint.y) == null && 
								placeSolid(level, newPoint.x, newPoint.y, randomSolid)){
							randomPoint.x = newPoint.x;
							randomPoint.y = newPoint.y;
							length -= 1;
							solidNumber -= 1;
							break;
						}
						else{
							continue;
						}
					}
					
					if(i >= directions.size()){
						break;
					}
				}
				
			}
		}
	}
	
	private double getArea(LevelData level){
		if(gameAnalyzer.getSolidSprites().size() > 0){
			return (level.getWidth() - 2) * (level.getHeight() - 2);
		}
		
		return level.getWidth() * level.getHeight();
	}
	
	private ArrayList<Point> getUpperLowerPoints(ArrayList<Point> freePositions){
		ArrayList<Point> result = new ArrayList<Point>();
		int minY = 100000;
		int maxY = 0;
		for(Point p:freePositions){
			if(p.y < minY){
				minY = p.y;
			}
			if(p.y > maxY){
				maxY = p.y;
			}
		}
		
		for(Point p:freePositions){
			if(p.y == minY || p.y == maxY){
				result.add(p);
			}
		}
		
		return result;
	}
	
	private Point addAvatar(LevelData level, GameDescription game){
		ArrayList<Point> freePositions = level.getAllFreeSpots();
		Point randomPoint = freePositions.get(random.nextInt(freePositions.size()));
		ArrayList<SpriteData> avatar = game.getAvatar();
		SpriteData randomAvatar = avatar.get(random.nextInt(avatar.size()));
		
		if(gameAnalyzer.horzAvatar.contains(randomAvatar.type)){
			freePositions = getUpperLowerPoints(freePositions);
			randomPoint = freePositions.get(random.nextInt(freePositions.size()));
		}
		
		level.set(randomPoint.x, randomPoint.y, randomAvatar.name);
		
		return randomPoint;
	}
	
	private HashMap<String, Integer> calculateNumberOfObjects(GameDescription game, LevelData level){
		HashMap<String, Integer> objects = new HashMap<String, Integer>();
		ArrayList<SpriteData> allSprites = game.getAllSpriteData();
		
		for(SpriteData sprite:allSprites){
			objects.put(sprite.name, 0);
		}
		
		for(int y = 0; y < level.getHeight(); y++){
			for(int x = 0; x < level.getWidth(); x++){
				if(objects.containsKey(level.get(x, y))){
					objects.put(level.get(x, y), objects.get(level.get(x, y)) + 1);
				}
				else{
					objects.put(level.get(x, y), 1);
				}
			}
		}
		
		return objects;
	}
	
	private void fixGoals(GameDescription game, LevelData level, LevelCoverData coverPercentage){
		ArrayList<TerminationData> termination = game.getTerminationConditions();
		HashMap<String, Integer> numObjects = calculateNumberOfObjects(game, level);
		
		int totalNum = 0;
		int currentNum = 0;
		int increase = 0;
		
		ArrayList<String> currentSprites = new ArrayList<String>();
		ArrayList<String> totalSprites = new ArrayList<String>();
		ArrayList<Point> positions = level.getAllFreeSpots();
		for(TerminationData ter:termination){
			currentSprites.clear();
			totalNum = ter.limit;
			currentNum = 0;
			for(String stype:ter.sprites){
				if(!gameAnalyzer.getAvatarSprites().contains(stype)){
					currentSprites.add(stype);
					totalSprites.add(stype);
					currentNum += numObjects.get(stype);
				}
			}
			increase = totalNum + 1 - currentNum;
			
			if(currentSprites.size() > 0){
				for(int i = 0; i < increase; i++){
					int index = random.nextInt(positions.size());
					Point pos = positions.remove(index);
					level.set(pos.x, pos.y, currentSprites.get(random.nextInt(currentSprites.size())));
				}
			}
		}
		
		if(totalSprites.size() > 0){
			increase = random.nextInt((int)Math.ceil(positions.size() * 1.0 * 
					gameAnalyzer.getGoalSprites().size() / game.getAllSpriteData().size()));
			for(int i = 0; i < increase; i++){
				int index = random.nextInt(positions.size());
				Point pos = positions.remove(index);
				level.set(pos.x, pos.y, totalSprites.get(random.nextInt(totalSprites.size())));
			}
		}
	}

	private boolean isMoving(GameDescription game, String stype){
		ArrayList<SpriteData> movingSprites = game.getMoving();
		for(SpriteData sprite:movingSprites){
			if(sprite.name.equals(stype)){
				return true;
			}
		}
		
		return false;
	}
	
	private int getFarLocation(ArrayList<Point> freePosition, Point avatarPosition){
		ArrayList<Double> distProb = new ArrayList<Double>();
		double totalValue = 0;
		distProb.add(avatarPosition.getDistance(freePosition.get(0)));
		totalValue += distProb.get(0);
		for(int i=1; i<freePosition.size(); i++){
			double distance = avatarPosition.getDistance(freePosition.get(i));
			distProb.add(distance + distProb.get(i - 1));
			totalValue += distance;
		}
		
		double randomValue = random.nextDouble();
		for(int i=0; i<freePosition.size(); i++){
			distProb.set(i, distProb.get(i) / totalValue);
			if(randomValue < distProb.get(i)){
				return i;
			}
		}
		
		return -1;
	}
	
	private void addHarmfulObjects(GameDescription game, LevelData level, LevelCoverData coverPercentage, Point avatarPosition){
		double numberOfHarmful = coverPercentage.levelPercentage * coverPercentage.harmfulPercentage * 
				getArea(generatedLevel);
		ArrayList<String> harmfulSprites = gameAnalyzer.getHarmfulSprites();
		ArrayList<Point> freePositions = level.getAllFreeSpots();
		while(numberOfHarmful > 0){
			String randomHarm = harmfulSprites.get(random.nextInt(harmfulSprites.size()));
			if(gameAnalyzer.checkIfSpawned(randomHarm) == 0){
				continue;
			}
			if(isMoving(game, randomHarm)){
				int index = getFarLocation(freePositions, avatarPosition);
				if(index != -1){
					Point randPoint = freePositions.get(index);
					level.set(randPoint.x, randPoint.y, randomHarm);
					freePositions.remove(index);
					numberOfHarmful -= 1;
				}
			}
			else{
				int index = random.nextInt(freePositions.size());
				Point randPoint = freePositions.get(index);
				level.set(randPoint.x, randPoint.y, randomHarm);
				freePositions.remove(index);
				numberOfHarmful -= 1;
			}
		}
	}
	
	private void addCollectableObjects(GameDescription game, LevelData level, LevelCoverData coverPercentage){
		double numberOfOther = coverPercentage.levelPercentage * coverPercentage.collectablePercentage * 
				getArea(level);
		ArrayList<String> otherSprites = gameAnalyzer.getCollectableSprites();
		ArrayList<Point> freePositions = level.getAllFreeSpots();
		while(numberOfOther > 0){
			String randomSprite = otherSprites.get(random.nextInt(otherSprites.size()));
			if(gameAnalyzer.checkIfSpawned(randomSprite) == 0){
				continue;
			}
			
			int index = random.nextInt(freePositions.size());
			Point randPoint = freePositions.get(index);
			level.set(randPoint.x, randPoint.y, randomSprite);
			freePositions.remove(index);
			numberOfOther -= 1;
		}
	}
	
	private void addOtherObjects(GameDescription game, LevelData level, LevelCoverData coverPercentage){
		double numberOfOther = coverPercentage.levelPercentage * coverPercentage.otherPercentage * 
				getArea(level);
		ArrayList<String> otherSprites = gameAnalyzer.getOtherSprites();
		ArrayList<Point> freePositions = level.getAllFreeSpots();
		while(numberOfOther > 0){
			String randomSprite = otherSprites.get(random.nextInt(otherSprites.size()));
			if(gameAnalyzer.checkIfSpawned(randomSprite) == 0){
				continue;
			}
			
			int index = random.nextInt(freePositions.size());
			Point randPoint = freePositions.get(index);
			level.set(randPoint.x, randPoint.y, randomSprite);
			freePositions.remove(index);
			numberOfOther -= 1;
		}
	}
	
	public String generateLevel(GameDescription game, ElapsedCpuTimer elapsedTimer, int width, int length){
		generatedLevel = new LevelData(width, length);
		LevelCoverData coverPercentages = getPercentagesCovered(game);
		
		buildLevelLayout(generatedLevel, coverPercentages);
		Point avatarPosition = addAvatar(generatedLevel, game);
		addHarmfulObjects(game, generatedLevel, coverPercentages, avatarPosition);
		addCollectableObjects(game, generatedLevel, coverPercentages);
		addOtherObjects(game, generatedLevel, coverPercentages);
		
		fixGoals(game, generatedLevel, coverPercentages);
		
		return generatedLevel.getLevel();
	}
	
	@Override
	public String generateLevel(GameDescription game, ElapsedCpuTimer elapsedTimer) {
		int size = 0;
		if(gameAnalyzer.getSolidSprites().size() > 0){
			size = 2;
		}
		
		int width = (int)Math.max(minSize + size, game.getAllSpriteData().size() * ((levelSizeMaxPercentage - levelSizeRandomPercentage) + 
				levelSizeRandomPercentage * random.nextDouble()) + size);
		int length = (int)Math.max(minSize + size, game.getAllSpriteData().size() * ((levelSizeMaxPercentage - levelSizeRandomPercentage) + 
				levelSizeRandomPercentage * random.nextDouble()) + size);
		width = (int)Math.min(width, maxSize + size);
		length = (int)Math.min(length, maxSize + size);
		
		return generateLevel(game, elapsedTimer, width, length);
	}

	@Override
	public HashMap<Character, ArrayList<String>> getLevelMapping() {
		return generatedLevel.getLevelMapping();
	}
}
