package com.kentnek.cdcl.algo.propagator;

import com.kentnek.cdcl.model.*;

/**
 * Default implementation of {@link UnitPropagator}, that scans all the literals during propagation.
 * <p>
 *
 * @author kentnek
 */

public class NaiveUnitPropagator implements UnitPropagator {

    @Override
    public boolean propagate(Formula formula, Assignment assignment) {
        boolean hasUnitClause;

        do {
            hasUnitClause = false;

            clauseLoop:
            for (Clause clause : formula) {
                int undefinedCount = 0;
                Literal unitLiteral = null;

                for (Literal literal : clause) {
                    Logic value = assignment.getLiteralValue(literal);

                    // This clause is already true, we move on to next clause
                    if (value == Logic.TRUE) continue clauseLoop;

                    if (value == Logic.UNDEFINED) {
                        unitLiteral = literal;
                        undefinedCount++;

                        // This clause has more than one undefined literals, also move on to next clause
                        if (undefinedCount > 1) continue clauseLoop;
                    }
                }

                // all literals are false, which is a conflict
                if (undefinedCount == 0) {
                    assignment.setKappaAntecedent(clause.getId());
                    return false;
                } else if (undefinedCount == 1) {
                    hasUnitClause = true;
                    assignment.add(unitLiteral.variable, !unitLiteral.isNegated, clause.getId());
                }
            }

            // Loop until there is no more unit clause left
        } while (hasUnitClause);

        return true;
    }
}
