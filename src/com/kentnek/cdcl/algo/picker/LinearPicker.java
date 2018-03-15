package com.kentnek.cdcl.algo.picker;


import com.kentnek.cdcl.model.Assignment;

import java.util.Collections;

/**
 * This picker selects the smallest variable that has not been assigned.
 * <p>
 *
 * @author kentnek
 */

public class LinearPicker extends TrackingUnassignedVariablesPicker {

    @Override
    public VariableValue select(Assignment assignment) {
        if (unassignedVariables.isEmpty()) return null;
        return new VariableValue(Collections.min(unassignedVariables), true);
    }

}
