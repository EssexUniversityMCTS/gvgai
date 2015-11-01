/**
 * Code written by Adrien Couetoux, acouetoux@ulg.ac.be It is free to use,
 * distribute, and modify. User: adrienctx Date: 12/01/2015
 */
package controllers.adrienctx;

//import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import core.game.StateObservation;
import core.game.Observation;
import java.awt.RenderingHints;
import java.util.Random;
import tools.Vector2d;
import ontology.Types;
import java.util.*;

public class TreeSearchPlayer {

    private static final int MAX_DEPTH = -1;

    public boolean gameIsPuzzle;

    public int age;

    public int lastActionPicked;

    /**
     * Root of the tree.
     */
    public StateNode rootNode;

    /**
     * Root of the tree.
     */
    public StateObservation rootObservation;

    public int rootGameTick;

    public Vector2d[] pastAvatarPositions;
    
    public Vector2d[] pastAvatarOrientations;

    public int memoryLength;

    public int memoryIndex;

    //normalizing variables
    public double distancesNormalizer;

    public double maxResources;

    public ArrayList<StateObservation> statesInTheTree;

    /**
     * Random generator.
     */
    public Random randomGenerator;

    public double epsilon;

    public double emergencyFactor;

    public double discountFactor;

    public double learningRate;

    public double regularizationTerm;

    //attributes related to a set of feature-functions for approximate value function learning
    public double[] slopes;

    public double[] offsets;
    
    public double logisticSlope;

    public double[] w;

    //attributes related to linear regression, with y=ax+b
    public double[] a;
    public double b;

    //Storage of past transitions
    public Transition[][] observedTransitions;

    public int numberOfTransitionsStoredPerLayer;

    public int numberOfLayers;

    public int[] currentTransitionIndex;
    
    public Trajectory[][] observedTrajectories;
    
    public int numberOfTrajectoriesPerLayer;
    
    public int[] currentTajectoryIndex;

    public int nbFeatures;
    
    //new parameters for value approximation - adapt to number of types/category
    public int nbCategories;
    
    public int [] nbTypesPerCategory;
    
    public int nbBasisPerType;
    
    public IntArrayOfDoubleHashMap[] weights;
    
    public IntArrayOfDoubleHashMap[] weightsTraj;
    
    public int miniBatchSize;
    
    //basis function(x) = alpha + beta * step(threshold, x)
    public double[] alpha;
    
    public double [] beta;
    
    public double[] thresholds;
    
    //number of neighbors for KNN
    public int nbNeighbors;
    
    //parameters for the new location bias
    public double radiusThreshold;
    
    public int nbLocationsForThresholding;
    
    public int nbLocationsForReferencing;
    
    public boolean useValueApproximation;

    /**
     * Creates the MCTS player with a sampleRandom generator object.
     *
     * @param a_rnd sampleRandom generator object.
     * @param _initialState the initial state observation - used to initialize weights for value function approximation
     */
    public TreeSearchPlayer(Random a_rnd, StateObservation _initialState) {
        randomGenerator = a_rnd;
        epsilon = 0.15;
        discountFactor = 0.80;
        memoryLength = 1000;
        pastAvatarPositions = new Vector2d[memoryLength];
        pastAvatarOrientations = new Vector2d[memoryLength];
        memoryIndex = 0;
        miniBatchSize = 100;
        
        nbFeatures = 7;
        
        //new value approximation parameters:
        nbCategories = 7;
        // 0 : location bias
        // 1 : distance to resources
        // 2 : avatar resources
        // 3 : distance to NPC
        // 4 : distance to movable
        // 5 : distance to immovable
        // 6 : distance to portals
        
        nbTypesPerCategory = new int[nbFeatures];
        for(int i=0; i<nbFeatures; i++){
            nbTypesPerCategory[i] = 1;
        }
        
        if(_initialState.getResourcesPositions(_initialState.getAvatarPosition())!=null){
            nbTypesPerCategory[1] = _initialState.getResourcesPositions(_initialState.getAvatarPosition()).length;
        }
        if(_initialState.getNPCPositions(_initialState.getAvatarPosition())!=null){
            nbTypesPerCategory[3] = _initialState.getNPCPositions(_initialState.getAvatarPosition()).length;
        }
        if(_initialState.getMovablePositions(_initialState.getAvatarPosition())!=null){
            nbTypesPerCategory[4] = _initialState.getMovablePositions(_initialState.getAvatarPosition()).length;
        }
        if(_initialState.getImmovablePositions(_initialState.getAvatarPosition())!=null){
            nbTypesPerCategory[5] = _initialState.getImmovablePositions(_initialState.getAvatarPosition()).length;
        }
        if(_initialState.getPortalsPositions(_initialState.getAvatarPosition())!=null){
            nbTypesPerCategory[6] = _initialState.getPortalsPositions(_initialState.getAvatarPosition()).length;
        }
        
        nbBasisPerType = 5;
        
        //Initializing the hashmap array of weights
        weights = new IntArrayOfDoubleHashMap[nbCategories];
        
        for(int i=0; i<nbCategories; i++){
            weights[i] = new IntArrayOfDoubleHashMap();
        }
        
        weightsTraj = new IntArrayOfDoubleHashMap[nbCategories];
        for(int i=0; i<nbCategories; i++){
            weightsTraj[i] = new IntArrayOfDoubleHashMap();
        }
        
        //initialize basis function parameters
        alpha = new double[nbBasisPerType];
        beta = new double[nbBasisPerType];
        thresholds = new double[nbBasisPerType+1];
        offsets = new double[nbBasisPerType];
                
        for(int i=0; i<nbBasisPerType/2.0; i++){
            alpha[i] = 0.0;
            beta[i] = 1.0;
        }
        for(int i=(int) Math.floor(nbBasisPerType/2.0); i<nbBasisPerType; i++){
            alpha[i] = 1.0;
            beta[i] = -1.0;
        }
        
        offsets[0]=0.95;
        for(int i=1; i<nbBasisPerType; i++){
            offsets[i] = 1.0 - 2.0*(1.0 - offsets[i-1]);
        }
        
        logisticSlope = 30.0;
        
        thresholds[nbBasisPerType]=1.0;
        for(int i=nbBasisPerType-1; i>0; i--){
            thresholds[i] = thresholds[i+1] - 1.0/(double)nbBasisPerType;
        }
        thresholds[0]=0.0;
        
//        for(int i=0; i<nbBasisPerType; i++){
//            thresholds[i] = (double)i/(double)(nbBasisPerType-1);
//        }
        
        //initializing linear regression parameters
        a = new double[5];
        a[0] = 0.0;   //location bias
        a[1] = -0.1;   //distance to movable object
        a[2] = 0.1;   //avatar resource
        a[3] = -0.1;   //distance to resource
        a[4] = -0.1;    //distance to portal
        b = 0.0;

        //initializing parameters of the basis functions

        // System.out.println(Arrays.toString(slopes));
        // System.out.println(Arrays.toString(offsets));

        learningRate = 0.01;
        regularizationTerm = 1.0;

        //initialize transition storage
        numberOfTransitionsStoredPerLayer = 5000;
        numberOfLayers = 3;
        observedTransitions = new Transition[numberOfLayers][numberOfTransitionsStoredPerLayer];
        currentTransitionIndex = new int[numberOfLayers];
        
        numberOfTrajectoriesPerLayer = 5000;
        observedTrajectories = new Trajectory[numberOfLayers][numberOfTrajectoriesPerLayer];
        currentTajectoryIndex = new int[numberOfLayers];

        //initialize resource normalizing variable
        maxResources = 20.0;

        nbNeighbors = 10;
        
        lastActionPicked = -1;
        
        useValueApproximation = false;
    }

    /**
     * Inits the tree with the new observation state in the root.
     *
     * @param a_gameState current state of the game.
     */
    public void init(StateObservation a_gameState) {
        age = 0;
        distancesNormalizer = Math.sqrt((Math.pow(a_gameState.getWorldDimension().getHeight(), 2) + Math.pow(a_gameState.getWorldDimension().getWidth(), 2)));
        rootObservation = a_gameState;
        rootGameTick = rootObservation.getGameTick();
        //emergencyFactor = (double)rootGameTick/1000.0;
        //featureWeights[3] = -emergencyFactor;
        rootNode = new StateNode(a_gameState, randomGenerator, this);
        //rootNode.parentTree = this;
        pastAvatarPositions[memoryIndex] = rootObservation.getAvatarPosition();
        pastAvatarOrientations[memoryIndex] = rootObservation.getAvatarOrientation();
        if (memoryIndex < memoryLength - 1) {
            memoryIndex += 1;
        } else {
            memoryIndex = 0;
        }
//        updateLinearRegressionUsingDatabase();
        if(useValueApproximation){
            updateTreeAttributes(getFeaturesFromStateObs(a_gameState));

        
        if(rootGameTick % 10 == 0){
           updateBasisFunctionRegressionUsingDatabase();
           updateBasisFunctionRegressionUsingTrajectoryDatabase();
        }
        }

        statesInTheTree = new ArrayList<StateObservation>();
    }

    public void initializeWithSelectedBranch(int selectedBranch) {
        StateNode newRootNode = rootNode.children[selectedBranch];
        rootNode = newRootNode;
        rootNode.parentNode = null;
        rootObservation = rootNode.encounteredStates.get(0);
        rootGameTick = rootObservation.getGameTick();
        //System.out.format("initializing with avatar pos and tick %d "+ rootObservation.getAvatarPosition().toString(), rootGameTick);
        //emergencyFactor = (double)rootGameTick/100.0;
        //featureWeights[0] = -emergencyFactor;
        pastAvatarPositions[memoryIndex] = rootObservation.getAvatarPosition();
        pastAvatarOrientations[memoryIndex] = rootObservation.getAvatarOrientation();
        if (memoryIndex < memoryLength - 1) {
            memoryIndex += 1;
        } else {
            memoryIndex = 0;
        }
        age = 0;

        boolean result = areTwoStatesEqual(rootObservation, rootObservation);
        if (!result) {
            System.out.format("%n Error, usually state==state = true !");
        }
        statesInTheTree = new ArrayList<StateObservation>();
    }

    public void initializeWithBiasVector(StateObservation newState) {
        rootObservation = newState;

        StateNode newRootNode = new StateNode(newState, randomGenerator, this);
        // int i = 0;
        // while(i<newRootNode.actionChildren.length){
        //     if(rootNode.actionChildren[i]!=null){
        //         newRootNode.biasForActionSelection[i] = rootNode.actionChildren[i].expectimax;
        //     }
        //     i++;
        // }
        rootNode = newRootNode;
        //rootNode.parentTree = this;

        pastAvatarPositions[memoryIndex] = rootObservation.getAvatarPosition();
        pastAvatarOrientations[memoryIndex] = rootObservation.getAvatarOrientation();
        if (memoryIndex < memoryLength - 1) {
            memoryIndex += 1;
        } else {
            memoryIndex = 0;
        }
        distancesNormalizer = Math.sqrt((Math.pow(newState.getWorldDimension().getHeight(), 2) + Math.pow(newState.getWorldDimension().getWidth(), 2)));
    }

    /**
     * Runs one iteration of tree search
     */
    public void iterate() {
        ArrayList<IntDoubleHashMap[]> visitedStatesFeatures = new ArrayList<IntDoubleHashMap[]>();
        ArrayList<Double> visitedStatesScores = new ArrayList<Double>();
        //initialize useful variables
        StateObservation currentState = rootObservation.copy();
        StateNode currentStateNode = rootNode;
        int currentAction = 0;
        boolean stayInTree = true;
        int depth = 0;
        IntDoubleHashMap[] features1 = new IntDoubleHashMap[nbCategories];
        IntDoubleHashMap[] features2 = new IntDoubleHashMap[nbCategories];
        double score1;
        double score2 = 0.0;
        double instantReward;

        //System.out.format("game tick is %d%n ", currentState.getGameTick());
        //loop navigating through the tree
        while (stayInTree) {
//            features1 = currentStateNode.features;
            if(useValueApproximation){
                features1 = getFeaturesFromStateObs(currentState);
                visitedStatesFeatures.add(0, features1);
            }
            score1 = getValueOfState(currentState);
            visitedStatesScores.add(0, score1);
            

            if (currentStateNode.notFullyExpanded()) {
                //add a new action
                int bestActionIndex = 0;
                if((currentStateNode.numberOfSimulations < 2)&&(currentStateNode.parentNode!=null)){
                    bestActionIndex = currentStateNode.parentAction;
                }
                else{
                    double bestValue = -1;
                    for (int i = 0; i < currentStateNode.children.length; i++) {
                        double x = randomGenerator.nextDouble();
                        if ((x > bestValue) && (currentStateNode.children[i] == null)) {
                            bestActionIndex = i;
                            bestValue = x;
                        }
                    }
                }
                

                currentState.advance(Agent.actions[bestActionIndex]);
                //test here to see if the new state has extra types per category
                
                
                currentStateNode = currentStateNode.addStateNode(currentState, bestActionIndex);  //creates a new action node, with its child state node - It modifies currentState!

//                features2 = currentStateNode.features;
                score2 = getValueOfState(currentState);
                instantReward = score2 - score1;
                if(useValueApproximation){
                    features2 = getFeaturesFromStateObs(currentState);
                
                addTransition(features1, currentAction, features2, instantReward, currentState.isGameOver());
                updateTreeAttributes(features1);
                updateTreeAttributes(features2);
                }
                
                
                //updateWeightsUsingFeatures(features1, features2, instantReward);
                //System.out.format("%n before update, local game tick %d ",gametick1);
                //updateLinearWeightsUsingFeatures(features1, features2, instantReward);

//                if ((oldPosition.equals(newPosition)) && (!(oldOrientation.equals(newOrientation)))) { //changed orientation BUT didnt change position
//                    currentStateNode = currentStateNode.addStateNode(currentState.copy(), bestActionIndex);
//                }
                //if((getValueOfState(currentState) != initialValue)||(depth>MAX_DEPTH)) {
                stayInTree = false;
                //}
            } else {
                //select an action
                double x = randomGenerator.nextDouble();
                currentAction = currentStateNode.selectAction();
                if (x < epsilon) {
                    currentAction = currentStateNode.selectRandomAction();
                }
                currentStateNode.actionNbSimulations[currentAction] += 1;
                currentStateNode = currentStateNode.children[currentAction];

                if (true) {   //TODO: refine this condition to something that triggers calling the forward model
                    currentState.advance(Agent.actions[currentAction]); //updates the current state with the forward model
                    //currentStateNode.updateData(currentState.copy());
                } else {
                    currentState = currentStateNode.encounteredStates.get(0);
                }

//                features2 = currentStateNode.features;
                score2 = getValueOfState(currentState);
                //featureFunctions2 = getFeatureFunctionsFromFeatures(features2);
                instantReward = score2 - score1;
                if(useValueApproximation){
                    features2 = getFeaturesFromStateObs(currentState);
                addTransition(features1, currentAction, features2, instantReward, currentState.isGameOver());
                updateTreeAttributes(features1);
                updateTreeAttributes(features2);
                }
                
                
                //System.out.format("%n before update, local game tick %d ",gametick1);
                //updateLinearWeightsUsingFeatures(features1, features2, instantReward);

                if (currentState.isGameOver()) {
                    stayInTree = false;
                }
            }
            depth++;
        }
        
        visitedStatesFeatures.add(0, features2);
        visitedStatesScores.add(0, score2);

        currentStateNode.backPropagateData(currentState, visitedStatesFeatures, visitedStatesScores);
    }

    /**
     * Checks if the game is a puzzle
     *
     * @return the action to execute in the game.
     */
    public boolean checkIfGameIsPuzzle() {
        ArrayList<Types.ACTIONS> actionList = rootObservation.getAvailableActions(true);
        Types.ACTIONS[] _actions = new Types.ACTIONS[actionList.size()];

        for (int i = 0; i < _actions.length; ++i) {
            _actions[i] = actionList.get(i);
        }

        //System.out.format("actions length %d ", _actions.length);
        StateObservation currentState = rootObservation.copy();
        currentState.advance(_actions[0]);

        return compareTwoStates(currentState, rootObservation);
        //Determine the best action to take and return it. 
    }

    public boolean compareTwoStates(StateObservation s1, StateObservation s2) {
//        if ((s1.getResourcesPositions(s1.getAvatarPosition()) != null) && (s2.getResourcesPositions(s2.getAvatarPosition()) != null) && (!s1.getResourcesPositions(s1.getAvatarPosition())[0].isEmpty()) && (!s2.getResourcesPositions(s2.getAvatarPosition())[0].isEmpty())) {
//            if (!s1.getResourcesPositions(s1.getAvatarPosition())[0].get(0).position.equals(s2.getResourcesPositions(s2.getAvatarPosition())[0].get(0).position)) {
//                //System.out.format("resources not the same ");
//                return false;
//            }
//        }
//        if ((s1.getNPCPositions(s1.getAvatarPosition()) != null) && (s2.getNPCPositions(s2.getAvatarPosition()) != null) && (!s1.getNPCPositions(s1.getAvatarPosition())[0].isEmpty()) && (!s2.getNPCPositions(s2.getAvatarPosition())[0].isEmpty())) {
//            if (!s1.getNPCPositions(s1.getAvatarPosition())[0].get(0).position.equals(s2.getNPCPositions(s2.getAvatarPosition())[0].get(0).position)) {
//                //System.out.format("NPC not the same ");
//                return false;
//            }
//        }

        int i = 0;
        int j = 0;
        
        if ((s1.getNPCPositions(s1.getAvatarPosition()) != null) && (s2.getNPCPositions(s2.getAvatarPosition()) != null) ) {
            if(s1.getNPCPositions(s1.getAvatarPosition()).length != s2.getNPCPositions(s2.getAvatarPosition()).length){
                return false;
            }
            else{
                while(i<s1.getNPCPositions(s1.getAvatarPosition()).length){
                    if(s1.getNPCPositions(s1.getAvatarPosition())[i].size() != s2.getNPCPositions(s2.getAvatarPosition())[i].size()){
                        return false;
                    }
                    else{
                        while(j<s1.getNPCPositions(s1.getAvatarPosition())[i].size()){
                            if(!s1.getNPCPositions(s1.getAvatarPosition())[i].get(j).equals(s2.getNPCPositions(s2.getAvatarPosition())[i].get(j))){
                                return false;
                            }
                            j++;
                        }
                    }
                    i++;
                }
            }
        }
        return true;
    }

    /**
     * Fetches the best action, according to the current tree.
     *
     * @return the action to execute in the game.
     */
    public int returnBestAction() {
        //Determine the best action to take and return it.
//        return rootNode.getHighestScoreAction();
        if(rootNode.getHighestScoreAction() != rootNode.getMostVisitedAction()){
            int a = 1;
        }
        return rootNode.getHighestScoreAction();
    }

    public double getValueOfState(StateObservation a_gameState) {

        boolean gameOver = a_gameState.isGameOver();
        Types.WINNER win = a_gameState.getGameWinner();
        double rawScore = a_gameState.getGameScore();

        if (gameOver && win == Types.WINNER.PLAYER_LOSES) {
            return (rawScore - 2000.0 * (1.0 + Math.abs(rawScore)));
        }

        if (gameOver && win == Types.WINNER.PLAYER_WINS) {
//            return (rawScore + 2.0 * (1.0 + Math.abs(rawScore)));
            return ((rawScore + 1.0) * (double)a_gameState.getGameTick()/1000.0);
        }

        return rawScore;
    }

    public IntArrayOfDoubleHashMap[] getBasisFunctionsFromFeatures(IntDoubleHashMap[] _features) {
        IntArrayOfDoubleHashMap[] result = new IntArrayOfDoubleHashMap[nbCategories];
        for(int i=0; i<nbCategories; i++){
            result[i] = new IntArrayOfDoubleHashMap();
            for(Integer type : _features[i].keySet()){
                double[] basisFunctionsForThisType = new double[nbBasisPerType];
                for(int j=0; j<nbBasisPerType; j++){
                    basisFunctionsForThisType[j] = logisticFunction(_features[i].get(type), offsets[j], logisticSlope);
//                    basisFunctionsForThisType[j] = alpha[j] + beta[j]* stumpFunction(_features[i].get(type) - thresholds[j]);
//                      basisFunctionsForThisType[j] = impulseFunction(_features[i].get(type), thresholds[j], thresholds[j+1]);
//                      basisFunctionsForThisType[j] = _features[i].get(type);
//                    basisFunctionsForThisType[j] = pyramidFunction(_features[i].get(type) - thresholds[j]);
                }
                result[i].put(type, basisFunctionsForThisType);
            }
        }
        return result;
    }

    public double getBasisFunctionLinearApproximation(IntArrayOfDoubleHashMap[] _basisFunctions) {
        double result = 0.0;
        for (int i = 0; i < nbCategories; i++) {
            for(Integer type : _basisFunctions[i].keySet()){
                for(int j=0; j<nbBasisPerType; j++){
                    result += weights[i].get(type)[j] * _basisFunctions[i].get(type)[j];
                }
            }
//            for(int j=0; j<_basisFunctions[i].length; j++){
//                for(int k=0; k<nbBasisPerType; k++){
//                    result += weights[i][j][k]*_basisFunctions[i][j][k];
//                }
//            }
        }
        return result;
    }

//    public double getValueApproximation(double[] _features){
//        double result = 0.0;
//        for(int i=0; i<featureWeights.length; i++){
//            result += featureWeights[i]*(_features[i]+featureOffsets[i]);
//        }
//        return result;
//    }
    public double getLinearRegression(HashMap<Integer, Double>[] _features) {
        double result = 0.0;
        for (int i = 0; i < nbCategories; i++) {
            for(Integer type : _features[i].keySet()){
                result += a[i] * _features[i].get(type);
            }
        }
        return (result + b);
    }

//    public double getLogisticValueApproximation(double[] _features){
//        double f = 0.0;
//        for(int i=0; i<featureWeights.length; i++){
//            f += featureWeights[i]*(_features[i]-featureOffsets[i]);
//        }
//        double result = 2.0/(1.0+approxExpo(-f)) - 1.0;
//        return result;
//    }
    
    public double logisticFunction(double x, double offset, double slope){
        double result = 1.0/(1.0 + Math.exp(-slope*(x-offset)) );
        return result;
    }
    
    public double approxExpo(double x) {
        double result = 1.0 + x / 6.0;
        result = Math.pow(result, 6);
        return result;
    }

    public double stumpFunction(double x) {
        double result = 1.0;
        if (x < 0) {
            result = 0.0;
        }
        return result;
    }
    
    public double impulseFunction(double x, double lb, double ub) {
        double result = 0.0;
        if ((x >= lb)&&(x<ub)) {
            result = 1.0;
        }
        return result;
    }
    
    public double pyramidFunction(double x) {
        double result = Math.max(0.5-Math.abs(x), 0.0);
        return result;
    }

    public void printWeights() {
        System.out.format("%n weights: ");
        for (int i = 0; i < w.length; i++) {
            System.out.format(", %f ", w[i]);
        }
    }

    public boolean areTwoStatesEqual(StateObservation _s1, StateObservation _s2) {
        boolean result = true;

        //check score
        if (!(_s1.getGameScore() == _s2.getGameScore())) {
            return false;
        }

        //check game over
        if (!(_s1.isGameOver() == _s2.isGameOver())) {
            return false;
        }

        //check avatar status
        if (!(_s1.getAvatarPosition().equals(_s2.getAvatarPosition()))) {
            return false;
        }
        if (!(_s1.getAvatarSpeed() == _s2.getAvatarSpeed())) {
            return false;
        }
        if (!(_s1.getAvatarOrientation().equals(_s2.getAvatarOrientation()))) {
            return false;
        }
        //TODO avatar resources

        //check observations
        //NPCs
        ArrayList<Observation>[] _s1NPCs = _s1.getNPCPositions();
        ArrayList<Observation>[] _s2NPCs = _s2.getNPCPositions();
        if ((_s1NPCs == null) != (_s2NPCs == null)) {
            return false;
        }

        if (_s1NPCs != null) {
            if (_s1NPCs.length != _s2NPCs.length) {
                return false;
            }
            for (int i = 0; i < _s1NPCs.length; i++) {
                if (_s1NPCs[i].size() != _s2NPCs[i].size()) {
                    return false;
                }
                for (int j = 0; j < _s1NPCs[i].size(); j++) {
                    if (!(_s1NPCs[i].get(j).equals(_s2NPCs[i].get(j)))) {
                        return false;
                    }
                }
            }
        }

        //Immovables
        ArrayList<Observation>[] _s1Immovables = _s1.getImmovablePositions();
        ArrayList<Observation>[] _s2Immovables = _s2.getImmovablePositions();
        if ((_s1Immovables == null) != (_s2Immovables == null)) {
            return false;
        }

        if (_s1Immovables != null) {
            if (_s1Immovables.length != _s2Immovables.length) {
                return false;
            }
            for (int i = 0; i < _s1Immovables.length; i++) {
                if (_s1Immovables[i].size() != _s2Immovables[i].size()) {
                    return false;
                }
                for (int j = 0; j < _s1Immovables[i].size(); j++) {
                    if (!(_s1Immovables[i].get(j).equals(_s2Immovables[i].get(j)))) {
                        return false;
                    }
                }
            }
        }

        //Movables
        ArrayList<Observation>[] _s1Movables = _s1.getMovablePositions();
        ArrayList<Observation>[] _s2Movables = _s2.getMovablePositions();
        if ((_s1Movables == null) != (_s2Movables == null)) {
            return false;
        }

        if (_s1Movables != null) {
            if (_s1Movables.length != _s2Movables.length) {
                return false;
            }
            for (int i = 0; i < _s1Movables.length; i++) {
                if (_s1Movables[i].size() != _s2Movables[i].size()) {
                    return false;
                }
                for (int j = 0; j < _s1Movables[i].size(); j++) {
                    if (!(_s1Movables[i].get(j).equals(_s2Movables[i].get(j)))) {
                        return false;
                    }
                }
            }
        }

        //Resources
        ArrayList<Observation>[] _s1Resources = _s1.getResourcesPositions();
        ArrayList<Observation>[] _s2Resources = _s2.getResourcesPositions();
        if ((_s1Resources == null) != (_s2Resources == null)) {
            return false;
        }

        if (_s1Resources != null) {
            if (_s1Resources.length != _s2Resources.length) {
                return false;
            }
            for (int i = 0; i < _s1Resources.length; i++) {
                if (_s1Resources[i].size() != _s2Resources[i].size()) {
                    return false;
                }
                for (int j = 0; j < _s1Resources[i].size(); j++) {
                    if (!(_s1Resources[i].get(j).equals(_s2Resources[i].get(j)))) {
                        return false;
                    }
                }
            }
        }

        //Portals
        ArrayList<Observation>[] _s1Portals = _s1.getPortalsPositions();
        ArrayList<Observation>[] _s2Portals = _s2.getPortalsPositions();
        if ((_s1Portals == null) != (_s2Portals == null)) {
            return false;
        }

        if (_s1Portals != null) {
            if (_s1Portals.length != _s2Portals.length) {
                return false;
            }
            for (int i = 0; i < _s1Portals.length; i++) {
                if (_s1Portals[i].size() != _s2Portals[i].size()) {
                    return false;
                }
                for (int j = 0; j < _s1Portals[i].size(); j++) {
                    if (!(_s1Portals[i].get(j).equals(_s2Portals[i].get(j)))) {
                        return false;
                    }
                }
            }
        }

        //From avatar sprites
        ArrayList<Observation>[] _s1FromAvatar = _s1.getFromAvatarSpritesPositions();
        ArrayList<Observation>[] _s2FromAvatar = _s2.getFromAvatarSpritesPositions();
        if ((_s1FromAvatar == null) != (_s2FromAvatar == null)) {
            return false;
        }

        if (_s1FromAvatar != null) {
            if (_s1FromAvatar.length != _s2FromAvatar.length) {
                return false;
            }
            for (int i = 0; i < _s1FromAvatar.length; i++) {
                if (_s1FromAvatar[i].size() != _s2FromAvatar[i].size()) {
                    return false;
                }
                for (int j = 0; j < _s1FromAvatar[i].size(); j++) {
                    if (!(_s1FromAvatar[i].get(j).equals(_s2FromAvatar[i].get(j)))) {
                        return false;
                    }
                }
            }
        }

        return result;
    }

    public boolean isThisStateInTheTree(StateObservation _s) {
        boolean result = false;
        for (int i = 0; i < statesInTheTree.size(); i++) {
            if (areTwoStatesEqual(_s, statesInTheTree.get(i))) {
                return true;
            }
        }
        return result;
    }

    public void addTransition(IntDoubleHashMap[] _feature1, int _action, IntDoubleHashMap[] _feature2, double _reward, boolean _isFinal) {
        IntArrayOfDoubleHashMap[] basisFunctions1 = getBasisFunctionsFromFeatures(_feature1);
        IntArrayOfDoubleHashMap[] basisFunctions2 = getBasisFunctionsFromFeatures(_feature2);
        
        Transition _transition = new Transition(_feature1, _action, _feature2, _reward, _isFinal, basisFunctions1, basisFunctions2);

        if (_reward < 0.0) {
            observedTransitions[0][currentTransitionIndex[0]] = _transition;
            if (currentTransitionIndex[0] < numberOfTransitionsStoredPerLayer - 1) {
                currentTransitionIndex[0] += 1;
            } else {
                currentTransitionIndex[0] = 0;
            }
        } else if (_reward == 0.0) {
            observedTransitions[1][currentTransitionIndex[1]] = _transition;
            if (currentTransitionIndex[1] < numberOfTransitionsStoredPerLayer - 1) {
                currentTransitionIndex[1] += 1;
            } else {
                currentTransitionIndex[1] = 0;
            }
        } else {
            observedTransitions[2][currentTransitionIndex[2]] = _transition;
            if (currentTransitionIndex[2] < numberOfTransitionsStoredPerLayer - 1) {
                currentTransitionIndex[2] += 1;
            } else {
                currentTransitionIndex[2] = 0;
            }
        }
    }
    
    public void addTrajectory(IntDoubleHashMap[] _feature1, IntDoubleHashMap[] _feature2, double _reward, boolean _isFinal, int _length) {
        IntArrayOfDoubleHashMap[] basisFunctions1 = getBasisFunctionsFromFeatures(_feature1);
        IntArrayOfDoubleHashMap[] basisFunctions2 = getBasisFunctionsFromFeatures(_feature2);
        
        Trajectory _trajectory = new Trajectory(_feature1, _feature2, _reward, _isFinal, _length, basisFunctions1, basisFunctions2);

        if (_reward < 0.0) {
            observedTrajectories[0][currentTajectoryIndex[0]] = _trajectory;
            if (currentTajectoryIndex[0] < numberOfTrajectoriesPerLayer - 1) {
                currentTajectoryIndex[0] += 1;
            } else {
                currentTajectoryIndex[0] = 0;
            }
        } else if (_reward == 0.0) {
            observedTrajectories[1][currentTajectoryIndex[1]] = _trajectory;
            if (currentTajectoryIndex[1] < numberOfTrajectoriesPerLayer - 1) {
                currentTajectoryIndex[1] += 1;
            } else {
                currentTajectoryIndex[1] = 0;
            }
        } else {
            observedTrajectories[2][currentTajectoryIndex[2]] = _trajectory;
            if (currentTajectoryIndex[2] < numberOfTrajectoriesPerLayer - 1) {
                currentTajectoryIndex[2] += 1;
            } else {
                currentTajectoryIndex[2] = 0;
            }
        }
    }

    public void updateBasisFunctionRegressionUsingDatabase() {
        double _localDiscount;
        double _reward;
        Transition currentTransition;

        IntArrayOfDoubleHashMap[] basisFunctions1 = new IntArrayOfDoubleHashMap[nbCategories];
        IntArrayOfDoubleHashMap[] basisFunctions2 = new IntArrayOfDoubleHashMap[nbCategories];

        IntArrayOfDoubleHashMap[][] gradientW = new IntArrayOfDoubleHashMap[nbCategories][numberOfLayers];
        for(int i=0; i<nbCategories; i++){
            for(int j=0; j<numberOfLayers; j++){
                gradientW[i][j] = new IntArrayOfDoubleHashMap();
            }
        }

        double observedValue;
        double belief;
        double error;
        
        int[][] miniBatchIndexes = new int[numberOfLayers][];
        int[] actualNumberOfTransitionsPerLayer = new int[numberOfLayers];
        int a;
        int m;
        Random randomGenerator = new Random();
        
        int nbTransitions = 0;

        double[] rewards = new double[numberOfLayers];
        rewards[0] = -1.0;
        rewards[1] = 0.0;
        rewards[2] = 1.0;

        int[] countTransitions = new int[numberOfLayers];

        for (int l = 0; l < numberOfLayers; l++) {
            m=0;
            while ((m < numberOfTransitionsStoredPerLayer) && (observedTransitions[l][m] != null)) {
                m++;
            }
            actualNumberOfTransitionsPerLayer[l]=m;
           miniBatchIndexes[l] = new int[Math.min(miniBatchSize, actualNumberOfTransitionsPerLayer[l])];
           for (int b=miniBatchIndexes[l].length-1; b>-1; b--){
                miniBatchIndexes[l][b] = b;
            }
           for (int b=miniBatchIndexes[l].length-1; b>-1; b--){
                a = randomGenerator.nextInt(b+1);
                miniBatchIndexes[l][b] = miniBatchIndexes[l][a];
                miniBatchIndexes[l][a] = b;
            }
            m = 0;
            for(int k=0; k<miniBatchIndexes[l].length; k++){
                m = miniBatchIndexes[l][k];
                nbTransitions += 1;
                countTransitions[l] += 1;
                currentTransition = observedTransitions[l][m];
                basisFunctions1 = currentTransition.basisFunctionValues1;
                basisFunctions2 = currentTransition.basisFunctionValues2;

                _reward = currentTransition.reward;

                belief = getBasisFunctionLinearApproximation(basisFunctions1);
                observedValue = rewards[l] + discountFactor * getBasisFunctionLinearApproximation(basisFunctions2);
                if(currentTransition.isFinal){
//                if (true) {
                    observedValue = rewards[l];
                    _localDiscount = 0.0;
                } else {
                    _localDiscount = discountFactor;
                }
                error = observedValue - belief;
                
                for (int i = 0; i < nbCategories; i++) {
                    for(Integer type : basisFunctions1[i].keySet()){
//                        if(basisFunctions2[i].containsKey(type)){
                        if(true){
                            double[] newGradient = new double[nbBasisPerType];
                            for(int j=0; j<nbBasisPerType; j++){
//                                newGradient[j] = learningRate * error * (basisFunctions1[i].get(type)[j]);
                                newGradient[j] = learningRate * (error * (basisFunctions1[i].get(type)[j]) + regularizationTerm * Math.signum(weights[i].get(type)[j]));
                            }
                            if(!gradientW[i][l].containsKey(type)){
                                gradientW[i][l].put(type, newGradient);
                            }
                            else{
                                for(int j=0; j<nbBasisPerType; j++){
                                    double lalala =1;
                                    gradientW[i][l].get(type)[j] += newGradient[j];
                                    double lilili =2;
                                }
                            }
                        }
                    }
                }
            }
        }

        double[] gradientWeights = new double[numberOfLayers];

        for (int l = 0; l < numberOfLayers; l++) {
            if (countTransitions[l] > 0) {
//                gradientWeights[l] = (double) nbTransitions / ((double) numberOfLayers * (double) countTransitions[l]);
                gradientWeights[l] = 1.0 / ((double) countTransitions[l]);
            }
        }

        for (int i = 0; i < nbCategories; i++) {
            for(Integer type : weights[i].keySet()){
                double[] newWeights = new double[nbBasisPerType];
                for(int j=0; j<nbBasisPerType; j++){
//                    newWeights[j] = (1.0 - regularizationTerm) * weights[i].get(type)[j];
                    for(int l=0; l<numberOfLayers; l++){
                        if(gradientW[i][l].containsKey(type)){
                            newWeights[j] -= gradientWeights[l] * gradientW[i][l].get(type)[j];
                        }
                    }
                }
                for(int j=0; j<nbBasisPerType; j++){
                    weights[i].get(type)[j] = newWeights[j];
                }
                
            }
            if((rootGameTick%10==0)&&(i==3)){
                int debug = 1;
            }
        }
        
        for(int k=0; k<nbBasisPerType; k++){
            weights[0].get(0)[k]=0.0;
        }
        
        if(rootObservation.getGameTick() > 10){
        //printWeights();
        }
    }
    
    public void updateBasisFunctionRegressionUsingTrajectoryDatabase() {
        double _localDiscount;
        double _reward;
        Trajectory currentTrajectory;

        IntArrayOfDoubleHashMap[] basisFunctions1 = new IntArrayOfDoubleHashMap[nbCategories];
        IntArrayOfDoubleHashMap[] basisFunctions2 = new IntArrayOfDoubleHashMap[nbCategories];

        IntArrayOfDoubleHashMap[][] gradientW = new IntArrayOfDoubleHashMap[nbCategories][numberOfLayers];
        for(int i=0; i<nbCategories; i++){
            for(int j=0; j<numberOfLayers; j++){
                gradientW[i][j] = new IntArrayOfDoubleHashMap();
            }
        }

        double observedValue;
        double belief;
        double error;
        
        int[][] miniBatchIndexes = new int[numberOfLayers][];
        int[] actualNumberOfTransitionsPerLayer = new int[numberOfLayers];
        int a;
        int m;
        Random randomGenerator = new Random();
        
        int nbTransitions = 0;

        double[] rewards = new double[numberOfLayers];
        rewards[0] = -1.0;
        rewards[1] = 0.0;
        rewards[2] = 1.0;

        int[] countTransitions = new int[numberOfLayers];

        for (int l = 0; l < numberOfLayers; l++) {
            m=0;
            while ((m < numberOfTrajectoriesPerLayer) && (observedTrajectories[l][m] != null)) {
                m++;
            }
            actualNumberOfTransitionsPerLayer[l]=m;
           miniBatchIndexes[l] = new int[Math.min(miniBatchSize, actualNumberOfTransitionsPerLayer[l])];
           for (int b=miniBatchIndexes[l].length-1; b>-1; b--){
                miniBatchIndexes[l][b] = b;
            }
           for (int b=miniBatchIndexes[l].length-1; b>-1; b--){
                a = randomGenerator.nextInt(b+1);
                miniBatchIndexes[l][b] = miniBatchIndexes[l][a];
                miniBatchIndexes[l][a] = b;
            }
            m = 0;
            for(int k=0; k<miniBatchIndexes[l].length; k++){
                m = miniBatchIndexes[l][k];
                nbTransitions += 1;
                countTransitions[l] += 1;
                currentTrajectory = observedTrajectories[l][m];
                basisFunctions1 = currentTrajectory.basisFunctionValues1;
                basisFunctions2 = currentTrajectory.basisFunctionValues2;

                _reward = currentTrajectory.reward;

                belief = getBasisFunctionLinearApproximation(basisFunctions1);
                observedValue = rewards[l] + 0.7 * getBasisFunctionLinearApproximation(basisFunctions2);
                if(currentTrajectory.isFinal){
//                if (true) {
                    observedValue = rewards[l];
                    _localDiscount = 0.0;
                } else {
                    _localDiscount = discountFactor;
                }
                error = belief - observedValue;
                
                for (int i = 0; i < nbCategories; i++) {
                    for(Integer type : basisFunctions1[i].keySet()){
//                        if(basisFunctions2[i].containsKey(type)){
                        if(true){
                            double[] newGradient = new double[nbBasisPerType];
                            for(int j=0; j<nbBasisPerType; j++){
                                newGradient[j] = learningRate * (error * (basisFunctions1[i].get(type)[j]));
                            }
                            if(!gradientW[i][l].containsKey(type)){
                                gradientW[i][l].put(type, newGradient);
                            }
                            else{
                                for(int j=0; j<nbBasisPerType; j++){
                                    double lalala =1;
                                    gradientW[i][l].get(type)[j] += newGradient[j];
                                    double lilili =2;
                                }
                            }
                        }
                    }
                }
            }
        }

        double[] gradientWeights = new double[numberOfLayers];

        for (int l = 0; l < numberOfLayers; l++) {
            if (countTransitions[l] > 0) {
//                gradientWeights[l] = (double) nbTransitions / ((double) numberOfLayers * (double) countTransitions[l]);
                gradientWeights[l] = 1.0 / ((double) countTransitions[l]);
            }
        }

        for (int i = 0; i < nbCategories; i++) {
            for(Integer type : weightsTraj[i].keySet()){
                double[] newWeights = new double[nbBasisPerType];
                for(int j=0; j<nbBasisPerType; j++){
                    newWeights[j] = (1.0 - regularizationTerm) * weightsTraj[i].get(type)[j];
                    for(int l=0; l<numberOfLayers; l++){
                        if(gradientW[i][l].containsKey(type)){
                            newWeights[j] -= gradientWeights[l] * gradientW[i][l].get(type)[j];
                        }
                    }
                }
                for(int j=0; j<nbBasisPerType; j++){
                    weightsTraj[i].get(type)[j] = newWeights[j];
                }
                
            }
            if((rootGameTick%100==0)&&(i==3)){
                int debug = 1;
            }
        }
        
        for(int k=0; k<nbBasisPerType; k++){
            weightsTraj[0].get(0)[k]=0.0;
        }
        
        if(rootObservation.getGameTick() > 10){
        //printWeights();
        }
    }

//    public double[][] normalizeFeatures(double[][] _features) {
//        double[][] result = _features;
//        result = _features;
//        double hardCodedNormalizingConstant = 1000.0;
//        for(int i=0; i<result[2].length; i++){
//            result[2][i] = result[2][i] / hardCodedNormalizingConstant;
//        }
//        return result;
//    }
    
    public void updateTreeAttributes(IntDoubleHashMap[] _features){
        
        for(int i=0; i<_features.length; i++){
            for(Integer type : _features[i].keySet()){
                if(!weights[i].containsKey(type)){
                    double[] newWeightVector = new double[nbBasisPerType];
                    for(int j=0; j<newWeightVector.length; j++){
                        newWeightVector[j] = 0.1;
                    }
//                    newWeightVector[0]=0.1;
                    weights[i].put(type, newWeightVector);
                }
                
                if(!weightsTraj[i].containsKey(type)){
                    double[] newWeightVector = new double[nbBasisPerType];
                    for(int j=0; j<newWeightVector.length; j++){
                        newWeightVector[j] = 0.1;
                    }
//                    newWeightVector[0]=0.1;
                    weightsTraj[i].put(type, newWeightVector);
                }
            }
        }
        
//        if((_stateObs.getResourcesPositions(_stateObs.getAvatarPosition()) != null)){
//            for(ArrayList<Observation> resourcesPositions : _stateObs.getResourcesPositions(_stateObs.getAvatarPosition())){
//                if(!weights[1].containsKey(resourcesPositions.get(0).itype)){
//                    weights[1].put(resourcesPositions.get(0).itype, new double[nbBasisPerType]);
//                }
//            }
//        }
//        
//        if(_stateObs.getNPCPositions(_stateObs.getAvatarPosition()) != null){
//            for(ArrayList<Observation> resourcesPositions : _stateObs.getNPCPositions(_stateObs.getAvatarPosition())){
//                if(!weights[3].containsKey(resourcesPositions.get(0).itype)){
//                    weights[3].put(resourcesPositions.get(0).itype, new double[nbBasisPerType]);
//                }
//            }
//        }
//        
//        if(_stateObs.getMovablePositions(_stateObs.getAvatarPosition()) != null){
//            for(ArrayList<Observation> resourcesPositions : _stateObs.getMovablePositions(_stateObs.getAvatarPosition())){
//                if(!weights[4].containsKey(resourcesPositions.get(0).itype)){
//                    weights[4].put(resourcesPositions.get(0).itype, new double[nbBasisPerType]);
//                }
//            }
//        }
//        
//        if(_stateObs.getImmovablePositions(_stateObs.getAvatarPosition()) != null){
//            for(ArrayList<Observation> resourcesPositions : _stateObs.getImmovablePositions(_stateObs.getAvatarPosition())){
//                if(!weights[5].containsKey(resourcesPositions.get(0).itype)){
//                    weights[5].put(resourcesPositions.get(0).itype, new double[nbBasisPerType]);
//                }
//            }
//        }
//        
//        if(_stateObs.getPortalsPositions(_stateObs.getAvatarPosition()) != null){
//            for(ArrayList<Observation> resourcesPositions : _stateObs.getPortalsPositions(_stateObs.getAvatarPosition())){
//                if(!weights[6].containsKey(resourcesPositions.get(0).itype)){
//                    weights[6].put(resourcesPositions.get(0).itype, new double[nbBasisPerType]);
//                }
//            }
//        }
        
    }
    
    public IntDoubleHashMap[] getFeaturesFromStateObs(StateObservation _state){
        IntDoubleHashMap locationBias = new IntDoubleHashMap();
        IntDoubleHashMap distanceToResources = new IntDoubleHashMap();
        IntDoubleHashMap totalAvatarResources = new IntDoubleHashMap();
        IntDoubleHashMap distanceToNPC = new IntDoubleHashMap();
        IntDoubleHashMap distanceToMovables = new IntDoubleHashMap();
        IntDoubleHashMap distanceToImmovables = new IntDoubleHashMap();
        IntDoubleHashMap distanceToPortals = new IntDoubleHashMap();
        
        //Computing Deterministic bias values
        int parsingIndex = 0;
        double tempLocationBias = 0.0;
        while ((parsingIndex < memoryLength) && (pastAvatarPositions[parsingIndex] != null)) {
            if ((pastAvatarPositions[parsingIndex].equals(_state.getAvatarPosition()))) {
                tempLocationBias+=0.01;
            }
            parsingIndex++;
        }
        locationBias.put(0, 1.0 - tempLocationBias);
        
        
        
        if(_state.getResourcesPositions((_state.getAvatarPosition())) != null){
            parsingIndex = 0;
            while(parsingIndex<_state.getResourcesPositions((_state.getAvatarPosition())).length){
                if(!_state.getResourcesPositions((_state.getAvatarPosition()))[parsingIndex].isEmpty()){
                    distanceToResources.put(_state.getResourcesPositions((_state.getAvatarPosition()))[parsingIndex].get(0).itype, 1.0 - Math.sqrt(_state.getResourcesPositions((_state.getAvatarPosition()))[parsingIndex].get(0).sqDist) / distancesNormalizer);
                }
                parsingIndex++;
            }
        }

        double tempTotalResources = 0.0;
        if(!_state.getAvatarResources().isEmpty()){
            for (int key : _state.getAvatarResources().keySet()) {
                tempTotalResources += _state.getAvatarResources().get(key)/maxResources;
            }
            totalAvatarResources.put(0, 1.0 - tempTotalResources);
        }
        
        
        if(_state.getNPCPositions(_state.getAvatarPosition()) != null){
            parsingIndex = 0;
            while(parsingIndex<_state.getNPCPositions((_state.getAvatarPosition())).length){
                if(!_state.getNPCPositions((_state.getAvatarPosition()))[parsingIndex].isEmpty()){
                    distanceToNPC.put(_state.getNPCPositions((_state.getAvatarPosition()))[parsingIndex].get(0).itype, 1.0 - Math.sqrt(_state.getNPCPositions((_state.getAvatarPosition()))[parsingIndex].get(0).sqDist) / distancesNormalizer);
                }
                parsingIndex++;
            }
        }
        
        if(_state.getMovablePositions((_state.getAvatarPosition())) != null){
            parsingIndex = 0;
            while(parsingIndex<_state.getMovablePositions((_state.getAvatarPosition())).length){
                if(!_state.getMovablePositions((_state.getAvatarPosition()))[parsingIndex].isEmpty()){
                    distanceToMovables.put(_state.getMovablePositions((_state.getAvatarPosition()))[parsingIndex].get(0).itype, 1.0 - Math.sqrt(_state.getMovablePositions((_state.getAvatarPosition()))[parsingIndex].get(0).sqDist) / distancesNormalizer);
                }
                parsingIndex++;
            }
        }
        
//        if(_state.getImmovablePositions((_state.getAvatarPosition())) != null){
//            parsingIndex = 0;
//            while((parsingIndex<_state.getImmovablePositions((_state.getAvatarPosition())).length)&&(parsingIndex<parentTree.nbTypesPerCategory[5])){
//                if(!_state.getImmovablePositions((_state.getAvatarPosition()))[parsingIndex].isEmpty()){
//                    distanceToImmovables[parsingIndex] = Math.sqrt(_state.getImmovablePositions((_state.getAvatarPosition()))[parsingIndex].get(0).sqDist) / parentTree.distancesNormalizer;
//                }
//                parsingIndex++;
//            }
//        }
        
        if(_state.getPortalsPositions((_state.getAvatarPosition())) != null){
            parsingIndex = 0;
            while(parsingIndex<_state.getPortalsPositions((_state.getAvatarPosition())).length){
                if(!_state.getPortalsPositions((_state.getAvatarPosition()))[parsingIndex].isEmpty()){
                    distanceToPortals.put(_state.getPortalsPositions((_state.getAvatarPosition()))[parsingIndex].get(0).itype, 1.0 - Math.sqrt(_state.getPortalsPositions((_state.getAvatarPosition()))[parsingIndex].get(0).sqDist) / distancesNormalizer);
                }
                parsingIndex++;
            }
        }

        IntDoubleHashMap[] features = new IntDoubleHashMap[nbCategories];
        for(int i=0; i<nbCategories; i++){
            features[i] = new IntDoubleHashMap();
        }
        
        features[0] = locationBias;
        features[1] = distanceToResources;
        features[2] = totalAvatarResources;
        features[3] = distanceToNPC;
        features[4] = distanceToMovables;
        features[5] = distanceToImmovables;
        features[6] = distanceToPortals;
        
        return features;
    }
    
    double getKNNValue(IntDoubleHashMap[] _features){
        double result = 0.0;
        double[] neighborsValue = new double[nbNeighbors];
        double[] neighborsDistances = new double[nbNeighbors];
        int nextBossIndex;
        int actualNbNeighbors = 0;
        
        boolean challenge;
        IntDoubleHashMap[] tempFeatures = new IntDoubleHashMap[nbFeatures];
        double tempValue;
        int layerIndex = 0;
        int intraLayerIndex = 0;
        
        while(layerIndex < observedTransitions.length){ //for each layer of observations
            while((intraLayerIndex<20)&&(observedTransitions[layerIndex][intraLayerIndex]!=null)){
//            while((intraLayerIndex<observedTransitions[layerIndex].length)&&(observedTransitions[layerIndex][intraLayerIndex]!=null)){  //for each observation in that layer
                tempFeatures = observedTransitions[layerIndex][intraLayerIndex].features1;  //compare this observation's features with the current top K closest
                tempValue = observedTransitions[layerIndex][intraLayerIndex].reward;
                
                challenge = true;
                nextBossIndex = actualNbNeighbors - 1;
                double tempDistance = getDistance(_features,tempFeatures);
                while(challenge){  //start with the Kth closest, then K-1, etc
                    if((nextBossIndex<0)||(tempDistance>neighborsDistances[nextBossIndex])){ //if we reach the closest neighbor or if we don't beat the current one, insert here
                        challenge = false;
                        for(int i = nbNeighbors-1; i>nextBossIndex+1; i--){
                            neighborsDistances[i] = neighborsDistances[i-1];
                            neighborsValue[i] = neighborsValue[i-1];
                        }
                        
                        if(nextBossIndex<nbNeighbors-1){
                            neighborsDistances[nextBossIndex+1] = tempDistance;
                            neighborsValue[nextBossIndex+1] = tempValue;
                        }
                        
                        if(actualNbNeighbors<nbNeighbors){
                            actualNbNeighbors+=1;
                        }
                    }
                    else{  //if closer than current challenger, then move one rank up
                        nextBossIndex -= 1;
                    }
                }
                intraLayerIndex++;
            }
            layerIndex++;
            intraLayerIndex=0;
        }
        
        int i = 0;
        while(i<actualNbNeighbors){
            result += neighborsValue[i];
            i++;
        }
        return result/(double)i;
    }
    
    double getDistance(IntDoubleHashMap[] _f1, IntDoubleHashMap[] _f2){
        double result = 0.0;
        double nbElements = 0.0;
        
        for(int i=0; i<nbFeatures; i++){
            for(Integer type : _f1[i].keySet()){
                if(_f2[i].containsKey(type)){
                    result += 0.5 * Math.pow(_f1[i].get(type) - _f2[i].get(type),2);
                    nbElements += 0.5;
                }
                else{
                    result += 1.0;
                    nbElements += 1.0;
                }
            }
            for(Integer type : _f2[i].keySet()){
                if(_f1[i].containsKey(type)){
                    result += 0.5 * Math.pow(_f1[i].get(type) - _f2[i].get(type),2);
                    nbElements += 0.5;
                }
                else{
                    result += 1.0;
                    nbElements += 1.0;
                }
            }
        }
        return Math.sqrt(result)/nbElements;
    }
    
    public void iterateOnPuzzle(){
        ArrayList<IntDoubleHashMap[]> visitedStatesFeatures = new ArrayList<IntDoubleHashMap[]>();
        ArrayList<Double> visitedStatesScores = new ArrayList<Double>();
        //initialize useful variables
        StateObservation currentState = rootObservation.copy();
        StateNode currentStateNode = rootNode;
        int currentAction = 0;
        boolean stayInTree = true;
        int depth = 0;
        IntDoubleHashMap[] features1 = new IntDoubleHashMap[nbCategories];
        IntDoubleHashMap[] features2 = new IntDoubleHashMap[nbCategories];
        double score1;
        double score2 = 0.0;
        double instantReward;

        //System.out.format("game tick is %d%n ", currentState.getGameTick());
        //loop navigating through the tree
        while (stayInTree) {
//            features1 = currentStateNode.features;
            
            score1 = getValueOfState(currentState);
            visitedStatesScores.add(0, score1);
            
            if(useValueApproximation){
                features1 = getFeaturesFromStateObs(currentState);
                visitedStatesFeatures.add(0, features1);
            }
            
            

            if (currentStateNode.notFullyExpanded()) {
                //add a new action
                int bestActionIndex = 0;
                double bestValue = -1;
                for (int i = 0; i < currentStateNode.children.length; i++) {
                    double x = randomGenerator.nextDouble();
                    if ((x > bestValue) && (currentStateNode.children[i] == null)) {
                        bestActionIndex = i;
                        bestValue = x;
                    }
                }

                StateObservation preAdvanceCopy = currentState.copy();
                preAdvanceCopy.advance(Agent.actions[bestActionIndex]);
                //test here to see if the new state has extra types per category
                
                
                currentStateNode = currentStateNode.addStateNode(preAdvanceCopy, bestActionIndex);  //creates a new action node, with its child state node - It modifies currentState!

//                features2 = currentStateNode.features;
                
                score2 = getValueOfState(preAdvanceCopy);
                
                //featureFunctions2 = getFeatureFunctionsFromFeatures(features2);
                
                if(useValueApproximation){
                    features2 = getFeaturesFromStateObs(preAdvanceCopy);
                    instantReward = score2 - score1;
                addTransition(features1, currentAction, features2, instantReward, preAdvanceCopy.isGameOver());
                updateTreeAttributes(features1);
                updateTreeAttributes(features2);
                }
                //updateWeightsUsingFeatures(features1, features2, instantReward);
                //System.out.format("%n before update, local game tick %d ",gametick1);
                //updateLinearWeightsUsingFeatures(features1, features2, instantReward);

//                if ((oldPosition.equals(newPosition)) && (!(oldOrientation.equals(newOrientation)))) { //changed orientation BUT didnt change position
//                    currentStateNode = currentStateNode.addStateNode(currentState.copy(), bestActionIndex);
//                }
                //if((getValueOfState(currentState) != initialValue)||(depth>MAX_DEPTH)) {
                stayInTree = false;
                //}
            } else {
                //select an action
                double x = randomGenerator.nextDouble();
                currentAction = currentStateNode.selectAction();
                if (x < epsilon) {
                    currentAction = currentStateNode.selectRandomAction();
                }
                currentStateNode.actionNbSimulations[currentAction] += 1;
                currentStateNode = currentStateNode.children[currentAction];

                if (false) {   //TODO: refine this condition to something that triggers calling the forward model
                    currentState.advance(Agent.actions[currentAction]); //updates the current state with the forward model
                    //currentStateNode.updateData(currentState.copy());
                } else {
                    currentState = currentStateNode.encounteredStates.get(0);
                }

//                features2 = currentStateNode.features;
                
                score2 = getValueOfState(currentState);
                //featureFunctions2 = getFeatureFunctionsFromFeatures(features2);
                
                if(useValueApproximation){
                    features2 = getFeaturesFromStateObs(currentState);
                    instantReward = score2 - score1;
                addTransition(features1, currentAction, features2, instantReward, currentState.isGameOver());
                updateTreeAttributes(features1);
                updateTreeAttributes(features2);
                }
                //System.out.format("%n before update, local game tick %d ",gametick1);
                //updateLinearWeightsUsingFeatures(features1, features2, instantReward);

                if (currentState.isGameOver()) {
                    stayInTree = false;
                }
            }
            depth++;
        }
        if(useValueApproximation){
            visitedStatesFeatures.add(0, features2);
        }
        
        visitedStatesScores.add(0, score2);

        currentStateNode.backPropagateData(currentState, visitedStatesFeatures, visitedStatesScores);
    }
}
