package tracks.levelGeneration.randomLevelGenerator;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import core.game.GameDescription;
import core.game.GameDescription.SpriteData;
import core.generator.AbstractLevelGenerator;
import tools.ElapsedCpuTimer;
import tools.GameAnalyzer;

public class LevelGenerator extends AbstractLevelGenerator{

	/**
	 * Add Borders to the generated random level
	 */
	public static boolean includeBorders = true;
	
	/**
	 * Random number generator for the level generator
	 */
	private Random random;
	
	/**
	 * Minimum size of the level
	 */
	private int minSize;
	
	/**
	 * Maximum size of the level
	 */
	private int maxSize;
	
	/**
	 * Amount of empty spaces in the playground
	 */
	private double emptyPercentage;
	
	/**
	 * Constructor for the RandomLevelGenerator where it initialize the random object used.
	 * @param game			Abstract game description object. This object contains all needed information about the current game.
	 * @param elapsedTimer	Timer to define the maximum amount of time for the constructor.
	 */
	public LevelGenerator(GameDescription game, ElapsedCpuTimer elapsedTimer){
		random = new Random();
		minSize = 4;
		maxSize = 18;

		emptyPercentage = 0.9;
	}
	
	/**
	 * Get the first solid character that is described in the level mapping
	 * @param gameDescription	game description object to get all data
	 * @return					character of the first solid object found or null otherwise
	 */
	private Character getSolidCharacter(GameDescription gameDescription){
		GameAnalyzer gameAnalyzer = new GameAnalyzer(gameDescription);
		ArrayList<String> solidSprites = gameAnalyzer.getSolidSprites();
		int minValue = Integer.MAX_VALUE;
		Character result = ' ';
		for(Entry<Character, ArrayList<String>> entry:gameDescription.getLevelMapping().entrySet()){
			for(String s:solidSprites){
				if(entry.getValue().contains(s)){
					if(entry.getValue().size() < minValue){
						result = entry.getKey();
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Surround the level with solid border
	 * @param gameDescription	game description that describe all aspects of games
	 * @param points			array of the unique points to be added
	 * @param width				width of the level
	 * @param height			height of the level
	 * @return					true if it build the border and false otherwise
	 */
	private boolean buildLayout(GameDescription gameDescription, ArrayList<DataPoint> points, int width, int height){
		Character solidCharacter = getSolidCharacter(gameDescription);
		
		if(solidCharacter != null){
			//Add the upper and lower solid object
			for(int x=0; x<width; x++){
				points.add(new DataPoint(x, 0, solidCharacter));
				points.add(new DataPoint(x, height - 1, solidCharacter));
			}
			
			//Add the left and right solid object
			for(int y=0; y<height; y++){
				points.add(new DataPoint(0, y, solidCharacter));
				points.add(new DataPoint(width - 1, y, solidCharacter));
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Check if the input x and y are found in the ArrayList
	 * @param points	list of points required to check
	 * @param x			the x value to be checked
	 * @param y			the y value to be checked
	 * @return			the point if its in the list or null otherwise
	 */
	private DataPoint isUnique(ArrayList<DataPoint> points, int x, int y){
		for(DataPoint temp:points){
			if(temp.x == x && temp.y == y){
				return temp;
			}
		}
		
		return null;
	}
	

	/**
	 * Add random unique x and y value that is not found in the 
	 * list of points associated with a certain character c
	 * @param points	list of points to check uniqueness with
	 * @param width		the maximum x value
	 * @param length	the maximum y value
	 * @param c			the character associated with the new point
	 */
	private void addUnique(ArrayList<DataPoint> points, int width, int length, char c){
		int x =0;
		int y = 0;
		do{

			int border = 0;
			if(includeBorders){
				border = 1;
			}
			x = random.nextInt(width - 2 * border) + border;
			y = random.nextInt(length - 2 * border) + border;
		}while(isUnique(points, x, y) != null);
		
		points.add(new DataPoint(x, y, c));
	}
	
	/**
	 * Generate a level string randomly contains only one avatar, 80% free space, and 20% of random sprites
	 * @param game			Abstract game description object. This object contains all needed information about the current game.
	 * @param elapsedTimer	Timer to define the maximum amount of time for the level generation.
	 */
	@Override
	public String generateLevel(GameDescription game, ElapsedCpuTimer elapsedTimer) {
		String result = "";
		ArrayList<SpriteData> sprites = game.getAllSpriteData();
		ArrayList<SpriteData> avatars = game.getAvatar();
		
		//Get a random width and random height value based on the length of game sprites
		//and it should be in between maxSize and minSize
		int width = (int)Math.max(minSize, sprites.size() * (1 + 0.25 * random.nextDouble()));
		int length = (int)Math.max(minSize, sprites.size() * (1 + 0.25 * random.nextDouble()));
		width = (int)Math.min(width, maxSize);
		length = (int)Math.min(length, maxSize);
		
		ArrayList<Character> avatar = new ArrayList<Character>();
		ArrayList<Character> choices = new ArrayList<Character>();
		for(Map.Entry<Character, ArrayList<String>> pair:game.getLevelMapping().entrySet()){
			boolean avatarExists = false;
			//check if the avatar is found in this level  mapping
			for (SpriteData avatarName:avatars){
				if(pair.getValue().contains(avatarName.name)){
					avatarExists = true;
				}
			}
			
			
			if(!avatarExists){
				//if not avatar add to other symbols
				if(!pair.getValue().contains("avatar")){
					choices.add(pair.getKey());
				}
			}
			else{
				//add the avatar symbol if it exist
				avatar.add(pair.getKey());
			}
		}
		
		ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();

		//add level borders based on static variable includeBorders
		if(includeBorders){
			includeBorders = buildLayout(game, dataPoints, width, length);
		}
		
		//Add only one of all objects in the choices array
		for(Character c:choices){
			addUnique(dataPoints, width, length, c);
		}
		
		//if no avatar is defined in the level mapping section use 'A' to add it
		if(avatar.size() == 0){
			avatar.add('A');
		}
		addUnique(dataPoints, width, length, avatar.get(random.nextInt(avatar.size())));
		
		//construct the result string from the array of datapoints
		for(int y=0; y < length; y++){
			for(int x=0; x < width; x++){
				//check if the position (x, y) is defined in the list of points
				DataPoint p = isUnique(dataPoints, x, y);
				//if yes then add the result
				if(p != null){
					result += p.c;

				}
				//add empty space
				else if(random.nextDouble() < emptyPercentage){
					result += " ";
				}
				//add random object
				else{
					result += choices.get(random.nextInt(choices.size()));
				}
			}
			result += "\n";
		}
		
		return result;
	}


	/**
	 * Helper class to store some data points with a 
	 * character associated with it
	 * @author AhmedKhalifa
	 */
	private class DataPoint{
		public int x;
		public int y;
		public char c;
		
		public DataPoint(int x, int y, char c){
			this.x = x;
			this.y = y;
			this.c = c;
		}
	}
	
}
