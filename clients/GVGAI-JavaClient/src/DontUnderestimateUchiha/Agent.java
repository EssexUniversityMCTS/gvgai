package DontUnderestimateUchiha;

import serialization.SerializableStateObservation;
import serialization.Types;
import utils.ElapsedCpuTimer;

import java.util.HashMap;
import java.util.Random;

/*
Brief description of the controller
      Simple decayed e-Greedy algorithm that keep tracks of average reward changes for each action,
      and pick the best one with probability 1-epsilon
*/
public class Agent extends utils.AbstractPlayer {

    HashMap<Types.ACTIONS, Double> averageIncreasedReward;
    HashMap<Types.ACTIONS, Integer> counter;
    int totalCount;


    public Agent(){
        averageIncreasedReward = new HashMap<>();
        counter = new HashMap<>();

    }
    double prevReward;
    double EPSILON = 0.5;
    Random random = new Random();


    /**
     * Public method to be called at the start of every level of a game.
     * Perform any level-entry initialization here.
     * @param sso Phase Observation of the current game.
     * @param elapsedTimer Timer (1s)
     */
    @Override
    public void init(SerializableStateObservation sso, ElapsedCpuTimer elapsedTimer){

        prevReward = 0;

        // so you doesn't let me do this in the constructor? Fine, I'll do in the first level
        // put all non-nil available actions as keys into each mapper, to initialize
        if(averageIncreasedReward.size()==0)
            for(Types.ACTIONS action : sso.availableActions)
            {
                if(!action.equals(Types.ACTIONS.ACTION_NIL))
                {
                    averageIncreasedReward.put(action,0.0);
                    counter.put(action,0);
                    totalCount = 0;
                }
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

        //first step, or when nil was the last action, pick next action randomly
        if(sso.avatarLastAction.equals(Types.ACTIONS.ACTION_NIL))
        {
            Types.ACTIONS action= sso.availableActions.get(random.nextInt(sso.availableActions.size()));
            prevReward = sso.gameScore;

            totalCount++;
            return action;
        }

        //update hashmaps
        update(sso.gameScore,sso.avatarLastAction);

        //select next action using UCB for probability EPSILON
        Types.ACTIONS action;
        if(random.nextDouble() < EPSILON)
        {
            action= sso.availableActions.get(random.nextInt(sso.availableActions.size()));
        }
        else
        {
            action = ucbPick();
        }

        //decreasing EPSILON more when we know more about the game we are playing (really?)
        if(EPSILON>0.1)
            EPSILON -= 0.0001;

        totalCount++;
        return action;
    }

    //pure greedy without ucb, not quite good but left here for tribute
    public Types.ACTIONS greedyPick()
    {
        double max = -Double.MAX_VALUE;
        Types.ACTIONS maxAction = Types.ACTIONS.ACTION_ESCAPE;
        for(Types.ACTIONS action : averageIncreasedReward.keySet())
        {
            if(averageIncreasedReward.get(action)>max) {
                max = averageIncreasedReward.get(action);
                maxAction = action;
            }
        }

        return maxAction;
    }

    // Pick next action using UCB equation
    public Types.ACTIONS ucbPick()
    {
        double maxUCB = -Double.MAX_VALUE;
        Types.ACTIONS maxAction = Types.ACTIONS.ACTION_ESCAPE;

        for(Types.ACTIONS action : averageIncreasedReward.keySet())
        {
            double qA = averageIncreasedReward.get(action);
            int nta = counter.get(action);

            double ucbValue = qA + Math.sqrt((2*Math.log(totalCount)/nta));

            if(ucbValue>maxUCB)
            {
                maxUCB = ucbValue;
                maxAction = action;
            }
        }
        return maxAction;
    }

    private void update(double curReward, Types.ACTIONS prevAction){
        double difReward = curReward-prevReward;
        prevReward = curReward;
        int counterPrevAction = counter.get(prevAction);

        double newAverage = ((averageIncreasedReward.get(prevAction)*counterPrevAction + difReward)/(counterPrevAction+1));

        averageIncreasedReward.replace(prevAction,newAverage);
        counter.replace(prevAction,counterPrevAction+1);
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

        if(sso.gameWinner.equals(Types.WINNER.PLAYER_LOSES))
        {
            update(-100,sso.avatarLastAction);
        }

        return level;
    }

}
