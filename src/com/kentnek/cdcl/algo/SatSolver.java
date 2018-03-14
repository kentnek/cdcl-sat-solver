package com.kentnek.cdcl.algo;

import com.kentnek.cdcl.model.Assignment;
import com.kentnek.cdcl.model.Formula;

/**
 * An abstract interface for SAT solvers.
 * <p>
 *
 * @author kentnek
 */

public interface SatSolver {
    /**
     * Solve a SAT formula.
     *
     * @param formula the formula to be solved.
     * @return an {@link Assignment} if the formula is satisfiable, or null otherwise.
     */
    Assignment solve(Formula formula);
}
