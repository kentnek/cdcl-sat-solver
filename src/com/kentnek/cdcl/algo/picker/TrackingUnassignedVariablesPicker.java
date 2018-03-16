package com.kentnek.cdcl.algo.picker;

import com.kentnek.cdcl.model.Assignment;
import com.kentnek.cdcl.model.Formula;

import java.util.HashSet;
import java.util.Set;

/**
 * An abstract subclass of {@link BranchPicker} that tracks unassigned variables.
 * <p>
 * This picker listens to events emitted by {@link Assignment} to keep track of the unassigned variables in a set.
 *
 * @author kentnek
 */

public abstract class TrackingUnassignedVariablesPicker implements BranchPicker, Assignment.Listener {

    protected int variableCount;
    protected final Set<Integer> unassignedVariables = new HashSet<>();

    @Override
    public void init(Formula formula, Assignment assignment) {
        variableCount = formula.getVariableCount();

        unassignedVariables.clear();
        for (int i = 1; i <= variableCount; i++) {
            if (!assignment.contains(i)) unassignedVariables.add(i);
        }
    }

    @Override
    public void add(int variable, boolean value, int antecedent) {
        unassignedVariables.remove(variable);
    }

    @Override
    public void remove(int variable, boolean value) {
        unassignedVariables.add(variable);
    }
}
