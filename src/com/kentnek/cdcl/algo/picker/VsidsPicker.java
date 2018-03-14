package com.kentnek.cdcl.algo.picker;

import com.kentnek.cdcl.model.Clause;
import com.kentnek.cdcl.model.Formula;

import java.util.TreeSet;

/**
 * A simple implementation of the Variable State Independent Decaying Sum heuristic.
 *
 * @author kentnek
 * @see <a href="https://www.princeton.edu/~chaff/publication/DAC2001v56.pdf"/>
 * <p>
 */

public class VsidsPicker extends TrackingUnassignedVariablesPicker {

    private TreeSet<LiteralScore> scores;

    private class LiteralScore implements Comparable<LiteralScore> {
        int literal;
        float score;

        @Override
        public int compareTo(LiteralScore other) {
            return Float.compare(this.score, other.score);
        }

        LiteralScore(int literalNum) {
            this.literal = 0;
        }

        // https://github.com/zjusbo/chaff_sat_solver/blob/master/solver.cpp
    }

    @Override
    public VariableValue select() {
        return null;
    }

    @Override
    public void init(Formula formula) {

    }

    @Override
    public void learn(Clause clause) {

    }
}
