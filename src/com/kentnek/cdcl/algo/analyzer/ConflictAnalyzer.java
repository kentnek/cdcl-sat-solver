package com.kentnek.cdcl.algo.analyzer;

import com.kentnek.cdcl.model.Assignment;
import com.kentnek.cdcl.model.Clause;
import com.kentnek.cdcl.model.Formula;

/**
 * This module analyzes the most recent conflict and learns a new clause from the conflict.
 * <p>
 *
 * @author kentnek
 */

public interface ConflictAnalyzer {
    /**
     * Analyzes a conflicting {@link Assignment} of a {@link Formula} and returns a new clause to be learned.
     *
     * @param formula    the formula to be analyzed.
     * @param assignment the conflicting assignment ({@link Assignment#getKappaAntecedent()} must be not NIL).
     * @return a new {@link Clause} that has been learned from the analysis.
     */
    Clause analyze(Formula formula, Assignment assignment);
}
