package com.kentnek.cdcl.algo.preprocessor;

import com.kentnek.cdcl.Logger;
import com.kentnek.cdcl.model.Assignment;
import com.kentnek.cdcl.model.Clause;
import com.kentnek.cdcl.model.Formula;
import com.kentnek.cdcl.model.Literal;

import java.util.*;

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
    public void preprocess(Formula formula, Assignment assignment) {
        Logger.log("\nOriginal clause count =", formula.getClauseSize());

        Map<Integer, LiteralType> types = new HashMap<>();

        // Resolve the polarities
        formula.forEach(c -> c.forEach(l ->
                types.compute(l.variable, (k, v) ->
                        (v == null) ? LiteralType.UNKNOWN : v.resolve(l)
                )
        ));

        Set<Literal> pureLiterals = new HashSet<>();

        // assign values to pure literals
        for (int v = 1; v <= formula.getVariableCount(); v++) {
            LiteralType type = types.get(v);
            if (type == LiteralType.POSITIVE) {
                assignment.add(v, true, Assignment.NIL);
                pureLiterals.add(new Literal(v));
            } else if (type == LiteralType.NEGATIVE) {
                assignment.add(v, false, Assignment.NIL);
                pureLiterals.add(new Literal(-v));
            }
        }

        Logger.debug("Pure literals:", pureLiterals);
        Logger.log("Found", pureLiterals.size(), "pure literals.");

        if (pureLiterals.size() == 0) return;

        // remove clauses that contain pure literals
        Iterator<Clause> iterator = formula.iterator();
        while (iterator.hasNext()) {
            if (pureLiterals.stream().anyMatch(iterator.next()::contains)) iterator.remove();
        }

        Logger.log("Preprocessed clause count =", formula.getClauseSize());
    }

}
