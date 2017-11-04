package DontUnderestimateUchiha;

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

    // TODO: 30/06/2017 More optimize with Priority Queue, later
    // TODO: 30/06/2017 Might also be better to create a class to keep these all, or use stat summary
    HashMap<Types.ACTIONS, Double> averageIncreasedReward;
    HashMap<Types.ACTIONS, Integer> counter;
//    ArrayList<Types.ACTIONS> actionsNoEscape;
    int totalCount;

    public Agent(){
        averageIncreasedReward = new HashMap<>();
        counter = new HashMap<>();
//        actionsNoEscape = new ArrayList<>();
    }
    double prevReward;
//    Types.ACTIONS prevAction;
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
        if(averageIncreasedReward.size()==0)
        for(Types.ACTIONS action : sso.availableActions)
        {
        //    System.out.println(elapsedTimer.remainingTimeMillis());
            if(
//                    !action.equals(Types.ACTIONS.ACTION_ESCAPE) &&
                        !action.equals(Types.ACTIONS.ACTION_NIL))
            {
                averageIncreasedReward.put(action,0.0);
                counter.put(action,0);
                totalCount = 0;
//                actionsNoEscape.add(action);
        //        System.out.println(elapsedTimer.remainingTimeMillis());
            }
        }

//        prevAction = null;
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

//        System.out.println(sso.avatarLastAction);
        //first step
        if(sso.avatarLastAction.equals(Types.ACTIONS.ACTION_NIL))
        {
            Types.ACTIONS action= sso.availableActions.get(random.nextInt(sso.availableActions.size()));
            prevReward = sso.gameScore;
//            prevAction = action;
//
//            System.out.println(action);

            totalCount++;
            return action;
        }

        update(sso.gameScore,sso.avatarLastAction);

        Types.ACTIONS action;
        if(random.nextDouble() < EPSILON)
        {
            action= sso.availableActions.get(random.nextInt(sso.availableActions.size()));
        }
        else
        {
            action = ucbPick();
        }

//        prevAction = action;
//        System.out.println("PICK ACTION "+action+", EPSILON="+EPSILON);

        if(EPSILON>0.1)
            EPSILON -= 0.0001;

        totalCount++;
//        System.out.println("hello");
        return action;
    }

    public Types.ACTIONS greedyPick()
    {
        double max = -Double.MAX_VALUE;
        Types.ACTIONS maxAction = Types.ACTIONS.ACTION_ESCAPE;
        for(Types.ACTIONS action : averageIncreasedReward.keySet())
        {
            System.out.print(action+":"+averageIncreasedReward.get(action)+", "+counter.get(action)+" | ");
            if(averageIncreasedReward.get(action)>max) {
                max = averageIncreasedReward.get(action);
                maxAction = action;
            }
        }
        System.out.println("MAX ACTION: "+maxAction);

        return maxAction;
    }

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
//        double curReward = sso.gameScore;
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

        // TODO: 03/07/2017 do whatever learning here before return the next level 

        return level;
    }

}
