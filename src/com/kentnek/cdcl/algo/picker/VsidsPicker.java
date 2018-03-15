package com.kentnek.cdcl.algo.picker;

import com.kentnek.cdcl.model.Assignment;
import com.kentnek.cdcl.model.Clause;
import com.kentnek.cdcl.model.Formula;
import com.kentnek.cdcl.model.Literal;

import java.util.concurrent.ConcurrentSkipListMap;

/**
 * A simple implementation of the Variable State Independent Decaying Sum heuristic.
 *
 * @author kentnek
 * @see <a href="https://www.princeton.edu/~chaff/publication/DAC2001v56.pdf"/>
 * <p>
 */

public class VsidsPicker extends TrackingUnassignedVariablesPicker {

    private ConcurrentSkipListMap<Integer, Integer> scores;

    private int decayPeriod = 256;
    private float decayAmount = 0.5f;

    private int conflictCount = 0;

    @Override
    public VariableValue select(Assignment assignment) {
        for (int literalValue : scores.descendingKeySet()) {
            if (!assignment.contains(Math.abs(literalValue))) {
                return new VariableValue(Math.abs(literalValue), literalValue > 0);
            }
        }

        return null;
    }

    private void incrementCount(Literal literal) {
        scores.merge(literal.toLiteralNum(), 1, Integer::sum);
    }

    @Override
    public void init(Formula formula) {
        scores = new ConcurrentSkipListMap<>();
        formula.forEach(c -> c.forEach(this::incrementCount));
    }

    @Override
    public void learn(Clause learnedClause) {
        learnedClause.forEach(this::incrementCount);

        conflictCount = conflictCount++ % decayPeriod;
        if (conflictCount == 0) scores.replaceAll((k, v) -> (int) (v * decayAmount));
    }
}
