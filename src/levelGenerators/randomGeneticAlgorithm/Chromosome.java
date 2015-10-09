package levelGenerators.randomGeneticAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import core.game.GameDescription.SpriteData;
import core.game.GameDescription.TerminationData;
import core.game.StateObservation;
import levelGenerators.randomGeneticAlgorithm.controller.Agent;
import ontology.Types;
import ontology.Types.WINNER;
import tools.ElapsedCpuTimer;

public class Chromosome implements Comparable<Chromosome>{
	public final int RANDOM_INIT_AMOUNT = 50;
	public final double INSERTION_PROB = 0.25;
	public final double DELETION_PROB = 0.25;
	public final int MUTATION_AMOUNT = 1;
	
	private ArrayList<String>[][] level;
	private double fitness;
	private boolean calculated;
	
	public Chromosome(int width, int height){
		this.level = new ArrayList[height][width];
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				this.level[y][x] = new ArrayList<String>();
			}
		}
		this.fitness = 0;
		this.calculated = false;
	}
	
	public Chromosome clone(){
		Chromosome c = new Chromosome(level[0].length, level.length);
		
		for(int y = 0; y < level.length; y++){
			for(int x = 0; x < level[y].length; x++){
				c.level[y][x].addAll(level[y][x]);
			}
		}
		
		return c;
	}
	
	public void InitializeRandom(){
		ArrayList<String> solidSprites = SharedData.gameAnalyzer.getSolidSprites();
		
		if(solidSprites.size() > 0){
			int solidIndex = SharedData.random.nextInt(solidSprites.size());
			//Initialize Borders
			for(int y = 0; y < level.length; y++){
				level[y][0].add(solidSprites.get(solidIndex));
				level[y][level[y].length - 1].add(solidSprites.get(solidIndex));
			}
			
			for(int x = 1; x < level[0].length - 1; x++){
				level[0][x].add(solidSprites.get(solidIndex));
				level[level.length - 1][x].add(solidSprites.get(solidIndex));
			}
		}
		
		for(int i = 0; i < RANDOM_INIT_AMOUNT; i++){
			this.mutate();
		}
	}
	
	public ArrayList<Chromosome> crossOver(Chromosome c){
		ArrayList<Chromosome> children = new ArrayList<Chromosome>();
		children.add(new Chromosome(level[0].length, level.length));
		children.add(new Chromosome(level[0].length, level.length));
		
		int pointY = SharedData.random.nextInt(level.length);
		int pointX = SharedData.random.nextInt(level[0].length);
		
		for(int y = 0; y < level.length; y++){
			for(int x = 0; x < level[y].length; x++){
				if(y < pointY){
					children.get(0).level[y][x].addAll(this.level[y][x]);
					children.get(1).level[y][x].addAll(c.level[y][x]);
				}
				else if(y == pointY){
					if(x <= pointX){
						children.get(0).level[y][x].addAll(this.level[y][x]);
						children.get(1).level[y][x].addAll(c.level[y][x]);
					}
					else{
						children.get(0).level[y][x].addAll(c.level[y][x]);
						children.get(1).level[y][x].addAll(this.level[y][x]);
					}
				}
				else{
					children.get(0).level[y][x].addAll(c.level[y][x]);
					children.get(1).level[y][x].addAll(this.level[y][x]);
				}
			}
		}
		
		children.get(0).FixLevel();
		children.get(1).FixLevel();
		
		return children;
	}
	
	public void mutate(){
		ArrayList<SpriteData> allSprites = SharedData.gameDescription.getAllSpriteData();
		
		int startingIndex = 0;
		if(SharedData.gameAnalyzer.getSolidSprites().size() > 0){
			startingIndex = 1;
		}
		
		for(int i = 0; i < MUTATION_AMOUNT; i++)
		{
			int pointX = SharedData.random.nextInt(level[0].length - 2 * startingIndex) + startingIndex;
			int pointY = SharedData.random.nextInt(level.length - 2 * startingIndex) + startingIndex;
			if(SharedData.random.nextDouble() < INSERTION_PROB){
				String spriteName = allSprites.get(SharedData.random.nextInt(allSprites.size())).name;
				if(!level[pointY][pointX].contains(spriteName)){
					level[pointY][pointX].add(spriteName);
				}
			}
			else if(SharedData.random.nextDouble() < INSERTION_PROB + DELETION_PROB){
				level[pointY][pointX].clear();
			}
			else{
				int point2X = SharedData.random.nextInt(level[0].length - 2 * startingIndex) + startingIndex;
				int point2Y = SharedData.random.nextInt(level.length - 2 * startingIndex) + startingIndex;
				
				ArrayList<String> temp = level[pointY][pointX];
				level[pointY][pointX] = level[point2Y][point2X];
				level[point2Y][point2X] = temp;
			}
		}
		
		FixLevel();
	}
	
	private ArrayList<SpritePointData> getPositions(ArrayList<String> sprites){
		return getPositions(sprites, true);
	}
	
	private ArrayList<SpritePointData> getPositions(ArrayList<String> sprites, boolean found){
		ArrayList<SpritePointData> positions = new ArrayList<SpritePointData>();
		
		for(int y = 0; y < level.length; y++){
			for(int x = 0; x < level[y].length; x++){
				ArrayList<String> tileSprites = level[y][x];
				for(String stype:tileSprites){
					for(String s:sprites){
						if(s.equals(stype) == found){
							positions.add(new SpritePointData(stype, x, y));
						}
					}
				}
			}
		}
		
		return positions;
	}
	
	private void FixPlayer(){
		ArrayList<SpriteData> avatar = SharedData.gameDescription.getAvatar();
		ArrayList<String> avatarNames = new ArrayList<String>();
		for(SpriteData a:avatar){
			avatarNames.add(a.name);
		}
		ArrayList<SpritePointData> avatarPositions = getPositions(avatarNames);
		
		int startingIndex = 0;
		if(SharedData.gameAnalyzer.getSolidSprites().size() > 0){
			startingIndex = 1;
		}
		
		if(avatarPositions.size() == 0){
			int pointX = SharedData.random.nextInt(level[0].length - 2 * startingIndex) + startingIndex;
			int pointY = SharedData.random.nextInt(level.length - 2 * startingIndex) + startingIndex;
			
			level[pointY][pointX].add(avatarNames.get(SharedData.random.nextInt(avatarNames.size())));
		}
		else if(avatarPositions.size() > 1){
			int notDelete = SharedData.random.nextInt(avatarPositions.size());
			int index = 0;
			for(SpritePointData point:avatarPositions){
				if(index != notDelete){
					level[point.y][point.x].remove(point.name);
				}
				index += 1;
			}
		}
	}
	
	private void FixGoal(){
		HashMap<String, Integer> numObjects = calculateNumberOfObjects();
		ArrayList<TerminationData> termination = SharedData.gameDescription.getTerminationConditions();
		
		int acheived = 0;
		int totalNum = 0;
		for(TerminationData ter:termination){
			if(ter.type == SharedData.gameAnalyzer.spriteCounter){
				for(String stype:ter.sprites){
					acheived += numObjects.get(stype);
					totalNum += ter.limit;
				}
				
				if(acheived <= totalNum){
					ArrayList<SpritePointData> positions = getPositions(ter.sprites, false);
					int increase = 1 + SharedData.random.nextInt(totalNum / 2);
					for(int i = 0; i < increase; i++){
						int index = SharedData.random.nextInt(positions.size());
						SpritePointData position = positions.remove(index);
						level[position.x][position.y].add(ter.sprites.get(SharedData.random.nextInt(ter.sprites.size())));
					}
				}
			}
			else if(ter.type == SharedData.gameAnalyzer.multiCounter){
				boolean create = false;
				for(String stype:ter.sprites){
					create |= SharedData.gameAnalyzer.getMinRequiredNumber(stype) == 0;
					acheived += numObjects.get(stype);
					totalNum += ter.limit;
				}
				
				if(create){
					if(acheived >= totalNum){
						ArrayList<SpritePointData> positions = getPositions(ter.sprites, false);
						int decrease = 1 + SharedData.random.nextInt((positions.size() - 1)/2);
						for(int i = 0; i < decrease; i++){
							int index = SharedData.random.nextInt(positions.size());
							SpritePointData position = positions.remove(index);
							level[position.x][position.y].remove(position.name);
						}
					}
				}
				else{
					if(acheived <= totalNum){
						ArrayList<SpritePointData> positions = getPositions(ter.sprites, false);
						int increase = 1 + SharedData.random.nextInt(totalNum / 2);
						for(int i = 0; i < increase; i++){
							int index = SharedData.random.nextInt(positions.size());
							SpritePointData position = positions.remove(index);
							level[position.x][position.y].add(ter.sprites.get(SharedData.random.nextInt(ter.sprites.size())));
						}
					}
				}
			}
		}
	}
	
	private void FixLevel(){
		FixGoal();
		FixPlayer();
	}
	
	public LevelMapping getLevelMapping(){
		LevelMapping levelMapping = new LevelMapping();
		levelMapping.clearLevelMapping();
		char c = 'a';
		for(int y = 0; y < level.length; y++){
			for(int x = 0; x < level[y].length; x++){
				if(levelMapping.getCharacter(level[y][x]) == null){
					levelMapping.addCharacterMapping(c, level[y][x]);
					c += 1;
				}
			}
		}
		
		return levelMapping;
	}
	
	public String getLevelString(LevelMapping levelMapping){
		String levelString = "";
		for(int y = 0; y < level.length; y++){
			for(int x = 0; x < level[y].length; x++){
				levelString += levelMapping.getCharacter(level[y][x]);
			}
			levelString += "\n";
		}
		
		levelString = levelString.substring(0, levelString.length() - 1);
		
		return levelString;
	}
	
	private StateObservation getStateObservation(){
		LevelMapping levelMapping = getLevelMapping();
		String levelString = getLevelString(levelMapping);
		return SharedData.gameDescription.testLevel(levelString, levelMapping.getCharMapping());
	}
	
	private HashMap<String, Integer> calculateNumberOfObjects(){
		HashMap<String, Integer> objects = new HashMap<String, Integer>();
		ArrayList<SpriteData> allSprites = SharedData.gameDescription.getAllSpriteData();
		
		for(SpriteData sprite:allSprites){
			objects.put(sprite.name, 0);
		}
		
		for(int y = 0; y < level.length; y++){
			for(int x = 0; x < level[y].length; x++){
				ArrayList<String> sprites = level[y][x];
				for(String stype:sprites){
					if(objects.containsKey(stype)){
						objects.put(stype, objects.get(stype) + 1);
					}
					else{
						objects.put(stype, 1);
					}
				}
			}
		}
		
		return objects;
	}
	
	private double getObjectScore(HashMap<String, Integer> objects){
		ArrayList<SpriteData> allSprites = SharedData.gameDescription.getAllSpriteData();
		
		int numOfObjects = 0;
		for(Entry<String, Integer> entry:objects.entrySet()){
			int value = entry.getValue();
			if(value > 0){
				value = 1;
			}
			if(value == SharedData.gameAnalyzer.getMinRequiredNumber(entry.getKey())){
				numOfObjects += 1;
			}
		}
		
		return numOfObjects * 1.0 / allSprites.size();
	}
	
	private double getGoalScore(HashMap<String, Integer> objects){
		ArrayList<TerminationData> termination = SharedData.gameDescription.getTerminationConditions();
		
		int acheived = 0;
		int totalNum = 0;
		for(TerminationData ter:termination){
			if(ter.type == SharedData.gameAnalyzer.spriteCounter){
				for(String stype:ter.sprites){
					if(objects.get(stype) > ter.limit){
						acheived += 1;
					}
					totalNum += 1;
				}
			}
			else if(ter.type == SharedData.gameAnalyzer.multiCounter){
				for(String stype:ter.sprites){
					if(SharedData.gameAnalyzer.getMinRequiredNumber(stype) == 0){
						if(objects.get(stype) < ter.limit){
							acheived += 1;
						}
					}
					else{
						if(objects.get(stype) > ter.limit){
							acheived += 1;
						}
					}
					totalNum += 1;
				}
			}
		}
		
		if(totalNum == 0){
			return 1;
		}
		
		return acheived / totalNum;
	}
	
	private StateObservation getDoNothingState(StateObservation stateObs, long time){
		ElapsedCpuTimer elapsedTimer = new ElapsedCpuTimer();
		elapsedTimer.setMaxTimeMillis(time);
		
		while(elapsedTimer.remainingTimeMillis() > 0 && !stateObs.isGameOver()){
			stateObs.advance(Types.ACTIONS.ACTION_NIL);
		}
		
		return stateObs;
	}
	
	private double getGameScore(double score, double maxScore, double mean, double sigma){
		if(maxScore == 0){
			return 1;
		}
		if(score <= 0){
			return 0;
		}
		double result = (3 * score / maxScore);
		return 2 / (1 + Math.exp(-result)) - 1;
	}
	
	private double getGameWins(WINNER win, double drawScore){
		if(win == WINNER.NO_WINNER){
			return drawScore;
		}
		if(win == WINNER.PLAYER_LOSES){
			return 0;
		}
		
		return 1;
	}
	
	private double getExploredScore(){
		return SharedData.agent.getNumberOfStates() / Agent.Node.HUGE_VALUE;
	}
	
	private double getRuleScore(ArrayList<Types.ACTIONS> solution, double mean, double sigma){
		if(solution.size() == 0){
			return 0;
		}
		double result = (SharedData.agent.getNumberOfEvents() * 1.0 / solution.size());
		return Math.exp(-Math.pow(result - mean, 2) / (2 * Math.pow(sigma, 2)));
	}
	
	private double getSolutionLengthScore(ArrayList<Types.ACTIONS> solution, double area, double mean, double sigma){
		double result = (solution.size() * 1.0 / area);
		return 2 / (1 + Math.exp(-3 * result)) - 1;
	}
	
	private double getBoxMetric(ArrayList<Types.ACTIONS> solution){
		double value = 0;
		for(int i=1;i<solution.size();i++){
			if(solution.get(i) != solution.get(i-1)){
				value += 1;
			}
		}
		
		if(solution.size() == 0){
			return 0;
		}
		
		return value / solution.size();
	}
	
	public double calculateFitness(long time, boolean recalculate){
		if(!calculated || recalculate){
			calculated = true;
			StateObservation stateObs = getStateObservation();
			
			ElapsedCpuTimer elapsedTimer = new ElapsedCpuTimer();
			elapsedTimer.setMaxTimeMillis(time / 2);
			SharedData.agent.act(stateObs.copy(), elapsedTimer);
			
			HashMap<String, Integer> numObjects = calculateNumberOfObjects();
			ArrayList<Types.ACTIONS> solution = SharedData.agent.getBestSolution();
			StateObservation doNothingState = getDoNothingState(stateObs.copy(), time / 2);
			double maxScore = 0;
			if(SharedData.gameAnalyzer.getMinScoreUnit() > 0){
				double numberOfUnits = SharedData.gameAnalyzer.getMaxScoreUnit() / (SharedData.maxScorePercentage * SharedData.gameAnalyzer.getMinScoreUnit());
				maxScore = numberOfUnits * SharedData.gameAnalyzer.getMinScoreUnit();
			}
			
			double bestScore = getGameScore(SharedData.agent.getScore(), maxScore, 1, 0.3);
			double doNothingScore = getGameScore(doNothingState.getGameScore(), maxScore, 1, 0.3);
			double bestWins = getGameWins(SharedData.agent.getWin(), SharedData.drawFitness);
			double doNothingWins = getGameWins(doNothingState.getGameWinner(), SharedData.drawFitness);
			double exploredScore = getExploredScore();
			double ruleScore = getRuleScore(solution, 0.6, 0.2);
			double boxMetricScore = getBoxMetric(solution);
			double solutionLengthScore = getSolutionLengthScore(solution, level.length * level[0].length, 1, 0.2);
			double objectScore = getObjectScore(numObjects);
			
			System.out.println("\t\tGameScore: " + SharedData.agent.getScore() + " BestScore: " + bestScore + " DoNothingGameScore: "+ doNothingState.getGameScore() + " DoNothingScore: " + doNothingScore + " BestWins: " + bestWins + " DoNothingWins: " + doNothingWins);
			System.out.println("\t\tSolutionLength: " + solution.size() + " SolutionScore: " + solutionLengthScore + " ObjectScore: " + objectScore);
			//System.out.println("\t\tExploredScore: " + exploredScore + " RuleScore: " + ruleScore + " BoxMetric: " + boxMetricScore + " SolLength: " + solution.size() + " SolLengthScore: " + solutionLengthScore + " ObjectScore: " + objectScore);
			fitness = (0.75 * (bestScore - doNothingScore) + 0.25 * (bestWins - doNothingWins) + solutionLengthScore + objectScore) / 3;
		}
		
		return fitness;
	}
	
	public double getFitness(){
		return fitness;
	}
	
	public class SpritePointData{
		public String name;
		public int x;
		public int y;
		
		public SpritePointData(String name, int x, int y){
			this.name = name;
			this.x = x;
			this.y = y;
		}
	}

	@Override
	public int compareTo(Chromosome o) {
		if(this.fitness < o.fitness){
			return 1;
		}
		if(this.fitness > o.fitness){
			return -1;
		}
		return 0;
	}
}
