package com.kentnek.cdcl.algo.analyzer;

import com.kentnek.cdcl.Loggable;
import com.kentnek.cdcl.Logger;
import com.kentnek.cdcl.model.Assignment;
import com.kentnek.cdcl.model.Clause;
import com.kentnek.cdcl.model.Formula;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.kentnek.cdcl.model.Assignment.NIL;

/**
 * The clause learning heuristic, with support for Unit Implication Points (UIP).
 * <p>
 *
 * @author kentnek
 */

public class ClauseLearningWithUip extends Loggable implements ConflictAnalyzer {

    private boolean stopLearningAtUIP = true;
    private boolean tracing = false;

    public ClauseLearningWithUip() {
    }

    public ClauseLearningWithUip(boolean stopLearningAtUIP) {
        this.stopLearningAtUIP = stopLearningAtUIP;
    }

    @Override
    public ClauseLearningWithUip debug() {
        return (ClauseLearningWithUip) super.debug();
    }

    @Override
    public void setTracing(boolean tracing) {
        this.tracing = tracing;
    }

    @Override
    public Clause analyze(Formula formula, Assignment assignment) {
        assert (assignment.getKappaAntecedent() != NIL);

        int conflictingDecisionLevel = assignment.getCurrentDecisionLevel();

        // first, set omega to clause at kappa
        Clause learnedClause = formula.getClause(assignment.getKappaAntecedent()).copy();

        // analysis stops when omega is constant
        Clause prevLearnedClause = null;

        // we also need to keep track of clauses used in resolution to produce the final learned clause
        List<Integer> trace = new ArrayList<>();
        if (tracing) trace.add(assignment.getKappaAntecedent());

        while (!learnedClause.equals(prevLearnedClause)) {
            // sigma = number of literals in omega assigned at current decision level
            int levelLiteralCount = 0;

            Clause clauseToResolve = null;

            // Sorts by descending order
            List<Assignment.SingleAssignment> sortedSingles = learnedClause.stream()
                    .map(assignment::getSingle)
                    .sorted((s1, s2) -> Integer.compare(s2.order, s1.order))
                    .collect(Collectors.toList());

            // for all literals in the learned clause
            for (Assignment.SingleAssignment single : sortedSingles) {
                // the the assignment params for this literal

                // if we're on the same decision level...
                if (single.decisionLevel == conflictingDecisionLevel) {
                    levelLiteralCount++;

                    int antecedent = single.antecedent;

                    // ...choose this literal's antecedent to resolve with omega
                    if (clauseToResolve == null && antecedent != NIL) {
                        clauseToResolve = formula.getClause(antecedent);
                    }
                }
            }

            // if there's only one literal in omega at conflict level (UIP)
            // or no more literal to resolve
            if ((stopLearningAtUIP && levelLiteralCount == 1 && conflictingDecisionLevel != 0) || clauseToResolve == null) {
                // ... stop resolving
                break;
            } else {
                prevLearnedClause = learnedClause;
                learnedClause = learnedClause.resolve(clauseToResolve);
                if (tracing) trace.add(clauseToResolve.getId());
                if (debug) {
                    Logger.debug(String.format(
                            ">> Resolving with clause %d %s => %s",
                            clauseToResolve.getId(), clauseToResolve, learnedClause
                    ));
                }
            }
        }

        if (tracing) learnedClause.setTrace(trace);
        return learnedClause;
    }
}
