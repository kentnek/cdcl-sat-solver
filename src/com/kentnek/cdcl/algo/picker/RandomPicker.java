package com.kentnek.cdcl.algo.picker;

import java.util.Random;

/**
 * This picker selects randomly one of the unassigned variables.
 * <p>
 *
 * @author kentnek
 */

public class RandomPicker extends TrackingUnassignedVariablesPicker {
    private final Random rand = new Random();

    @Override
    public VariableValue select() {
        int randomIndex = rand.nextInt(unassignedVariables.size());
        int variable = unassignedVariables.stream().skip(randomIndex).findFirst().get();
        return new VariableValue(variable, true);
    }
}
