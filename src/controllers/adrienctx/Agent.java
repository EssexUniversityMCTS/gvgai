/**
 * Code written by Adrien Couetoux, acouetoux@ulg.ac.be It is free to use,
 * distribute, and modify. User: adrienctx Date: 12/01/2015
 */
package controllers.adrienctx;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import java.util.ArrayList;
import java.util.Random;
import tools.Vector2d;

public class Agent extends AbstractPlayer {

    public static int NUM_ACTIONS;
    public static Types.ACTIONS[] actions;
    private static final int AGE_LIMIT = 0;
    private boolean gameIsPuzzle;

    /**
     * Random generator for the agent.
     */
    private TreeSearchPlayer treeSearchPlayer;

    /**
     * Public constructor with state observation and time due.
     *
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        //Get the actions in a static array.
        ArrayList<Types.ACTIONS> act = so.getAvailableActions();
        actions = new Types.ACTIONS[act.size()];
        for (int i = 0; i < actions.length; ++i) {
            actions[i] = act.get(i);
        }
        NUM_ACTIONS = actions.length;
        gameIsPuzzle = true;

        //Create the player.
        treeSearchPlayer = new TreeSearchPlayer(new Random(), so);
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     *
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        ArrayList<Observation> obs[] = stateObs.getFromAvatarSpritesPositions();
        ArrayList<Observation> grid[][] = stateObs.getObservationGrid();

        // printStateObservation(stateObs);
        // printImmovables(stateObs);
        // printMovables(stateObs);
        //printResources(stateObs);
        //printNPCs(stateObs);
        //printPortals(stateObs);
        //System.out.format("acting with avatar in position  and tick %d "+ stateObs.getAvatarPosition().toString(), stateObs.getGameTick());
        //Determine the action using MCTS...
        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int numIters = 0;
        int remainingLimit = 5;

        if (stateObs.getGameTick() > 10) {
            //Set the state observation object as the new root of the tree.
            if (gameIsPuzzle) {  //play in puzzle mode
//            if(false){
                return actOnPuzzle(stateObs, elapsedTimer);
//                int lastAction = treeSearchPlayer.lastActionPicked;
//                treeSearchPlayer.initializeWithSelectedBranch(lastAction);
//
//                while (remaining > 2 * avgTimeTaken && remaining > remainingLimit) {
//                    ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
//                    treeSearchPlayer.iterateOnPuzzle();
//
//                    numIters++;
//                    acumTimeTaken += (elapsedTimerIteration.elapsedMillis());
//                    avgTimeTaken = acumTimeTaken / numIters;
//                    remaining = elapsedTimer.remainingTimeMillis();
//                }
//                    treeSearchPlayer.lastActionPicked = treeSearchPlayer.returnBestAction();
//                    // System.out.format("age limit reached, returning action %d %n", treeSearchPlayer.lastActionPicked);
//                    // System.out.format(" points to child with avatar position : "+ treeSearchPlayer.rootNode.children[treeSearchPlayer.lastActionPicked].encounteredStates.get(0).getAvatarPosition().toString());
//                    return actions[treeSearchPlayer.lastActionPicked];

            } else {
                treeSearchPlayer.init(stateObs);

                while (remaining > 2 * avgTimeTaken && remaining > remainingLimit) {
                    ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
                    treeSearchPlayer.iterate();

                    numIters++;
                    acumTimeTaken += (elapsedTimerIteration.elapsedMillis());
                    avgTimeTaken = acumTimeTaken / numIters;
                    remaining = elapsedTimer.remainingTimeMillis();
                }

                return actions[treeSearchPlayer.returnBestAction()];
            }
        } else {
            treeSearchPlayer.init(stateObs);
            boolean _gameIsPuzzle = treeSearchPlayer.checkIfGameIsPuzzle();
//            _gameIsPuzzle = false;
            if(!_gameIsPuzzle){
                gameIsPuzzle = false;
//                System.out.println("game is not puzzle");
            }
//            if (_gameIsPuzzle) {
//                treeSearchPlayer.discountFactor = 0.99;
//            }
            Vector2d vectorNIL = new Vector2d(-1, -1);
            Types.ACTIONS result = Types.ACTIONS.fromVector(vectorNIL);
            return result;
        }

        //
        //... and return it.
        //
    }
    
    public Types.ACTIONS actOnPuzzle(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int numIters = 0;
        int remainingLimit = 5;
        
        if(treeSearchPlayer.lastActionPicked>-1)
        {
            treeSearchPlayer.initializeWithSelectedBranch(treeSearchPlayer.lastActionPicked);
        }
        else{
            treeSearchPlayer.init(stateObs);
        }

                do{
                    ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
                    treeSearchPlayer.iterateOnPuzzle();

                    numIters++;
                    acumTimeTaken += (elapsedTimerIteration.elapsedMillis());
                    avgTimeTaken = acumTimeTaken / numIters;
                    remaining = elapsedTimer.remainingTimeMillis();
                }while (remaining > 2 * avgTimeTaken && remaining > remainingLimit);
                
                treeSearchPlayer.lastActionPicked = treeSearchPlayer.returnBestAction();
                return actions[treeSearchPlayer.lastActionPicked];
    }

    public void printStateObservation(StateObservation _stateObs) {
        ArrayList<Observation>[][] obsGrid = _stateObs.getObservationGrid();

        for (int i = 0; i < obsGrid.length; i++) {
            for (int j = 0; j < obsGrid[i].length; j++) {
                for (int k = 0; k < obsGrid[i][j].size(); k++) {
                    printObservation(obsGrid[i][j].get(k));
                }
            }
        }
    }

    public void printMovables(StateObservation _stateObs) {
        ArrayList<Observation>[] movables = _stateObs.getMovablePositions();
        System.out.format("%n printing movables : %n");
        for (int i = 0; i < movables.length; i++) {
            for (int k = 0; k < movables[i].size(); k++) {
                printObservation(movables[i].get(k));
            }
        }
    }

    public void printImmovables(StateObservation _stateObs) {
        ArrayList<Observation>[] immovables = _stateObs.getImmovablePositions();
        System.out.format("%n printing immovables : %n");
        for (int i = 0; i < immovables.length; i++) {
            for (int k = 0; k < immovables[i].size(); k++) {
                printObservation(immovables[i].get(k));
            }
        }
    }

    public void printNPCs(StateObservation _stateObs) {
        ArrayList<Observation>[] immovables = _stateObs.getNPCPositions();
        System.out.format("%n printing NPCs : %n");
        for (int i = 0; i < immovables.length; i++) {
            for (int k = 0; k < immovables[i].size(); k++) {
                printObservation(immovables[i].get(k));
            }
        }
    }

    public void printResources(StateObservation _stateObs) {
        ArrayList<Observation>[] immovables = _stateObs.getResourcesPositions();
        System.out.format("%n printing resources : %n");
        for (int i = 0; i < immovables.length; i++) {
            for (int k = 0; k < immovables[i].size(); k++) {
                printObservation(immovables[i].get(k));
            }
        }
    }

    public void printPortals(StateObservation _stateObs) {
        ArrayList<Observation>[] immovables = _stateObs.getPortalsPositions();
        System.out.format("%n printing portals : %n");
        for (int i = 0; i < immovables.length; i++) {
            for (int k = 0; k < immovables[i].size(); k++) {
                printObservation(immovables[i].get(k));
            }
        }
    }

    public void printFromAvatarSprites(StateObservation _stateObs) {
        ArrayList<Observation>[] immovables = _stateObs.getFromAvatarSpritesPositions();
        System.out.format("%n printing from Avatar sprites : %n");
        for (int i = 0; i < immovables.length; i++) {
            for (int k = 0; k < immovables[i].size(); k++) {
                printObservation(immovables[i].get(k));
            }
        }
    }

    public void printObservation(Observation _obs) {

        System.out.format("cat, type, ID, pos : %d, %d, %d ,", _obs.category, _obs.itype, _obs.obsID);
        System.out.format(_obs.position.toString() + "%n");
    }
}
