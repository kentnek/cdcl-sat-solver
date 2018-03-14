package com.kentnek.cdcl.algo.analyzer;

import com.kentnek.cdcl.model.Assignment;
import com.kentnek.cdcl.model.Clause;
import com.kentnek.cdcl.model.Formula;
import com.kentnek.cdcl.model.Literal;

import static com.kentnek.cdcl.model.Assignment.NIL;

/**
 * The clause learning heuristic, with support for Unit Implication Points (UIP).
 * <p>
 *
 * @author kentnek
 */

public class ClauseLearning implements ConflictAnalyzer {

    private boolean stopLearningAtUIP = true;

    public boolean shouldStopLearningAtUIP() {
        return stopLearningAtUIP;
    }

    public void setStopLearningAtUIP(boolean stopLearningAtUIP) {
        this.stopLearningAtUIP = stopLearningAtUIP;
    }

    @Override
    public Clause analyze(Formula formula, Assignment assignment) {
        assert (assignment.getKappaAntecedent() != NIL);

        int conflictingDecisionLevel = assignment.getCurrentDecisionLevel();

        // omega
        Clause learnedClause = formula.getClause(assignment.getKappaAntecedent()).copy();

        Clause prevLearnedClause = null;

        while (!learnedClause.equals(prevLearnedClause)) {
            // sigma = number of literals in omega assigned at current decision level
            int levelLiteralCount = 0;
            Clause clauseToResolve = null;

            for (Literal literal : learnedClause) {
                Assignment.SingleAssignment single = assignment.getSingle(literal);

                if (single.decisionLevel == conflictingDecisionLevel) {
                    levelLiteralCount++;

                    // Choose this literal to resolve with omega
                    if (clauseToResolve == null && single.antecedent != NIL) {
                        clauseToResolve = formula.getClause(single.antecedent);
                    }
                }
            }

            if ((shouldStopLearningAtUIP() && levelLiteralCount == 1 && conflictingDecisionLevel != 0) || clauseToResolve == null) {
                // Stop resolving upon UIP (only one literal in omega at conflict level) or no more literal to resolve.
                break;
            } else {
                prevLearnedClause = learnedClause;
                learnedClause = resolve(learnedClause, clauseToResolve);
            }
        }

        return learnedClause;
    }

    /**
     * Performs resolution between two clauses w1 and w2.
     * <p>
     * For every variable x such that one clause contains x and the other has -x, the resulting clause contains all
     * literals off w1 and w2 with the exception of x and -x.
     *
     * @param w1 clause w1
     * @param w2 clause w2
     * @return a new clause which is the result of the resolution between w1 and w2.
     */
    private Clause resolve(Clause w1, Clause w2) {
        Clause result = w1.copy();

        for (Literal literal : w2) {
            if (result.contains(literal.negate())) {
                // if w2 contains x and w1 contains -x then remove -x from the result
                result.remove(literal.negate());
            } else if (!result.contains(literal)) {
                result.add(literal);
            }
        }

        return result;
    }


}
