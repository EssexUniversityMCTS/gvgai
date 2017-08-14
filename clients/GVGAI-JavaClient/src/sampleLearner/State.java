package sampleLearner;

import serialization.Types;
import serialization.Types.ACTIONS;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Jialin Liu on 08/08/17.
 * CSEE, University of Essex, UK
 * Email: jialin.liu@essex.ac.uk
 * <p>
 * Respect to Google Java Style Guide:
 * https://google.github.io/styleguide/javaguide.html
 */
public class State {
    public static int meshSize = Agent.meshSize;
    private Cell[][] mesh_;
    private ArrayList<ACTIONS> available_actions_;
    private double value_;
    private Random rdm;
    private boolean is_start_;
    private boolean is_terminate_;

    public State () {
        new State(false, false);
    }

    public State (boolean is_start, boolean is_terminate) {
        this.mesh_ = new Cell[meshSize][meshSize]; // null 2-d array
        this.available_actions_ = new ArrayList<>();
        this.rdm = new Random();
        this.is_start_ = is_start;
        this.is_terminate_ = is_terminate;
        this.value_ = 0.0;
    }

    public Types.ACTIONS pickRandomAction() {
        int idx = rdm.nextInt(available_actions_.size());
        return available_actions_.get(idx);
    }

    @Override
    public boolean equals(Object object_to_compare) {
        // not same Class
        if (!(object_to_compare instanceof State)) {
            return false;
        }

        State state_to_compare = (State) object_to_compare;

        // check mesh
        for (int i=0; i<meshSize; i++) {
            for (int j=1; j<meshSize; j++) {
                if (mesh_[i][j] != state_to_compare.getCell(i,j)) {
                    return false;
                }
            }
        }

        // check actions
        if (available_actions_.size() != state_to_compare.getAvailableActions().size()) {
            return false;
        }
        ArrayList<ACTIONS> cloned_actions =
            (ArrayList<ACTIONS>) state_to_compare.getAvailableActions().clone();
        for (ACTIONS action: available_actions_) {
            if (!cloned_actions.remove(action)) {
                return false;
            }
        }
        if (!cloned_actions.isEmpty()) {
            return false;
        }
        return true;
    }

    public Cell[][] getMesh() {
        return mesh_;
    }

    public Cell getCell(int x, int y) {
        if (x>=meshSize || y>meshSize || x<0 || y<0){
            return null;
        }
        return mesh_[x][y];
    }

    public void setCell(int x, int y, Cell cell) {
        this.mesh_[x][y] = cell;
    }

    public void addLegalAction(ACTIONS action) {
        if (!available_actions_.contains(action)) {
            available_actions_.add(action);
        }
    }

    public void setActions(ArrayList<ACTIONS> actions) {
        this.available_actions_ = actions;
    }

    public ArrayList<ACTIONS> getAvailableActions() {
        return this.available_actions_;
    }

    public void setValue(double value) {
        this.value_ = value;
    }

    public double getValue(){
        return value_;
    }

    public ACTIONS getRandomAction() {
        return available_actions_.get(rdm.nextInt(available_actions_.size()));
    }
}
