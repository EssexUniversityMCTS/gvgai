package sampleLearner;

import serialization.Types;

/**
 * Created by Jialin Liu on 09/08/17.
 * CSEE, University of Essex, UK
 * Email: jialin.liu@essex.ac.uk
 * <p>
 * Respect to Google Java Style Guide:
 * https://google.github.io/styleguide/javaguide.html
 */
public class StateActionKey {

    private final State state_;
    private final Types.ACTIONS action_;

    public StateActionKey(State state, Types.ACTIONS action) {
        this.state_ = state;
        this.action_ = action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StateActionKey)) return false;
        StateActionKey key = (StateActionKey) o;
        return state_ == key.state_ && action_ == key.action_;
    }

    public Types.ACTIONS getAction() {
        return action_;
    }

    public State getState() {
        return state_;
    }
}