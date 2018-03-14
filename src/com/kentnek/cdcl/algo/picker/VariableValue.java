package com.kentnek.cdcl.algo.picker;

/**
 * Represents a pair of variable and its value.
 * <p>
 *
 * @author kentnek
 */

public class VariableValue {
    public final int variable;
    public final boolean value;

    public VariableValue(int variable, boolean value) {
        assert (variable > 0);
        this.variable = variable;
        this.value = value;
    }

    @Override
    public String toString() {
        return "x" + String.valueOf(variable) + " -> " + String.valueOf(value);
    }
}
