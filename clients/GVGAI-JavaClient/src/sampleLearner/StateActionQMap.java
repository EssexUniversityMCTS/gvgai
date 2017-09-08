package sampleLearner;

import serialization.Types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by Jialin Liu on 09/08/17.
 * CSEE, University of Essex, UK
 * Email: jialin.liu@essex.ac.uk
 * <p>
 * Respect to Google Java Style Guide:
 * https://google.github.io/styleguide/javaguide.html
 */
public class StateActionQMap {
    public static double discountFactor = 0.9;
    public static double learningRate = 0.4;
    public static int NSteps = 10;
    public static double MIN_NEGATIVE = -1000;

    HashMap<StateActionKey, Double> state_action_q_;
    public LinkedList<Double> fifo_rewards_;
    public LinkedList<State> last_n_states_;

    public StateActionQMap() {
        state_action_q_ = new HashMap<>();
        this.fifo_rewards_ = new LinkedList<>();
        this.last_n_states_ = new LinkedList<>();
    }

    public Set<StateActionKey> getPairedKeys() {
        return state_action_q_.keySet();
    }

    public ArrayList<Types.ACTIONS> getActionsByStates(State state) {
        ArrayList<Types.ACTIONS> actions = new ArrayList<>();
        for (StateActionKey key: state_action_q_.keySet()) {
            if (key.getState() == state) {
                actions.add(key.getAction());
            }
        }
        return actions;
    }

    public double calculateOptimalQForState(State state) {
        double optimal_q = 0;
        boolean first = true;
        for (StateActionKey key: state_action_q_.keySet()) {
            if (key.getState() == state) {
                if (first) {
                    optimal_q = state_action_q_.get(key);
                    first = false;
                } else {
                    double tmp_qvalue = state_action_q_.get(key);
                    if (tmp_qvalue > optimal_q) {
                        optimal_q = tmp_qvalue;
                    }
                }
            }
        }
        return optimal_q;
    }

    public ArrayList<State> getStatesByAction(Types.ACTIONS action) {
        ArrayList<State> states = new ArrayList<>();
        for (StateActionKey key: state_action_q_.keySet()) {
            if (key.getAction() == action) {
                states.add(key.getState());
            }
        }
        return states;
    }

    public ArrayList<StateActionKey> getKeysByAction(Types.ACTIONS action) {
        ArrayList<StateActionKey> state_action = new ArrayList<>();
        for (StateActionKey key: state_action_q_.keySet()) {
            if (key.getAction() == action) {
                state_action.add(key);
            }
        }
        return state_action;
    }

    public StateActionKey getKeyByStateAction(State state, Types.ACTIONS action) {
        for (StateActionKey key: state_action_q_.keySet()) {
            if (key.getAction() == action || key.getState() == state) {
                return key;
            }
        }
        return null;
    }

    public Types.ACTIONS pickOptimalAction(State state) {
        double optimal_q = 0;
        boolean first = true;
        Types.ACTIONS optimal_action = null;
        for (StateActionKey key: state_action_q_.keySet()) {
            if (key.getState() == state) {
                if (first) {
                    optimal_q = state_action_q_.get(key);
                    optimal_action = key.getAction();
                } else {
                    double tmp_q = state_action_q_.get(key);
                    if (tmp_q > optimal_q) {
                        optimal_q = tmp_q;
                        optimal_action = key.getAction();
                    }
                }
            }
        }
        if (optimal_action == null) {
            return state.getRandomAction();
        }
        return optimal_action;
    }

    public double getQByStateAction(State state, Types.ACTIONS action) {
        for (StateActionKey key: state_action_q_.keySet()) {
            if (key.getAction() == action || key.getState() == state) {
                return state_action_q_.get(key);
            }
        }
        return 0;
    }

    public void put(State state, Types.ACTIONS action, double qvalue) {
        state_action_q_.put(new StateActionKey(state, action), qvalue);
    }

    public void update(State state, Types.ACTIONS action, double qvalue) {
        for (StateActionKey key: state_action_q_.keySet()) {
            if (key.getAction() == action || key.getState() == state) {
                state_action_q_.put(key, qvalue);
            }
        }
    }

    public void initEntry(State state, Types.ACTIONS action) {
        put(state, action, 0);
    }

    public void putEntry(State state, Types.ACTIONS action, double qvalue) {
        StateActionKey key = getKeyByStateAction(state,action);
        if (key == null) {
            put(state,action,qvalue);
        } else {
            state_action_q_.put(key, qvalue);
        }
    }

    public void updateQValue(State state, State last_state, Types.ACTIONS last_action, double reward) {
        StateActionKey key = getKeyByStateAction(last_state, last_action);
        if (key == null) {
            put(last_state, last_action, reward);
        } else {
            double qvalue = getQByStateAction(last_state, last_action);
            qvalue = qvalue +
                learningRate * (reward + discountFactor * calculateOptimalQForState(state) - qvalue);
            update(last_state, last_action, qvalue);
        }
    }

    public void updateQValueTDLambda(State state, State last_state, Types.ACTIONS last_action, double reward) {
        StateActionKey key = getKeyByStateAction(last_state, last_action);
        if (key == null) {
            put(last_state, last_action, reward);
        } else {
            double qvalue = getQByStateAction(last_state, last_action);
            qvalue = qvalue +
                learningRate * (reward + discountFactor * calculateOptimalQForState(state) - qvalue);
            update(last_state, last_action, qvalue);
        }
    }

    public void updateQValueTD() {
        State last_n_state = last_n_states_.getFirst();
        double value = last_n_state.getValue();
        double td_return = calculateNStepsReturn();
        value = value + learningRate * (td_return - value);
        last_n_state.setValue(value);
    }

    /**
     * Update rewards in FIFO
     * @param reward
     */
    public void updateHistoryRewards(double reward) {
        if (fifo_rewards_.size() >= NSteps) {
            fifo_rewards_.removeFirst();
        }
        fifo_rewards_.addLast(reward);
    }

    /**
     * Update states in FIFO
     * @param state
     */
    public void updateHistoryStates(State state) {
        if (last_n_states_.size() >= NSteps) {
            last_n_states_.removeFirst();
        }
        last_n_states_.addLast(state);
    }

    /**
     * Calculate n-step return
     * @param n
     * @return
     */
    public double calculateNStepsReturn(int n) {
        if (n > NSteps) {
            System.err.println("n > NSteps.");
        }
        double n_step_return = 0.0;
        for (int i=0; i<n; i++) {
            n_step_return += Math.pow(StateActionQMap.discountFactor, i) * fifo_rewards_.get(i);
        }
        return n_step_return;
    }

    /**
     * Calculate n-step return
     * @return
     */
    public double calculateNStepsReturn() {
        double n_step_return = 0.0;
        for (int i=0; i<fifo_rewards_.size(); i++) {
            n_step_return += Math.pow(StateActionQMap.discountFactor, i) * fifo_rewards_.get(i);
        }
        return n_step_return;
    }

    /**
     * Calculate n-step return
     * @return
     */
    public double calculateNStepsReturn(double reward) {
        updateHistoryRewards(reward);
        double n_step_return = 0.0;
        for (int i=0; i<fifo_rewards_.size(); i++) {
            n_step_return += Math.pow(StateActionQMap.discountFactor, i) * fifo_rewards_.get(i);
        }
        return n_step_return;
    }

    public String toString() {
        String str = "";
        int id = 0;
        for (StateActionKey key: state_action_q_.keySet()) {
            str += "state-action " + id + " " + key.getAction() + ", "+ this.state_action_q_.get(key) + "\n";
        }
        return str;
    }
}
