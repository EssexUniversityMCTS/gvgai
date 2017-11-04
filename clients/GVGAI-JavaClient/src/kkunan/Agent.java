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

/**
 * This class has been built with a simple design in mind.
 * It is to be used to store player agent information,
 * to be later used by the client to send and receive information
 * to and from the server.
 */
public class Agent extends utils.AbstractPlayer {


    /**
     * Public method to be called at the start of the communication. No game has been initialized yet.
     * Perform one-time setup here.
     */

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

    /**
     * Public method to be called at the start of every level of a game.
     * Perform any level-entry initialization here.
     * @param sso Phase Observation of the current game.
     * @param elapsedTimer Timer (1s)
     */
    @Override
    public void init(SerializableStateObservation sso, ElapsedCpuTimer elapsedTimer){
        if(veryFirstTime)
        {
            // TODO: 05/07/2017 do whatever initialize the things
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
//        System.out.println("start "+elapsedTimer.remainingTimeMillis());
        LearningState currentState = new AvatarInfoState(sso);

        //2nd step onwards, "assume" that we have previous state stored (and should be)
        //update Q Value
        if(previousState !=null)
        {
            HashMap<Types.ACTIONS,Double> mapper = QValues.get(previousState);
            Types.ACTIONS lastAction = sso.avatarLastAction;

            if(mapper.containsKey(lastAction))
            {
                double oldQ = mapper.get(lastAction);
                double plusReward = sso.gameScore-previousReward;

                double newQ = oldQ + ALPHA*(plusReward + GAMMA*(getMaxQNext(previousState))-oldQ);
//                System.out.println(plusReward+" "+oldQ+" "+getMaxQNext(previousState)+" "+newQ);
//                System.out.println("new "+newQ+", old "+oldQ);
                mapper.replace(lastAction,newQ);
            }

            else
            {
                mapper.put(lastAction,new Double(sso.gameScore));
            }
        }

        Types.ACTIONS toActAction;
        if(QValues.containsKey(currentState))
        {

        }

        else
        {
            HashMap<Types.ACTIONS,Double> mapper = new HashMap<>();
            QValues.put(currentState,mapper);
        }

        if(random.nextDouble() > EPSILON || sso.isValidation) {
            toActAction = getMaxAction(currentState, sso.availableActions);
        }
        else toActAction = sso.availableActions.get(random.nextInt(sso.availableActions.size()));

//        System.out.println(toActAction);
//        printList(AvatarInfoState.generateFeatureFromState(currentState));
//        System.out.println(QValues.keySet().size());
        previousState = currentState;
        previousReward = sso.gameScore;

        System.out.println(sso.gameTick+": done "+elapsedTimer.elapsedMillis()+" "+elapsedTimer.remainingTimeMillis());
        return toActAction;
    }

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
//                System.out.println(maxAction+" "+mapper.get(maxAction)+", "+action+" "+mapper.get(action));

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
