package com.kentnek.cdcl.algo.picker;

import com.kentnek.cdcl.model.Assignment;
import com.kentnek.cdcl.model.Clause;
import com.kentnek.cdcl.model.Formula;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Wraps around another picker, and bootstraps with a predefined set of assignments.
 * <p>
 *
 * @author kentnek
 */

public class BootstrapPicker implements BranchPicker, Assignment.Listener, Formula.Listener {

    private BranchPicker picker;
    private final List<Integer> bootstrap;

    public BootstrapPicker(BranchPicker picker, int... bootstrap) {
        this.picker = picker;
        this.bootstrap = IntStream.of(bootstrap).boxed().collect(Collectors.toList());
    }


    @Override
    public void init(Formula formula, Assignment assignment) {
        picker.init(formula, assignment);
    }

    @Override
    public VariableValue select(Assignment assignment) {
        while (!this.bootstrap.isEmpty() && assignment.contains(Math.abs(bootstrap.get(0)))) {
            bootstrap.remove(0);
        }

        if (this.bootstrap.isEmpty()) {
            return picker.select(assignment);
        } else {
            int literal = bootstrap.remove(0);
            return new VariableValue(Math.abs(literal), literal > 0);
        }
    }

    @Override
    public void add(int variable, boolean value, int antecedent) {
        if (picker instanceof Assignment.Listener) {
            ((Assignment.Listener) picker).add(variable, value, antecedent);
        }
    }

    @Override
    public void remove(int variable, boolean value) {
        if (picker instanceof Assignment.Listener) {
            ((Assignment.Listener) picker).remove(variable, value);
        }
    }

    @Override
    public void learn(Clause clause) {
        if (picker instanceof Formula.Listener) {
            ((Formula.Listener) picker).learn(clause);
        }
    }
}
