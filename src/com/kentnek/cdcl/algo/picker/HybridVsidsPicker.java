package com.kentnek.cdcl.algo.picker;

import com.kentnek.cdcl.model.Assignment;
import com.kentnek.cdcl.model.Clause;
import com.kentnek.cdcl.model.Formula;

/**
 * A hybrid branch picker that combines {@link RandomPicker} and {@link VsidsPicker}.
 * Randomized choices are made in {@link #randomPickingRatio} of the time, which defaults to be 10%.
 * <p>
 *
 * @author kentnek
 */

public class HybridVsidsPicker extends RandomPicker implements Formula.Listener {
    private VsidsPicker vsidsPicker = new VsidsPicker();

    // Default probability for random choices: 10%.
    private float randomPickingRatio = 0.1f;

    public HybridVsidsPicker() {
    }

    public HybridVsidsPicker(float randomPickingRatio) {
        this.randomPickingRatio = randomPickingRatio;
    }

    @Override
    public VariableValue select(Assignment assignment) {
        if (rand.nextFloat() <= randomPickingRatio) {
            return super.select(assignment);
        } else {
            return vsidsPicker.select(assignment);
        }
    }

    @Override
    public void init(Formula formula, Assignment assignment) {
        super.init(formula, assignment);
        vsidsPicker.init(formula, assignment);
    }

    @Override
    public void learn(Clause learnedClause) {
        vsidsPicker.learn(learnedClause);
    }
}
