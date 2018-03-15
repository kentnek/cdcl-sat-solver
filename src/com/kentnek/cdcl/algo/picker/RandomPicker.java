package com.kentnek.cdcl.algo.picker;

import com.kentnek.cdcl.model.Assignment;

import java.util.Random;

/**
 * This picker selects randomly one of the unassigned variables.
 * <p>
 *
 * @author kentnek
 */

public class RandomPicker extends TrackingUnassignedVariablesPicker {
    protected final Random rand = new Random();

    public RandomPicker() {
        rand.setSeed(System.currentTimeMillis());
    }

    @Override
    public VariableValue select(Assignment assignment) {
        int randomIndex = rand.nextInt(unassignedVariables.size());
        int variable = unassignedVariables.stream().skip(randomIndex).findFirst().get();
        return new VariableValue(variable, rand.nextBoolean());
    }
}
