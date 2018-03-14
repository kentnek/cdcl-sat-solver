package com.kentnek.cdcl.algo;

import com.kentnek.cdcl.Logger;
import com.kentnek.cdcl.algo.analyzer.ConflictAnalyzer;
import com.kentnek.cdcl.algo.picker.BranchPicker;
import com.kentnek.cdcl.algo.picker.VariableValue;
import com.kentnek.cdcl.algo.propagator.UnitPropagator;
import com.kentnek.cdcl.model.Assignment;
import com.kentnek.cdcl.model.Clause;
import com.kentnek.cdcl.model.Formula;
import com.kentnek.cdcl.model.Literal;

import static com.kentnek.cdcl.model.Assignment.NIL;

/**
 * Implementation of {@link SatSolver}, using the CDCL algorithm.
 * <p>
 *
 * @author kentnek
 */

public class CdclSolver implements SatSolver {

    private BranchPicker branchPicker;
    private ConflictAnalyzer conflictAnalyzer;
    private UnitPropagator unitPropagator;

    public CdclSolver with(BranchPicker picker) {
        this.branchPicker = picker;
        return this;
    }

    public CdclSolver with(ConflictAnalyzer analyzer) {
        this.conflictAnalyzer = analyzer;
        return this;
    }

    public CdclSolver with(UnitPropagator propagator) {
        this.unitPropagator = propagator;
        return this;
    }

    @Override
    public Assignment solve(Formula formula) {
        if (formula == null) return null;
        if (branchPicker == null || conflictAnalyzer == null || unitPropagator == null) {
            throw new IllegalArgumentException("'branchPicker', 'conflictAnalyzer' and 'unitPropagator' must be not null.");
        }

        Assignment assignment = new Assignment(formula.getVariableCount());
        assignment.setListener(branchPicker);

        // Try unit propagation once to detect top-level conflicts,
        // returns null assignment if there is any.
        if (!unitPropagator.propagate(formula, assignment)) return null;

        Logger.debug("Assignment after initial propagation:", assignment.toStringFull());

        // Loop until the assignment is complete.
        while (!assignment.isComplete()) {
            VariableValue branchVar = branchPicker.select();
            Logger.debug("\nPicked: " + branchVar);

            assignment.incrementDecisionLevel();
            assignment.add(branchVar.variable, branchVar.value, NIL);

            // Loop until there's no more conflict.
            while (!unitPropagator.propagate(formula, assignment)) {
                Logger.debug("Assignment after propagation: " + assignment.toStringFull());

                Clause learnedClause = conflictAnalyzer.analyze(formula, assignment);
                Logger.debug("Learned clause = " + learnedClause);

                int newDecisionLevel = backtrack(assignment, learnedClause);
                Logger.debug("Backtrack level = " + newDecisionLevel);
                if (newDecisionLevel < 0) return null;

                formula.add(learnedClause);
                branchPicker.learn(learnedClause);

                Logger.debug("Assignment after backtrack: " + assignment.toStringFull());
            }

        }

        return assignment;
    }

    /**
     * Derives the decision level from the learned clause, and backtracks to it.
     *
     * @param assignment    the current conflicting assignment.
     * @param learnedClause the clause learned by {@link ConflictAnalyzer}.
     * @return the new decision level to backtrack to.
     */
    private int backtrack(Assignment assignment, Clause learnedClause) {
        // Learned clause is empty, which means a contradiction
        if (learnedClause.isEmpty()) return -1;

        int conflictingLevel = assignment.getCurrentDecisionLevel();
        int newDecisionLevel = 0;

        // Find the 2nd highest decision level among the clause's literals (i.e. maximum level before the
        // conflicting level)
        for (Literal literal : learnedClause) {
            int literalLevel = assignment.getSingle(literal).decisionLevel;

            if (literalLevel < conflictingLevel) newDecisionLevel = Math.max(newDecisionLevel, literalLevel);
        }

        // Removes all existing assignments whose decision level is later than our backtrack point.
        for (Assignment.SingleAssignment single : assignment) {
            if (single.decisionLevel > newDecisionLevel) assignment.remove(single.variable);
        }

        assignment.setCurrentDecisionLevel(newDecisionLevel);
        assignment.setKappaAntecedent(NIL);

        return newDecisionLevel;
    }
}
