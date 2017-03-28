package tracks.multiPlayer.deprecated.shiftBufferRHGA.rhAlgo.strategy;

import tracks.multiPlayer.deprecated.shiftBufferRHGA.rhAlgo.search.Individual;

import java.util.Random;

/**
 * Created by dperez on 08/07/15.
 */
public class TournamentSelection implements ISelection
{
    private int tournament_size;
    Random rnd;

    public TournamentSelection(Random rnd, int tSize)
    {
        tournament_size = tSize;
        this.rnd = rnd;
    }


    public Individual getParent(Individual[] pop, Individual first)
    {
        Individual best = null;
        int[] tour= new int[tournament_size];
        for(int i = 0; i < tournament_size; ++i)
            tour[i] = -1;

        int i = 0;
        while(tour[tournament_size-1] == -1)
        {
            int part = (int) (rnd.nextFloat()*pop.length);
            boolean valid = pop[part] != first;  //Check it is not the same selected first.
            for(int k = 0; valid && k < i; ++k)
            {
                valid = (part != tour[k]);                 //Check it is not in the tournament already.
            }

            if(valid)
            {
                tour[i++] = part;
                if(best == null || (pop[part].getFitness() > best.getFitness()))
                    best = pop[part];
            }
        }

        return best;
    }
}