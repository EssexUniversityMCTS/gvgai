package kkunan.FeatureState;

/**
 * Created by SteepMike on 14/07/2017.
 */
public interface LearningState {


    static double[] generateFeatureFromState(AvatarInfoState learningstate){
        return learningstate.generatedFeatureFromState();
    }

    double[] generatedFeatureFromState();

    @Override
    public int hashCode();

    @Override
    public boolean equals(Object state);

}
