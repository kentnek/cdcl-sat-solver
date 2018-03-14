package com.kentnek.cdcl.algo.picker;

import com.kentnek.cdcl.model.Formula;

import java.util.HashSet;
import java.util.Set;

/**
 * An abstract subclass of {@link BranchPicker} that tracks unassigned variables.
 * <p>
 *
 * @author kentnek
 */

public abstract class TrackingUnassignedVariablesPicker implements BranchPicker {

    protected int variableCount;
    protected final Set<Integer> unassignedVariables = new HashSet<>();

    @Override
    public void init(Formula formula) {
        unassignedVariables.clear();
        variableCount = formula.getVariableCount();
        for (int i = 1; i <= variableCount; i++) unassignedVariables.add(i);
    }

    @Override
    public void add(int variable, boolean value) {
        unassignedVariables.remove(variable);
    }

    @Override
    public void remove(int variable, boolean value) {
        unassignedVariables.add(variable);
    }
}
