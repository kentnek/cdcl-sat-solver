package com.kentnek.cdcl.algo.picker;


/**
 * This picker selects the smallest variable that has not been assigned.
 * <p>
 *
 * @author kentnek
 */

public class LinearPicker extends TrackingUnassignedVariablesPicker {

    @Override
    public VariableValue select() {
        for (int v = 1; v <= variableCount; v++) {
            if (unassignedVariables.contains(v)) return new VariableValue(v, true);
        }

        return null;
    }

}
