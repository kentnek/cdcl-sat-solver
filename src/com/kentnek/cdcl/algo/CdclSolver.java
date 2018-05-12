package com.kentnek.cdcl.algo;

import com.kentnek.cdcl.Logger;
import com.kentnek.cdcl.Metrics;
import com.kentnek.cdcl.algo.analyzer.ConflictAnalyzer;
import com.kentnek.cdcl.algo.picker.BootstrapPicker;
import com.kentnek.cdcl.algo.picker.BranchPicker;
import com.kentnek.cdcl.algo.picker.VariableValue;
import com.kentnek.cdcl.algo.preprocessor.FormulaPreprocessor;
import com.kentnek.cdcl.algo.propagator.UnitPropagator;
import com.kentnek.cdcl.model.*;

import static com.kentnek.cdcl.Metrics.Key.*;
import static com.kentnek.cdcl.model.Assignment.NIL;

/**
 * Implementation of {@link SatSolver}, using the CDCL algorithm.
 * <p>
 *
 * @author kentnek
 */

public class CdclSolver implements SatSolver {

    private FormulaPreprocessor formulaPreprocessor;
    private BranchPicker branchPicker;
    private ConflictAnalyzer conflictAnalyzer;
    private UnitPropagator unitPropagator;

    // Enables resolution tracing for refutation proof generation.
    private boolean tracing = false;

    public CdclSolver with(FormulaPreprocessor preprocessor) {
        this.formulaPreprocessor = preprocessor;
        return this;
    }

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

    public CdclSolver withTracing(boolean tracing) {
        this.tracing = tracing;
        return this;
    }

    public CdclSolver bootstrap(int... assignments) {
        assert (branchPicker != null);
        this.branchPicker = new BootstrapPicker(branchPicker, assignments);
        return this;
    }

    private void registerListener(Formula formula, Assignment assignment, GenericListener listener) {
        formula.register(listener);
        assignment.register(listener);
        listener.init(formula, assignment);
    }

    @Override
    public Assignment solve(Formula formula) {
        if (formula == null) return null;
        if (branchPicker == null || conflictAnalyzer == null || unitPropagator == null) {
            throw new IllegalArgumentException("'branchPicker', 'conflictAnalyzer' and 'unitPropagator' must be not null.");
        }

        this.conflictAnalyzer.setTracing(this.tracing);

        Assignment assignment = new Assignment(formula.getVariableCount());
        preprocessFormula(formula, assignment);

        // We use do...while loop to unit propagation once at first to detect top-level conflicts,
        // returns null assignment if there is any.
        do {
            // Loop as long as there's conflict
            while (timedUnitPropagation(formula, assignment)) {
                Clause learnedClause = timedConflictAnalysis(formula, assignment);

                int newDecisionLevel = determineDecisionLevel(assignment, learnedClause);

                // unsatisfiable, return the assignment with non-null kappa
                if (newDecisionLevel < 0) {
                    if (tracing) formula.setBottomClause(learnedClause);
                    return null;
                }

                backtrack(assignment, newDecisionLevel);

                // If the learned clause has trace of size 1, it must be the previous kappa clause,
                // so we don't need to learn it
                if (learnedClause.getTrace().size() > 1) formula.learn(learnedClause);
                Logger.debug("");
            }

            // If the assignment is complete, exit
            if (assignment.isComplete()) break;

            // When there's no more conflict, chooses a branch
            VariableValue branchVar = timedBranchPicker(assignment);
            assignment.incrementDecisionLevel();
            assignment.add(branchVar.variable, branchVar.value, NIL);

        } while (true);

        return assignment;
    }

    private void preprocessFormula(Formula formula, Assignment assignment) {
        if (this.formulaPreprocessor != null) {
            int originalCount = formula.getClauseSize();
            this.formulaPreprocessor.preprocess(formula, assignment);
            if (formula.getClauseSize() < originalCount) {
                Logger.debug("Formula after preprocessing:", formula);
                Logger.debug("Assignment after preprocessing:", assignment, "\n");
            }

        }

        registerListener(formula, assignment, branchPicker);
        registerListener(formula, assignment, unitPropagator);
    }

    private boolean timedUnitPropagation(Formula formula, Assignment assignment) {
        Metrics.startTimer(UNIT_PROPAGATION);
        boolean ret = unitPropagator.propagate(formula, assignment);
        Metrics.stopTimer(UNIT_PROPAGATION);

        if (assignment.getKappaAntecedent() > NIL) {
            Logger.debug("\nConflict! kappa =", formula.getClause(assignment.getKappaAntecedent()));
            Logger.debug("Assignment after propagation: " + assignment);
        } else {
            Logger.debug("Assignment after initial propagation:", assignment);
        }

        return ret;
    }

    private Clause timedConflictAnalysis(Formula formula, Assignment assignment) {
        Metrics.startTimer(CONFLICT_ANALYSIS);
        Clause ret = conflictAnalyzer.analyze(formula, assignment);
        Metrics.stopTimer(CONFLICT_ANALYSIS);

        Logger.debug(String.format("New learned clause %d = %s", formula.nextClauseId(), ret));
        return ret;
    }

    private VariableValue timedBranchPicker(Assignment assignment) {
        Metrics.startTimer(BRANCH_PICKING);
        VariableValue ret = branchPicker.select(assignment);
        Metrics.stopTimer(BRANCH_PICKING);
        Metrics.incrementCounter(BRANCH_PICKING);

        Logger.debug(String.format("\n======\nLevel %d, Picked: %s", assignment.getCurrentDecisionLevel() + 1, ret));
        return ret;
    }

    /**
     * Find the new decision level from the learned clause.
     *
     * @return the new decision level to backtrack to.
     */
    private int determineDecisionLevel(Assignment assignment, Clause learnedClause) {
        // Learned clause is empty, which means a contradiction
        if (learnedClause.isEmpty()) return -1;

        int conflictingLevel = assignment.getCurrentDecisionLevel();
        int newDecisionLevel = 0;

        // Find the 2nd highest decision level among the clause's literals
        // (i.e. maximum level before the conflicting level)
        for (Literal literal : learnedClause) {
            int literalLevel = assignment.getSingle(literal).decisionLevel;

            if (literalLevel < conflictingLevel) newDecisionLevel = Math.max(newDecisionLevel, literalLevel);
        }

        // Note: if all literals are on the same level with the conflicting level
        // the newDecisionLevel is 0 by convention

        return newDecisionLevel;
    }

    /**
     * Performs backtracking on a conflicting assignment.
     *
     * @param assignment the current conflicting assignment.
     */
    private void backtrack(Assignment assignment, int newDecisionLevel) {
        // Removes all existing assignments whose decision level is later than our backtrack point.
        for (Assignment.SingleAssignment single : assignment) {
            if (single.decisionLevel > newDecisionLevel) assignment.remove(single.variable);
        }

        assignment.setCurrentDecisionLevel(newDecisionLevel);
        assignment.setKappaAntecedent(NIL);

        Logger.debug("Backtrack level = " + newDecisionLevel);
        Logger.debug("Assignment after backtrack: " + assignment);
    }
}
