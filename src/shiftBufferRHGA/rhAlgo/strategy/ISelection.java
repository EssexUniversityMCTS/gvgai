package shiftBufferRHGA.rhAlgo.strategy;

import shiftBufferRHGA.rhAlgo.search.Individual;

/**
 * Created by dperez on 08/07/15.
 */
public interface ISelection
{
    Individual getParent(Individual[] pop, Individual first);
}