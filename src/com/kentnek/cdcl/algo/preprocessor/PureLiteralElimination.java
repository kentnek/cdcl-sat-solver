package com.kentnek.cdcl.algo.preprocessor;

import com.kentnek.cdcl.Logger;
import com.kentnek.cdcl.model.Assignment;
import com.kentnek.cdcl.model.Clause;
import com.kentnek.cdcl.model.Formula;
import com.kentnek.cdcl.model.Literal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This preprocessor determines the polarity of each variable in the formula, and prunes those that are pure.
 * <p>
 *
 * @author kentnek
 */

public class PureLiteralElimination implements FormulaPreprocessor {

    private enum LiteralType {
        UNKNOWN,
        POSITIVE,
        NEGATIVE,
        MIXED;

        // Given the current type of the literal, resolve the type with the next value found
        LiteralType resolve(Literal literal) {
            // unknown polarity, just set it accordingly
            if (this == UNKNOWN) return literal.isNegated ? NEGATIVE : POSITIVE;

            // same polarity
            if ((this == POSITIVE && !literal.isNegated) || (this == NEGATIVE && literal.isNegated)) return this;

            // different polarity, it's MIXED, and no need to resolve anymore.
            return MIXED;
        }
    }

    @Override
    public Formula preprocess(Formula formula, Assignment assignment) {
        Logger.log("\nOriginal clause count =", formula.getClauseSize());

        LiteralType[] types = new LiteralType[formula.getVariableCount() + 1];
        Arrays.fill(types, LiteralType.UNKNOWN);

        // Resolve the polarities
        formula.forEach(c -> c.forEach(l -> types[l.variable] = types[l.variable].resolve(l)));

        Set<Literal> pureLiterals = new HashSet<>();

        // assign values to pure literals
        for (int v = 1; v <= formula.getVariableCount(); v++) {
            LiteralType type = types[v];
            if (type == LiteralType.POSITIVE) {
                assignment.add(v, true, -1);
                pureLiterals.add(new Literal(v));
            } else if (type == LiteralType.NEGATIVE) {
                assignment.add(v, false, -1);
                pureLiterals.add(new Literal(-v));
            }
        }

        Logger.debug("Pure literals:", pureLiterals);
        Logger.log("Found", pureLiterals.size(), "pure literals.");

        if (pureLiterals.size() == 0) return formula;

        Formula preprocessedFormula = new Formula(formula.getVariableCount());

        // remove clauses that contain pure literals
        for (Clause clause : formula) {
            if (pureLiterals.stream().anyMatch(clause::contains)) continue;
            preprocessedFormula.add(clause);
        }

        Logger.log("Preprocessed clause count =", preprocessedFormula.getClauseSize());

        return preprocessedFormula;
    }

}
