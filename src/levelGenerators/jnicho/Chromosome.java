package levelGenerators.jnicho;

import core.game.GameDescription.SpriteData;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.LevelMapping;
import tools.StepController;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;

public class Chromosome implements  Comparable<Chromosome> {

    ArrayList<String>[][] level;
    double fitness;

    boolean calculated;

    AbstractPlayer goodAgent;
    AbstractPlayer badAgent;
    AbstractPlayer doNothingAgent;

    StateObservation stateObservation;

    public Chromosome (int width, int height) {
        this.level = new ArrayList[height][width];
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++){
                this.level[y][x] = new ArrayList<String>();
            }
        }
        this.fitness = Double.MIN_VALUE;
        this.calculated = false;
        this.stateObservation = null;
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
            Class agentClass = Class.forName(Constants.goodAgent);
            Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
            goodAgent = (AbstractPlayer)agentConst.newInstance(getStateObservation().copy(), null);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        try{
            Class agentClass = Class.forName(Constants.badAgent);
            Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
            badAgent = (AbstractPlayer)agentConst.newInstance(getStateObservation().copy(), null);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        try{
            Class agentClass = Class.forName(Constants.doNothingAgent);
            Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
            doNothingAgent = (AbstractPlayer)agentConst.newInstance(getStateObservation().copy(), null);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void Randomise() {
        for (int i=0; i<Constants.randomInitAmount; i++)
            this.mutate();

        constructAgent();
    }

    public ArrayList<Chromosome> crossOver(Chromosome c) {
        ArrayList<Chromosome> children = new ArrayList<>();
        children.add(new Chromosome(level[0].length, level.length));
        children.add(new Chromosome(level[0].length, level.length));

        //crossover point
        int pointY = Constants.random.nextInt(level.length);
        int pointX = Constants.random.nextInt(level[0].length);

        //swap the two chromosomes around this point
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

    public void mutate() {

        ArrayList<SpriteData> allSprites = Constants.gameDescription.getAllSpriteData();

        for (int i=0; i<Constants.mutationAmount; i++) {
            int solidFrame = 0;
            if (Constants.gameAnalyzer.getSolidSprites().size() > 0)
                solidFrame = 2;

            int pointX = Constants.random.nextInt(level[0].length-solidFrame)+ solidFrame/2;
            int pointY = Constants.random.nextInt(level.length-solidFrame) + solidFrame/2;


            // Add a random new sprite
            if (Constants.random.nextDouble() < Constants.addSpriteProb) {
                String spriteName = allSprites.get(Constants.random.nextInt(allSprites.size())).name;
                ArrayList<SpritePointData> freePositions = getFreePositions(new ArrayList<String>(Arrays.asList(new String[]{spriteName})));
                int index = Constants.random.nextInt(freePositions.size());
                level[freePositions.get(index).y][freePositions.get(index).x].add(spriteName);
            }

            //Clear a random position
            else if (Constants.random.nextDouble() < Constants.clearProb) {
                level[pointY][pointX].clear();
            }
        }
        FixLevel();
    }

    private ArrayList<SpritePointData> getPositions(ArrayList<String> sprites){
        ArrayList<SpritePointData> positions = new ArrayList<>();

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
        ArrayList<SpritePointData> positions = new ArrayList<>();

        for(int y = 0; y < level.length; y++){
            for(int x = 0; x < level[y].length; x++){
                ArrayList<String> tileSprites = level[y][x];
                boolean found = false;
                for(String stype:tileSprites){
                    found = found || sprites.contains(stype);
                    found = found || Constants.gameAnalyzer.getSolidSprites().contains(stype);
                }

                if(!found){
                    positions.add(new SpritePointData("", x, y));
                }
            }
        }

        return positions;
    }

    private void FixPlayer() {
        ArrayList<SpriteData> avatar = Constants.gameDescription.getAvatar();
        ArrayList<String> avatarNames = new ArrayList<>();
        for (SpriteData a:avatar) {
            avatarNames.add(a.name);
        }

        ArrayList<SpritePointData> avatarPositions = getPositions(avatarNames);

        // if not avatar insert a new one
        if(avatarPositions.size() == 0){
            ArrayList<SpritePointData> freePositions = getFreePositions(avatarNames);

            int index = Constants.random.nextInt(freePositions.size());
            level[freePositions.get(index).y][freePositions.get(index).x].
                    add(avatarNames.get(Constants.random.nextInt(avatarNames.size())));
        }

        //if there is more than one avatar remove all of them except one
        else if(avatarPositions.size() > 1){
            int notDelete = Constants.random.nextInt(avatarPositions.size());
            int index = 0;
            for(SpritePointData point:avatarPositions){
                if(index != notDelete){
                    level[point.y][point.x].remove(point.name);
                }
                index += 1;
            }
        }

    }

    private void FixLevel() {
        FixPlayer();
    }

    public String getLevelString(LevelMapping levelMapping) {
        String levelString = "";
        for (int y=0; y<level.length; y++) {
            for (int x=0; x<level[y].length; x++) {
                levelString += levelMapping.getCharacter(level[y][x]);
            }
            levelString += "\n";
        }
        levelString = levelString.substring(0, levelString.length() - 1);
        return levelString;
    }

    public LevelMapping getLevelMapping(){
        LevelMapping levelMapping = new LevelMapping(Constants.gameDescription);
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

    private StateObservation getStateObservation(){
        if(stateObservation != null){
            return stateObservation;
        }

        LevelMapping levelMapping = getLevelMapping();
        String levelString = getLevelString(levelMapping);
        stateObservation = Constants.gameDescription.testLevel(levelString, levelMapping.getCharMapping());
        return stateObservation;
    }

    private int getbadPlayerResult(StateObservation stateObs, int steps, AbstractPlayer agent){
        int i =0;
        for(i=0;i<steps;i++){
            if(stateObs.isGameOver()){
                break;
            }
            Types.ACTIONS bestAction = agent.act(stateObs, null);
            stateObs.advance(bestAction);
        }

        return i;
    }

    public void calculateFitness(long time) {
        if (!calculated) {
            calculated = true;


            double value = 0;


            //Check one and only one avatar

            ArrayList<SpriteData> avatar = Constants.gameDescription.getAvatar();
            ArrayList<String> avatarNames = new ArrayList<>();
            for (SpriteData a:avatar) {
                avatarNames.add(a.name);
            }

            ArrayList<SpritePointData> avatarPositions = getPositions(avatarNames);

            // if not avatar insert a new one
            if(avatarPositions.size() != 1){
                value -=1;
            } else {
                value += 1;
            }


            //Check Goal Constraint



            //Check Num sprites


            //Check Solution length


            //Check do nothing


            //Check good player vs bad player

            StateObservation stateObs = getStateObservation();


            //Play the game using the "good" agent
            StepController stepAgent = new StepController(goodAgent, Constants.evaluationStepTime);
            ElapsedCpuTimer elapsedTimer = new ElapsedCpuTimer();
            elapsedTimer.setMaxTimeMillis(time);
            stepAgent.playGame(stateObs.copy(), elapsedTimer);
            StateObservation bestState = stepAgent.getFinalState();
            double bestScore = bestState.getGameScore();

            //Play the game using the "bad" agent
            stepAgent = new StepController(badAgent, Constants.evaluationStepTime);
            elapsedTimer.setMaxTimeMillis(time);
            stepAgent.playGame(stateObs.copy(), elapsedTimer);
            StateObservation worstState = stepAgent.getFinalState();
            double worstScore = bestState.getGameScore();


            value += (bestScore-worstScore);


            ArrayList<Types.ACTIONS> bestSol = stepAgent.getSolution();

            StateObservation doNothingState = null;
            int doNothingLength = Integer.MAX_VALUE;

            for (int i=0; i<Constants.repetitionAmount; i++) {
                StateObservation tempState = stateObs.copy();
                int temp = getbadPlayerResult(tempState, bestSol.size(), doNothingAgent);
                if (temp < doNothingLength) {
                    doNothingLength = temp;
                    doNothingState = tempState;
                }
            }





            this.fitness = value;
        }
    }

    @Override
    public int compareTo(Chromosome o) {

        if (this.fitness > o.fitness)
            return -1;
        else if (this.fitness < o.fitness)
            return 1;
        else
            return 0;
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

}
