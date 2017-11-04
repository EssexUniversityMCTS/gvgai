package kkunan.FeatureState;

import serialization.SerializableStateObservation;
import serialization.Types;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by SteepMike on 11/07/2017.
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

    public AvatarInfoState(SerializableStateObservation sso)
    {
        this.avatarSpeed = sso.avatarSpeed;
        this.avatarOrientation = new double[sso.avatarOrientation.length];
        System.arraycopy(sso.avatarOrientation,0,this.avatarOrientation,0,sso.avatarOrientation.length);

//        this.avatarPosition = new double[sso.avatarPosition.length];
//        System.arraycopy(sso.avatarPosition,0,this.avatarPosition,0,sso.avatarPosition.length);

        this.avatarLastAction = sso.avatarLastAction;
        this.avatarType = sso.avatarType;
        this.avatarHealthPoints = sso.avatarHealthPoints;
        this.avatarMaxHealthPoints = sso.avatarMaxHealthPoints;
//        this.avatarLimitHealthPoints = sso.avatarLimitHealthPoints;
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
    public int hashCode(){
        return avatarLastAction.ordinal()*10;
    }

    @Override
    public boolean equals(Object state)
    {
        AvatarInfoState anotherState = (AvatarInfoState)state;
//        System.out.println("equals call");
        return (Math.abs(avatarSpeed - anotherState.avatarSpeed) <= SPEED_FILTER)
                && Arrays.equals(avatarOrientation,anotherState.avatarOrientation)
//                && Arrays.equals(avatarPosition,anotherState.avatarPosition)
                && avatarLastAction.equals(anotherState.avatarLastAction)
                && avatarType == anotherState.avatarType
                && Math.abs(avatarHealthPoints - anotherState.avatarHealthPoints) <= HEATHPOINT_FILTER
                && avatarMaxHealthPoints == anotherState.avatarMaxHealthPoints
                && (isAvatarAlive == anotherState.isAvatarAlive)
                && avatarResources.equals(anotherState.avatarResources)

                ;
    }

    @Override
    public double[] generatedFeatureFromState() {
        double[] result = new double[resultLength];
        try {


            result[0] = avatarSpeed;

            int i = 1;
            while (i - 1 < avatarOrientation.length) {
                result[i] = avatarOrientation[i - 1];
                i++;
            }

//            int j = 0;
//            while (j < learningstate.avatarPosition.length) {
//                result[i++] = learningstate.avatarPosition[j];
//                j++;
//            }

            result[i + avatarLastAction.ordinal()] = 1;
            i += Types.ACTIONS.values().length;
            result[i++] = avatarType;
            result[i++] = avatarHealthPoints;
            result[i++] = avatarMaxHealthPoints;
//        result[i++] = learningstate.avatarLimitHealthPoints;
            result[i++] = isAvatarAlive ? 1 : 0;



//        resultLength += 1; //speed
//        resultLength += sso.avatarOrientation.length;
//        resultLength += Types.ACTIONS.values().length;
//        resultLength += 1; //avatar type
//        resultLength += 1; //health point
//        resultLength += 1; //max health point
//        resultLength += 1; //limit health point
//        resultLength += 1; //isAvatarAlive
        }catch (Exception e)
        {
            System.out.println("something wrong in genFeature");
            e.printStackTrace();
        }
        return result;
    }

}
