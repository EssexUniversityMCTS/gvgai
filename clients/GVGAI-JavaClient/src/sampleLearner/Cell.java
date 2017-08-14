package sampleLearner;

import serialization.Observation;

import java.util.ArrayList;

/**
 * Created by Jialin Liu on 08/08/17.
 * CSEE, University of Essex, UK
 * Email: jialin.liu@essex.ac.uk
 * <p>
 * Respect to Google Java Style Guide:
 * https://google.github.io/styleguide/javaguide.html
 */
public class Cell {
    private ArrayList<int[]> observations_;

    public Cell() {
        this.observations_ = null;
    }

    public Cell(Observation[] observations) {
        this.observations_ = new ArrayList<>();
        for (Observation obs: observations) {
            if (obs != null) {
                this.observations_.add(new int[]{obs.category, obs.itype});
            }
        }
    }

    /**
     * Compare if two cells contain identical information
     * @param object_to_compare
     * @return
     */
    @Override
    public boolean equals(Object object_to_compare) {
        // not same Class
        if (!(object_to_compare instanceof Cell)) {
            return false;
        }

        Cell cell_to_compare = (Cell) object_to_compare;

        // not same length
        if (this.getNbObservations() != cell_to_compare.getNbObservations()) {
            return false;
        }

        // both null or empty
        if (this.getNbObservations() == 0) {
            return true;
        }

        // check if contain same sprites, though in different order
        ArrayList<int[]> cloned_list = cloneArrayList(cell_to_compare.getObservations());
        for (Object obj: observations_) {
            if (!cloned_list.remove(obj)) {
                return false;
            }
        }
        if (!cloned_list.isEmpty()) {
            return false;
        }
        return true;
    }

    public ArrayList<int[]> getObservations() {
        return observations_;
    }

    public int getNbObservations() {
        if (this.observations_ == null) {
            return 0;
        }
        return this.observations_.size();
    }

    /**
     * Clone a list of sprites
     * @param list_to_clone
     * @return
     */
    public static ArrayList<int[]> cloneArrayList(ArrayList<int[]> list_to_clone) {
        if (list_to_clone == null) {
            return null;
        }
        ArrayList<int[]> cloned_list = new ArrayList<>();
        cloned_list.addAll(list_to_clone);
        return cloned_list;
    }
}
