package controllers.Return42.hashing;

import core.game.Observation;

import java.util.List;

/**
 * Unility class for "hashing" a bunch of observations (npc, movable, ...)
 */
public class ObservationHasher {

    /**
     * Creates a hash for a bunch of oberstions.
     * Will only consider the positions of the objects.
     * Other properties are not included.
     */
    public static int hash(List<Observation>[] observations) {
        if (observations == null)
            return 0;

        int sum = 0;
        for (List<Observation> observationsOfSameType : observations) {
            for (Observation anObservations : observationsOfSameType) {
                sum += hash( anObservations );
            }
        }

        return sum;
    }

    /**
     * "Hashes" a single observation.
     * Will only consider it's position, nothing else.
     */
    private static int hash( Observation anObservation ) {
        return (int) (anObservation.obsID * (anObservation.position.x + anObservation.position.y));
    }
}