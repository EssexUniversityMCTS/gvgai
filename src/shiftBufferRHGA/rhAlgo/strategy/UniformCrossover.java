package shiftBufferRHGA.rhAlgo.strategy;

import shiftBufferRHGA.rhAlgo.search.Individual;

import java.util.Random;

/**
 * Created by dperez on 08/07/15.
 */
public class UniformCrossover implements ICrossover {

    Random rnd;

    public UniformCrossover(Random rnd)
    {
        this.rnd = rnd;
    }

    @Override
    public Individual uniformCross(Individual parentA, Individual parentB) {

        int[] newInd = new int[parentA.getGenome().length];

        for(int i = 0; i < parentA.getGenome().length; ++i)
        {
            if(rnd.nextFloat() < 0.5f)
            {
                newInd[i] = parentA.getGenome()[i];
            }else{
                newInd[i] = parentB.getGenome()[i];
            }
        }

        return new Individual(newInd, parentA.playerId, parentA.evaluator);
    }

}