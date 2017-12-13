package kkunan.FeatureState;

import serialization.SerializableStateObservation;
import serialization.Types;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Mike on 11/07/2017.
 */


public class AvatarInfoState implements LearningState {
    public float avatarSpeed;
    public double[] avatarOrientation;
    //    public double[] avatarPosition;
    public Types.ACTIONS avatarLastAction;
    public int avatarType;
    public int avatarHealthPoints;
    public int avatarMaxHealthPoints;
    //    public int avatarLimitHealthPoints;
    public boolean isAvatarAlive;
    public HashMap<Integer, Integer> avatarResources;

    private static double SPEED_FILTER = 0.5;
    private static double MAX_SPEED = 5; //checked on 12 July from the framework

    private static double HEATHPOINT_FILTER = 0;

    private static int resultLength;

    //brute force, getting everything that relate to avatar from state observation, and set to state
    public AvatarInfoState(SerializableStateObservation sso)
    {
        this.avatarSpeed = sso.avatarSpeed;
        this.avatarOrientation = new double[sso.avatarOrientation.length];
        System.arraycopy(sso.avatarOrientation,0,this.avatarOrientation,0,sso.avatarOrientation.length);


        this.avatarLastAction = sso.avatarLastAction;
        this.avatarType = sso.avatarType;
        this.avatarHealthPoints = sso.avatarHealthPoints;
        this.avatarMaxHealthPoints = sso.avatarMaxHealthPoints;
        this.isAvatarAlive = sso.isAvatarAlive;
        this.avatarResources = new HashMap<>();
        avatarResources.putAll(sso.avatarResources);

        resultLength = 0;
        resultLength += 1; //speed
        resultLength += sso.avatarOrientation.length;
//        resultLength += sso.avatarPosition.length;
        resultLength += Types.ACTIONS.values().length;
        resultLength += 1; //avatar type
        resultLength += 1; //health point
        resultLength += 1; //max health point
//        resultLength += 1; //limit health point
        resultLength += 1; //isAvatarAlive

    }

    @Override
    //this is super necessary to do the equals bit, or it won't even call the method
    public int hashCode(){
        return avatarLastAction.ordinal()*10;
    }

    @Override
    public boolean equals(Object state)
    {
        //consider two states are the same when the speed is a bit different,
        //same orientation, same last action, same type, health a bit different,
        //same max health point (is this really necessary?), alive or dead, same resources
        AvatarInfoState anotherState = (AvatarInfoState)state;
        return (Math.abs(avatarSpeed - anotherState.avatarSpeed) <= SPEED_FILTER)
                && Arrays.equals(avatarOrientation,anotherState.avatarOrientation)
                && avatarLastAction.equals(anotherState.avatarLastAction)
                && avatarType == anotherState.avatarType
                && Math.abs(avatarHealthPoints - anotherState.avatarHealthPoints) <= HEATHPOINT_FILTER
                && avatarMaxHealthPoints == anotherState.avatarMaxHealthPoints
                && (isAvatarAlive == anotherState.isAvatarAlive)
                && avatarResources.equals(anotherState.avatarResources)
                ;
    }

    @Override
    //a long feature array to use in q-learning
    public double[] generatedFeatureFromState() {
        double[] result = new double[resultLength];
        try {

            result[0] = avatarSpeed;

            int i = 1;
            while (i - 1 < avatarOrientation.length) {
                result[i] = avatarOrientation[i - 1];
                i++;
            }

            result[i + avatarLastAction.ordinal()] = 1;
            i += Types.ACTIONS.values().length;
            result[i++] = avatarType;
            result[i++] = avatarHealthPoints;
            result[i++] = avatarMaxHealthPoints;
            result[i++] = isAvatarAlive ? 1 : 0;

        }catch (Exception e)
        {
            System.out.println("something wrong in genFeature");
            e.printStackTrace();
        }
        return result;
    }

}
