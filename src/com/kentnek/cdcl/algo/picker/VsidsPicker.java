package com.kentnek.cdcl.algo.picker;

import com.kentnek.cdcl.Logger;
import com.kentnek.cdcl.model.Assignment;
import com.kentnek.cdcl.model.Clause;
import com.kentnek.cdcl.model.Formula;
import com.kentnek.cdcl.model.Literal;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A simple implementation of the Variable State Independent Decaying Sum (VSIDS) heuristic.
 *
 * @author kentnek
 * @see <a href="https://www.princeton.edu/~chaff/publication/DAC2001v56.pdf"/>
 */

public class VsidsPicker implements BranchPicker, Formula.Listener {
    private final Random rand = new Random();

    private LinkedHashMap<Integer, Integer> scores;

    private int learningCount = 0;
    private int decayPeriod = 256;
    private float decayAmount = 0.5f;

    /**
     * During selection, the variables that have the highest scores are most likely to be chosen.
     * This setting determines the probability that the next highest score will be chosen instead.
     */
    private float findNextHighestScoreProb = 0.3f;

    public VsidsPicker() {
        rand.setSeed(System.currentTimeMillis());
    }

    public VsidsPicker(float findNextHighestScoreProb) {
        this();
        this.findNextHighestScoreProb = findNextHighestScoreProb;
    }

    @Override
    public void init(Formula formula, Assignment assignment) {
        learningCount = 0;

        scores = new LinkedHashMap<>();

        for (int v = 1; v <= formula.getVariableCount(); v++) {
            if (assignment.contains(v)) continue;
            scores.put(v, 0);
            scores.put(-v, 0);
        }

        // Initialize the counter maps for all polarities.
        formula.forEach(c -> c.forEach(this::incrementCount));
        Logger.debug("Initial score map:", scores);
    }


    @Override
    public VariableValue select(Assignment assignment) {
        // Sort the scores from highest -> lowest.
        scores = scores.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new
                ));

        int latestSatisfyingLiteral = 0;

        for (int literalNum : scores.keySet()) {
            if (!assignment.contains(Math.abs(literalNum))) {
                // We found the unassigned variable with the highest score
                latestSatisfyingLiteral = literalNum;

                // Stop searching if the randomized value lies out of the probability range
                if (rand.nextFloat() >= findNextHighestScoreProb) break;
            }
        }

        if (latestSatisfyingLiteral != 0) {
            return new VariableValue(Math.abs(latestSatisfyingLiteral), latestSatisfyingLiteral > 0);
        } else {
            return null;
        }

    }

    @Override
    public void learn(Clause learnedClause) {
        // Increment score for each literal in the learned clause.
        learnedClause.forEach(this::incrementCount);
        Logger.debug("Current score map:", scores);

        learningCount = (learningCount + 1) % decayPeriod;

        if (learningCount == 0) { // We reach the decay period
            scores.replaceAll((k, v) -> (int) (v * decayAmount));
            Logger.debug("Score map after decay:", scores);
        }
    }

    private void incrementCount(Literal literal) {
        scores.merge(literal.toLiteralNum(), 1, Integer::sum);
    }
}
