package tracks.levelGeneration.constructiveLevelGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import core.game.GameDescription;
import core.game.GameDescription.SpriteData;
import core.game.GameDescription.TerminationData;
import core.generator.AbstractLevelGenerator;
import tracks.levelGeneration.constructiveLevelGenerator.LevelData.Point;
import tools.ElapsedCpuTimer;
import tools.GameAnalyzer;
import tools.Utils;

public class LevelGenerator extends AbstractLevelGenerator{


	/**
	 * object for game analyzer
	 */
	private GameAnalyzer gameAnalyzer;
	/**
	 * random object 
	 */
	private Random random;
	/**
	 * minimum dimension for the level
	 */
	private int minSize;
	/**
	 * maximum dimension of the level
	 */
	private int maxSize;
	/**
	 * maximum cover percentage
	 */
	private double maxCoverPercentage;
	/**
	 * tradeoff between intended percentage of objects and 
	 * the generated one from the number of created objects
	 */
	private double coverTradeOffPercentage;
	/**
	 * percentage of changing direction during creating walls
	 */
	private double shuffleDirectionPercentage;
	/**
	 * level size randomness percentages
	 */
	private double levelSizeRandomPercentage;
	private double levelSizeMaxPercentage;
	/**
	 * object percentage randomness
	 */
	private double randomnessPercentage;
	/**
	 * object of leveldata to hold the current constructed level
	 */
	private LevelData generatedLevel;
	
	/**
	 * Initialize all the parameters for the level generator
	 * @param game			game description object that define the current game
	 * @param elpasedTimer	the amount of time that the constructor have
	 */

	public LevelGenerator(GameDescription game, ElapsedCpuTimer elpasedTimer){
		gameAnalyzer = new GameAnalyzer(game);
		random = new Random();
		
		shuffleDirectionPercentage = 0.2;
		
		minSize = 4;
		maxSize = 18;
		levelSizeRandomPercentage = 0.75;
		levelSizeMaxPercentage = 1.5;
		maxCoverPercentage = 0.1;
		randomnessPercentage = 0.05;
		coverTradeOffPercentage = 0.8;
	}
	
	/**
	 * Calculate the percentage covered from the level and 
	 * percentages of each data type
	 * @param game	game description object provided by the system
	 * @return		level cover data object contain all the calculated information
	 */
	private LevelCoverData getPercentagesCovered(GameDescription game){
		LevelCoverData data = new LevelCoverData();
		
		//calculate the number of objects spawned by other objects
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

			//number of solid objects (priority value, if not spawned and 0 otherwise)
			if(gameAnalyzer.getSolidSprites().contains(sprite.name)){
				solidValue += value;
			}
			//number of harmful sprites (priority value, if not spawned and 0 otherwise)
			if(gameAnalyzer.getHarmfulSprites().contains(sprite.name)){
				harmfulValue += value;
			}
			//number of other sprites (priority value, if not spawned and 0 otherwise)
			if(gameAnalyzer.getOtherSprites().contains(sprite.name)){
				otherValue += value;
			}
			//number of collectable sprites  (priority value, if not spawned and 0 otherwise)
			if(gameAnalyzer.getCollectableSprites().contains(sprite.name)){
				collectableValue += value;
			}
			//overall total of all kind of sprites
			totalValue += value;
		}
		
		//covered percentage is inversely proportional with number of spawned objects 
		//and harmful objects but directly proportional with collectable objects
		data.levelPercentage = (coverTradeOffPercentage + (1 - coverTradeOffPercentage) / (numberOfCreationalObjects + 1)) * 
				((collectableValue + 1) / (harmfulValue + collectableValue + 1)) * maxCoverPercentage;
		data.levelPercentage = Utils.noise(data.levelPercentage, randomnessPercentage, random.nextDouble());

		//calculate different percentage based on the previous data
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
	

	/**
	 * Add a solid to the level space without disconnecting the level
	 * @param level	the current level to test
	 * @param x		the x position
	 * @param y		the y position
	 * @param solid	the name of the solid
	 * @return		true if it placed it and false otherwise
	 */
	private boolean placeSolid(LevelData level, int x, int y, String solid){
		if(!level.checkConnectivity(x, y)){
			return false;
		}
		level.set(x, y, solid);
		return true;
	}

	/**
	 * build level layout using the solid objects
	 * @param level				the current level
	 * @param coverPercentage	the cover percentages
	 */
	private void buildLevelLayout(LevelData level, LevelCoverData coverPercentage){
		ArrayList<String> solidSprites = gameAnalyzer.getSolidSprites();
		if(solidSprites.size() > 0){
			//picking a random solid object to use
			String randomSolid = solidSprites.get(random.nextInt(solidSprites.size()));
			
			//adding a borders around the level
			for(int x=0; x<level.getWidth(); x++){
				level.set(x, 0, randomSolid);
				level.set(x, level.getHeight() - 1, randomSolid);
			}
			
			for(int y=0; y<level.getHeight(); y++){
				level.set(0, y, randomSolid);
				level.set(level.getWidth() - 1, y, randomSolid);
			}

			//number of solid to insert in the level

			double solidNumber = coverPercentage.levelPercentage * coverPercentage.solidPercentage * 
					getArea(level);
			
			while(solidNumber > 0){
				//list of all the free positions
				ArrayList<Point> freePositions = level.getAllFreeSpots();
				//pick random position
				Point randomPoint = freePositions.get(random.nextInt(freePositions.size()));
				solidNumber -= 1;
				//if can't place it choose another one and try again
				if(!placeSolid(level, randomPoint.x, randomPoint.y, randomSolid)){
					continue;
				}
				//start construct a corridor of random length
				int length = 2 + random.nextInt(3);
				ArrayList<Point> directions = new ArrayList<Point>(Arrays.asList(new Point[]{new Point(1,0), new Point(-1,0), new Point(0,-1), new Point(0,1)}));
				while(length > 0){
					if(random.nextDouble() < shuffleDirectionPercentage){
						Collections.shuffle(directions);
					}
					int i=0;

					//check each direction and move using them
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

					//if no direction can done just stop this corridor
					if(i >= directions.size()){
						break;
					}
				}

			}
		}
	}

	/**
	 * Get the area of the level
	 * @param level	the current level
	 * @return		the size of the internal level (without the borders if exists)
	 */
	private double getArea(LevelData level){
		if(gameAnalyzer.getSolidSprites().size() > 0){
			return (level.getWidth() - 2) * (level.getHeight() - 2);
		}
		
		return level.getWidth() * level.getHeight();
	}

	/**
	 * get all free positions that have the highest and lowest y value 
	 * @param freePositions	list of all free positions
	 * @return				return list of all these points
	 */
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

	/**
	 * add the avatar to the current level
	 * @param level	the current level
	 * @param game	the game description object
	 * @return		the added position
	 */
	private Point addAvatar(LevelData level, GameDescription game){
		//get all the free position in the level
		ArrayList<Point> freePositions = level.getAllFreeSpots();
		//pick random position from them
		Point randomPoint = freePositions.get(random.nextInt(freePositions.size()));
		//pick random avatar sprite
		ArrayList<String> avatar = gameAnalyzer.getAvatarSprites();
		String randomAvatar = avatar.get(random.nextInt(avatar.size()));
		
		//get the type of the avatar
		String type = "";
		for(SpriteData data:game.getAvatar()){
			if(randomAvatar.equals(data.name)){
				type = data.type;
				break;
			}
		}
		
		//if the avatar is horizontal mover or shooter it placed at the top or the bottom
		//free positions
		if(gameAnalyzer.horzAvatar.contains(type)){
			freePositions = getUpperLowerPoints(freePositions);
			randomPoint = freePositions.get(random.nextInt(freePositions.size()));
		}
		

		level.set(randomPoint.x, randomPoint.y, randomAvatar);
		
		return randomPoint;
	}
	

	/**
	 * calculate the number of objects in the level
	 * @param game	game description object
	 * @param level	the current level
	 * @return		hashmap for all sprite names with the associated numbers
	 */
	private HashMap<String, Integer> calculateNumberOfObjects(GameDescription game, LevelData level){
		HashMap<String, Integer> objects = new HashMap<String, Integer>();
		ArrayList<SpriteData> allSprites = game.getAllSpriteData();

		//add all sprite names as keys in the hashmap
		for(SpriteData sprite:allSprites){
			objects.put(sprite.name, 0);
		}

		//calculate the numbers
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
	

	/**
	 * fix the termination conditions by making sure all of them are unstaisfied
	 * @param game				game description object
	 * @param level				current level
	 * @param coverPercentage	the cover percentages
	 */
	private void fixGoals(GameDescription game, LevelData level, LevelCoverData coverPercentage){
		//get all the termination conditions
		ArrayList<TerminationData> termination = game.getTerminationConditions();
		//get the number of objects in the level
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

			//calculate the number of sprites found on the board
			for(String stype:ter.sprites){
				if(!gameAnalyzer.getAvatarSprites().contains(stype)){
					currentSprites.add(stype);
					totalSprites.add(stype);
					currentNum += numObjects.get(stype);
				}
			}

			//difference between the expected number and the current number of objects
			increase = totalNum + 1 - currentNum;
			
			//if the condition is satisfied add more sprites to make till be unstaisfied
			if(currentSprites.size() > 0){
				for(int i = 0; i < increase; i++){
					int index = random.nextInt(positions.size());
					Point pos = positions.remove(index);
					level.set(pos.x, pos.y, currentSprites.get(random.nextInt(currentSprites.size())));
				}
			}
		}
	}

	/**
	 * check if the object is a moving object
	 * @param game	game description object
	 * @param stype	current sprite need to be checked
	 * @return		true if stype is moving and false otherwise
	 */
	private boolean isMoving(GameDescription game, String stype){
		ArrayList<SpriteData> movingSprites = game.getMoving();
		for(SpriteData sprite:movingSprites){
			if(sprite.name.equals(stype)){
				return true;
			}
		}
		
		return false;
	}
	

	/**
	 * get a free position far from the avatar position
	 * @param freePosition		list of the free positions
	 * @param avatarPosition	the avatar position
	 * @return					the index of the possible far position
	 */
	private int getFarLocation(ArrayList<Point> freePosition, Point avatarPosition){
		ArrayList<Double> distProb = new ArrayList<Double>();
		double totalValue = 0;
		distProb.add(avatarPosition.getDistance(freePosition.get(0)));
		totalValue += distProb.get(0);

		//give each position a probability to be picked based on 
		//how near or far it is from the avatar position
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
	

	/**
	 * Add harmful objects to the level
	 * @param game				the game description object
	 * @param level				the current level
	 * @param coverPercentage	the cover percentages
	 * @param avatarPosition	the current avatar position
	 */
	private void addHarmfulObjects(GameDescription game, LevelData level, LevelCoverData coverPercentage, Point avatarPosition){
		double numberOfHarmful = coverPercentage.levelPercentage * coverPercentage.harmfulPercentage * 
				getArea(generatedLevel);
		
		ArrayList<String> harmfulSprites = gameAnalyzer.getHarmfulSprites();
		ArrayList<Point> freePositions = level.getAllFreeSpots();
		while(numberOfHarmful > 0){
			//get a random harmful sprite that is not spawned by another one
			String randomHarm = harmfulSprites.get(random.nextInt(harmfulSprites.size()));
			if(gameAnalyzer.checkIfSpawned(randomHarm) == 0){
				continue;
			}

			//if the harmful object is moving then get a far position from the player starting point
			if(isMoving(game, randomHarm)){
				int index = getFarLocation(freePositions, avatarPosition);
				if(index != -1){
					Point randPoint = freePositions.get(index);
					level.set(randPoint.x, randPoint.y, randomHarm);
					freePositions.remove(index);
					numberOfHarmful -= 1;
				}
			}

			//pick any random position will be fine
			else{
				int index = random.nextInt(freePositions.size());
				Point randPoint = freePositions.get(index);
				level.set(randPoint.x, randPoint.y, randomHarm);
				freePositions.remove(index);
				numberOfHarmful -= 1;
			}
		}
	}
	

	/**
	 * Add Collectable objects to the current level
	 * @param game				the game description object
	 * @param level				the current level
	 * @param coverPercentage	the current cover percentages
	 */
	private void addCollectableObjects(GameDescription game, LevelData level, LevelCoverData coverPercentage){
		double numberOfOther = coverPercentage.levelPercentage * coverPercentage.collectablePercentage * 
				getArea(level);
		ArrayList<String> otherSprites = gameAnalyzer.getCollectableSprites();
		ArrayList<Point> freePositions = level.getAllFreeSpots();
		while(numberOfOther > 0){
			//pick a random non spawning sprite
			String randomSprite = otherSprites.get(random.nextInt(otherSprites.size()));
			if(gameAnalyzer.checkIfSpawned(randomSprite) == 0){
				continue;
			}
			

			//place it at any random free position
			int index = random.nextInt(freePositions.size());
			Point randPoint = freePositions.get(index);
			level.set(randPoint.x, randPoint.y, randomSprite);
			freePositions.remove(index);
			numberOfOther -= 1;
		}
	}
	

	/**
	 * Add other kind of objects to the level
	 * @param game				the current game description
	 * @param level				the current game level
	 * @param coverPercentage	the cover percentages
	 */
	private void addOtherObjects(GameDescription game, LevelData level, LevelCoverData coverPercentage){
		double numberOfOther = coverPercentage.levelPercentage * coverPercentage.otherPercentage * 
				getArea(level);
		ArrayList<String> otherSprites = gameAnalyzer.getOtherSprites();
		ArrayList<Point> freePositions = level.getAllFreeSpots();
		while(numberOfOther > 0){

			//pick a random sprite that is not spawned by other sprites
			String randomSprite = otherSprites.get(random.nextInt(otherSprites.size()));
			if(gameAnalyzer.checkIfSpawned(randomSprite) == 0){
				continue;
			}
			

			//place it at any random free position
			int index = random.nextInt(freePositions.size());
			Point randPoint = freePositions.get(index);
			level.set(randPoint.x, randPoint.y, randomSprite);
			freePositions.remove(index);
			numberOfOther -= 1;
		}
	}
	

	/**
	 * Generate a level with a fixed width and length
	 * @param game			the current level description
	 * @param elapsedTimer	the amount of time allowed for generation
	 * @param width			the width of the level
	 * @param length		the length of the level
	 * @return				string for the generated level
	 */
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
	

	/**
	 * generate a level without specifying the width and the height of the level
	 * @param game			the current game description object
	 * @param elpasedTimer	the amount of time allowed for generation
	 * @return				string for the generated level
	 */
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


	/**
	 * get the current used level mapping to create the level string
	 * @return	the level mapping used to create the level string
	 */
	@Override
	public HashMap<Character, ArrayList<String>> getLevelMapping() {
		return generatedLevel.getLevelMapping();
	}
}
