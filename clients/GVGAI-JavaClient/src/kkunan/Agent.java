package kkunan;

/**
 * Created by Mike on 10.07.2017.
 */

import kkunan.FeatureState.AvatarInfoState;
import kkunan.FeatureState.LearningState;
import serialization.SerializableStateObservation;
import serialization.Types;
import utils.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/*
Brief description of the controller
      Simple Q-learning using 'most' of the Avatar information.
      Limit health point was not used because it was difficult to scale.
      Avatar position was not used because the screen sizes can be different, so nothing to rely on in that case.

*/
public class Agent extends utils.AbstractPlayer {

    boolean veryFirstTime = true;
    Random random;

    HashMap<LearningState, HashMap<Types.ACTIONS,Double>> QValues;
    private static double ALPHA = 0.05;
    private static double GAMMA = 0.8;
    private static double EPSILON = 0.1;
    LearningState previousState;
    double previousReward;

    public Agent(){
        QValues = new HashMap<>();
        random = new Random();
    }

    @Override
    public void init(SerializableStateObservation sso, ElapsedCpuTimer elapsedTimer){
        if(veryFirstTime)
        {
            previousState = null;
            previousReward = 0;
        }

    }

    /**
     * Method used to determine the next move to be performed by the agent.
     * This method can be used to identify the current state of the game and all
     * relevant details, then to choose the desired course of action.
     *
     * @param sso Observation of the current state of the game to be used in deciding
     *            the next action to be taken by the agent.
     * @param elapsedTimer Timer (40ms)
     * @return The action to be performed by the agent.
     */
    @Override
    public Types.ACTIONS act(SerializableStateObservation sso, ElapsedCpuTimer elapsedTimer){

        LearningState currentState = new AvatarInfoState(sso);

        //2nd step onwards, "assume" that we have previous state stored (and should be)
        //update Q Value
        if(previousState !=null)
        {
            HashMap<Types.ACTIONS,Double> mapper = QValues.get(previousState);
            Types.ACTIONS lastAction = sso.avatarLastAction;

            //update the previous values if we have one stored
            if(mapper.containsKey(lastAction))
            {
                double oldQ = mapper.get(lastAction);
                double plusReward = sso.gameScore-previousReward;

                //actual Q-learning equation
                double newQ = oldQ + ALPHA*(plusReward + GAMMA*(getMaxQNext(previousState))-oldQ);
                mapper.replace(lastAction,newQ);
            }

            //or just put game score if we haven't found this before
            else
            {
                mapper.put(lastAction,new Double(sso.gameScore));
            }
        }

        Types.ACTIONS toActAction;

        //bad style of code, but I like it
        //basically just put new key in mapper if we don't have it
        if(QValues.containsKey(currentState))
        {

        }

        else
        {
            HashMap<Types.ACTIONS,Double> mapper = new HashMap<>();
            QValues.put(currentState,mapper);
        }

        //get the best action with probability 1-EPSILON, if we're still learning, if validating, just pick the best we know
        if(random.nextDouble() > EPSILON || sso.isValidation) {
            toActAction = getMaxAction(currentState, sso.availableActions);
        }
        //otherwise pick randomly
        else toActAction = sso.availableActions.get(random.nextInt(sso.availableActions.size()));

        previousState = currentState;
        previousReward = sso.gameScore;

        return toActAction;
    }

    //just a normal find the best action that seems unnecessary long
    private Types.ACTIONS getMaxAction(LearningState state, ArrayList<Types.ACTIONS> actions) {
        int index = random.nextInt(actions.size());
        try {

            if (!QValues.containsKey(state))
                return actions.get(index);

            HashMap<Types.ACTIONS, Double> mapper = QValues.get(state);

            if (mapper.keySet().size() == 0)
                return actions.get(index);

            Types.ACTIONS maxAction = mapper.keySet().iterator().next();

            for (Types.ACTIONS action : mapper.keySet()) {
                if (mapper.get(maxAction) < mapper.get(action))
                    maxAction = action;
            }
            return maxAction;
        }catch (Exception e)
        {
            System.out.println("something wrong in getMaxAction");
            e.printStackTrace();
        }
        return actions.get(index);
    }

    //Another get max, but for the next possible states, given the current one
    private double getMaxQNext(LearningState state) {
        try {
            if (!QValues.containsKey(state))
                return 0;

            HashMap<Types.ACTIONS, Double> mapper = QValues.get(state);

            if (mapper.keySet().size() == 0)
                return 0;

            double maxAction = -Double.MAX_VALUE;

            for (Types.ACTIONS actions : mapper.keySet()) {
                if (maxAction < mapper.get(actions))
                    maxAction = mapper.get(actions);
            }

            return maxAction;
        }catch (Exception e)
        {
            System.out.println("something wrong in getMaxQNext");
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Method used to perform actions in case of a game end.
     * This is the last thing called when a level is played (the game is already in a terminal state).
     * Use this for actions such as teardown or process data.
     *
     * @param sso The current state observation of the game.
     * @param elapsedTimer Timer (up to CompetitionParameters.TOTAL_LEARNING_TIME
     * or CompetitionParameters.EXTRA_LEARNING_TIME if current global time is beyond TOTAL_LEARNING_TIME)
     * @return The next level of the current game to be played.
     * The level is bound in the range of [0,2]. If the input is any different, then the level
     * chosen will be ignored, and the game will play a random one instead.
     */
    @Override
    public int result(SerializableStateObservation sso, ElapsedCpuTimer elapsedTimer){
        Random r = new Random();
        Integer level = r.nextInt(3);

        return level;
    }

    public void printList(double[] doubles)
    {
        for(double d: doubles)
        {
            System.out.print(d+" ");
        }
        System.out.println();
    }
}
