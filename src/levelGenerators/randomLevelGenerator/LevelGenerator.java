package levelGenerators.randomLevelGenerator;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import core.game.GameDescription;
import core.game.GameDescription.SpriteData;
import core.generator.AbstractLevelGenerator;
import tools.ElapsedCpuTimer;

public class LevelGenerator extends AbstractLevelGenerator{

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
		emptyPercentage = 0.7;
	}
	
	private DataPoint isUnique(ArrayList<DataPoint> points, int x, int y){
		for(DataPoint temp:points){
			if(temp.x == x && temp.y == y){
				return temp;
			}
		}
		
		return null;
	}
	
	private void addUnique(ArrayList<DataPoint> points, int width, int length, char c){
		int x =0;
		int y = 0;
		do{
			x = random.nextInt(width);
			y = random.nextInt(length);
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
		
		int width = (int)Math.max(minSize, sprites.size() * (1 + 0.25 * random.nextDouble()));
		int length = (int)Math.max(minSize, sprites.size() * (1 + 0.25 * random.nextDouble()));
		width = (int)Math.min(width, maxSize);
		length = (int)Math.min(length, maxSize);
		
		ArrayList<Character> avatar = new ArrayList<Character>();
		ArrayList<Character> choices = new ArrayList<Character>();
		for(Map.Entry<Character, ArrayList<String>> pair:game.getLevelMapping().entrySet()){
			boolean avatarExists = false;
			for (SpriteData avatarName:avatars){
				if(pair.getValue().contains(avatarName.name)){
					avatarExists = true;
				}
			}
			
			if(!avatarExists){
				if(!pair.getValue().contains("avatar")){
					choices.add(pair.getKey());
				}
			}
			else{
				avatar.add(pair.getKey());
			}
		}
		
		ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();
		for(Character c:choices){
			addUnique(dataPoints, width, length, c);
		}
		
		if(avatar.size() == 0){
			avatar.add('A');
		}
		addUnique(dataPoints, width, length, avatar.get(random.nextInt(avatar.size())));
		
		for(int y=0; y < length; y++){
			for(int x=0; x < width; x++){
				DataPoint p = isUnique(dataPoints, x, y);
				if(p != null){
					result += p.c;
				}
				else if(random.nextDouble() < emptyPercentage){
					result += " ";
				}
				else{
					result += choices.get(random.nextInt(choices.size()));
				}
			}
			result += "\n";
		}
		
		return result;
	}

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
