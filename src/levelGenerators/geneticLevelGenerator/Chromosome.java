package levelGenerators.geneticLevelGenerator;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import core.game.GameDescription.SpriteData;
import core.game.GameDescription.TerminationData;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import levelGenerators.StepController;
import levelGenerators.constraints.CombinedConstraints;
import levelGenerators.constraints.CoverPercentageConstraint;
import levelGenerators.constraints.DeathConstraint;
import levelGenerators.constraints.SolutionLengthConstraint;
import ontology.Types;
import ontology.Types.WINNER;
import tools.ElapsedCpuTimer;
import tools.LevelMapping;

public class Chromosome implements Comparable<Chromosome>, Runnable{
	private ArrayList<String>[][] level;
	private double fitness;
	private boolean calculated;
	private AbstractPlayer automatedAgent;
	
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
		
		c.constructAgent();
		return c;
	}
	
	private void constructAgent(){
		try{
			Class agentClass = Class.forName(SharedData.AGENT_NAME);
			Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
			automatedAgent = (AbstractPlayer)agentConst.newInstance(getStateObservation(), null);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void InitializeRandom(){
		for(int i = 0; i < SharedData.RANDOM_INIT_AMOUNT; i++){
			this.mutate();
		}
		
		constructAgent();
	}
	
	public void InitializeConstructive(){
		String[] levelString = SharedData.constructiveGen.generateLevel(SharedData.gameDescription, null, level[0].length, level.length).split("\n");
		HashMap<Character, ArrayList<String>> charMap = SharedData.constructiveGen.getLevelMapping();
		
		for(int y=0; y<levelString.length; y++){
			for(int x=0; x<levelString[y].length(); x++){
				if(levelString[y].charAt(x) != ' '){
					this.level[y][x].addAll(charMap.get(levelString[y].charAt(x)));
				}
			}
		}
		
		FixLevel();
		constructAgent();
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
		
		children.get(0).constructAgent();
		children.get(1).constructAgent();
		
		return children;
	}
	
	public void mutate(){
		ArrayList<SpriteData> allSprites = SharedData.gameDescription.getAllSpriteData();
		
		for(int i = 0; i < SharedData.MUTATION_AMOUNT; i++)
		{
			int pointX = SharedData.random.nextInt(level[0].length);
			int pointY = SharedData.random.nextInt(level.length);
			if(SharedData.random.nextDouble() < SharedData.INSERTION_PROB){
				String spriteName = allSprites.get(SharedData.random.nextInt(allSprites.size())).name;
				if(!level[pointY][pointX].contains(spriteName)){
					level[pointY][pointX].add(spriteName);
				}
			}
			else if(SharedData.random.nextDouble() < SharedData.INSERTION_PROB + SharedData.DELETION_PROB){
				level[pointY][pointX].clear();
			}
			else{
				int point2X = SharedData.random.nextInt(level[0].length);
				int point2Y = SharedData.random.nextInt(level.length);
				
				ArrayList<String> temp = level[pointY][pointX];
				level[pointY][pointX] = level[point2Y][point2X];
				level[point2Y][point2X] = temp;
			}
		}
		
		FixLevel();
	}
	
	private ArrayList<SpritePointData> getPositions(ArrayList<String> sprites){
		ArrayList<SpritePointData> positions = new ArrayList<SpritePointData>();
		
		for(int y = 0; y < level.length; y++){
			for(int x = 0; x < level[y].length; x++){
				ArrayList<String> tileSprites = level[y][x];
				for(String stype:tileSprites){
					for(String s:sprites){
						if(s.equals(stype)){
							positions.add(new SpritePointData(stype, x, y));
						}
					}
				}
			}
		}
		
		return positions;
	}
	
	private ArrayList<SpritePointData> getFreePositions(ArrayList<String> sprites){
		ArrayList<SpritePointData> positions = new ArrayList<SpritePointData>();
		
		for(int y = 0; y < level.length; y++){
			for(int x = 0; x < level[y].length; x++){
				ArrayList<String> tileSprites = level[y][x];
				boolean isNotFound = true;
				for(String stype:tileSprites){
					for(String s:sprites){
						if(s.equals(stype)){
							isNotFound = false;
						}
					}
				}
				
				if(isNotFound){
					positions.add(new SpritePointData(sprites.get(SharedData.random.nextInt(sprites.size())), x, y));
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
		
		if(avatarPositions.size() == 0){
			int pointX = SharedData.random.nextInt(level[0].length);
			int pointY = SharedData.random.nextInt(level.length);
			
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
		for(TerminationData ter:termination){
			for(String stype:ter.sprites){
				acheived += numObjects.get(stype);
			}
			
			if(acheived <= ter.limit){
				ArrayList<SpritePointData> positions = getFreePositions(ter.sprites);
				int increase = (ter.limit - acheived) + 1;
				if(ter.limit > 0){
					increase += SharedData.random.nextInt(ter.limit);
				}
				for(int i = 0; i < increase; i++){
					int index = SharedData.random.nextInt(positions.size());
					SpritePointData position = positions.remove(index);
					level[position.y][position.x].add(ter.sprites.get(SharedData.random.nextInt(ter.sprites.size())));
				}
			}
		}
	}
	
	private void FixObjects(){
		HashMap<String, Integer> numObjects = calculateNumberOfObjects();
		
		for(Entry<String, Integer> entry:numObjects.entrySet()){
			int value = entry.getValue();
			if(value > 0){
				value = 1;
			}
			if(value != SharedData.gameAnalyzer.getMinRequiredNumber(entry.getKey())){
				ArrayList<SpritePointData> positions = getFreePositions(new ArrayList<String>(Arrays.asList(new String[]{entry.getKey()})));
				int index = SharedData.random.nextInt(positions.size());
				SpritePointData position = positions.remove(index);
				level[position.y][position.x].add(entry.getKey());
			}
		}
	}
	
	private void FixLevel(){
		FixObjects();
		FixGoal();
		FixPlayer();
	}
	
	public LevelMapping getLevelMapping(){
		LevelMapping levelMapping = new LevelMapping(SharedData.gameDescription);
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
	
	private double getCoverPercentage(){
		int objects = 0;
		for (int y = 0; y < level.length; y++) {
			for (int x = 0; x < level[y].length; x++) {
				objects += Math.min(1, level[y][x].size());
			}
		}
		
		return 1.0 * objects / (level.length * level[0].length);
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
	
	private double getGameScore(double scoreDiff, double maxScore){
		if(maxScore == 0){
			return 1;
		}
		if(scoreDiff <= 0){
			return 0;
		}
		double result = (3 * scoreDiff / maxScore);
		return 2 / (1 + Math.exp(-result)) - 1;
	}
	
	private double getDeaths(WINNER win, double drawScore){
		if(win == WINNER.NO_WINNER){
			return drawScore;
		}
		if(win == WINNER.PLAYER_LOSES){
			return 1;
		}
		
		return 0;
	}
	
	private double getDeathScore(double bestScore, double doNothingScore){
		if(bestScore == 0){
			return 1;
		}
		
		return 0;
	}
	
	private double getRuleScore(double ruleDiff, double minRule){
		if(ruleDiff < 0){
			return 0;
		}
		return 2 / (1 + Math.exp(-3 * ruleDiff / minRule)) - 1;
	}
	
	private double getSolutionLengthScore(ArrayList<Types.ACTIONS> solution, double minSol){
		return 2 / (1 + Math.exp(-3 * solution.size() / minSol)) - 1;
	}
	
	private int getDoNothingState(StateObservation stateObs, int steps){
		int i =0;
		for(i=0;i<steps;i++){
			if(stateObs.isGameOver()){
				break;
			}
			stateObs.advance(Types.ACTIONS.ACTION_NIL);
		}
		
		return i;
	}
	
	public double calculateFitness(long time, boolean recalculate){
		if(!calculated || recalculate){
			calculated = true;
			StateObservation stateObs = getStateObservation();
			
			StepController stepAgent = new StepController(automatedAgent);
			ElapsedCpuTimer elapsedTimer = new ElapsedCpuTimer();
			elapsedTimer.setMaxTimeMillis(time);
			stepAgent.playGame(stateObs.copy(), elapsedTimer);
			
			StateObservation bestState = stepAgent.getFinalState();
			ArrayList<Types.ACTIONS> bestSol = stepAgent.getSolution();
			StateObservation doNothingState = stateObs.copy(); 
			int doNothingLength = getDoNothingState(stateObs.copy(), bestSol.size());
			double coverPercentage = getCoverPercentage();
			
			double maxScore = 0;
			if(SharedData.gameAnalyzer.getMinScoreUnit() > 0){
				double numberOfUnits = SharedData.gameAnalyzer.getMaxScoreUnit() / (SharedData.MAX_SCORE_PERCENTAGE * SharedData.gameAnalyzer.getMinScoreUnit());
				maxScore = numberOfUnits * SharedData.gameAnalyzer.getMinScoreUnit();
			}
			
			HashMap<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("solutionLength", bestSol.size());
			parameters.put("maxSolutionLength", SharedData.MIN_SOLUTION_LENGTH);
			parameters.put("doNothingSteps", doNothingLength);
			parameters.put("bestPlayer", bestState.getGameWinner());
			parameters.put("doNothingPlayer", doNothingState.getGameWinner());
			parameters.put("minDoNothingSteps", SharedData.MIN_DOTHING_STEPS);
			parameters.put("coverPercentage", coverPercentage);
			parameters.put("minCoverPercentage", SharedData.MIN_COVER_PERCENTAGE);
			parameters.put("maxCoverPercentage", SharedData.MAX_COVER_PERCENTAGE);
			parameters.put("numOfObjects", calculateNumberOfObjects());
			parameters.put("gameAnalyzer", SharedData.gameAnalyzer);
			parameters.put("gameDescription", SharedData.gameDescription);
			
			CombinedConstraints constraint = new CombinedConstraints();
			constraint.addConstraints(new String[]{"SolutionLengthConstraint", "DeathConstraint", 
					"CoverPercentageConstraint", "SpriteNumberConstraint", "GoalConstraint"});
			constraint.setParameters(parameters);
			System.out.println(constraint.checkConstraint());
			
			double scoreDiffScore = getGameScore(bestState.getGameScore() - doNothingState.getGameScore(), maxScore);
			double deathDiffScore = getDeathScore(getDeaths(bestState.getGameWinner(), SharedData.DRAW_FITNESS), getDeaths(doNothingState.getGameWinner(), SharedData.DRAW_FITNESS));
			double ruleScore = getRuleScore(bestState.getEventsHistory().size() - doNothingState.getEventsHistory().size(), SharedData.MIN_RULE_NUMBER);
			double solutionLengthScore = getSolutionLengthScore(bestSol, SharedData.MIN_SOLUTION_LENGTH);
			
			fitness = (ruleScore + deathDiffScore + scoreDiffScore + solutionLengthScore) / 4;
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

	@Override
	public void run() {
		calculateFitness(SharedData.EVALUATION_TIME, true);
	}
}
