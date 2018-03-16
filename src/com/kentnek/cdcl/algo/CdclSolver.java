package com.kentnek.cdcl.algo;

import com.kentnek.cdcl.Logger;
import com.kentnek.cdcl.Metrics;
import com.kentnek.cdcl.algo.analyzer.ConflictAnalyzer;
import com.kentnek.cdcl.algo.picker.BranchPicker;
import com.kentnek.cdcl.algo.picker.VariableValue;
import com.kentnek.cdcl.algo.preprocessor.FormulaPreprocessor;
import com.kentnek.cdcl.algo.propagator.UnitPropagator;
import com.kentnek.cdcl.model.*;

import static com.kentnek.cdcl.Metrics.Key.BRANCH_PICKING;
import static com.kentnek.cdcl.Metrics.Key.CONFLICT_ANALYSIS;
import static com.kentnek.cdcl.Metrics.Key.UNIT_PROPAGATION;
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

    private void registerListener(Formula formula, Assignment assignment, GenericListener listener) {
        listener.init(formula, assignment);
        formula.register(listener);
        assignment.register(listener);
    }

    @Override
    public Assignment solve(Formula formula) {
        if (formula == null) return null;
        if (branchPicker == null || conflictAnalyzer == null || unitPropagator == null) {
            throw new IllegalArgumentException("'branchPicker', 'conflictAnalyzer' and 'unitPropagator' must be not null.");
        }

        Assignment assignment = new Assignment(formula.getVariableCount());
        formula = preprocessFormula(formula, assignment);

        // Try unit propagation once to detect top-level conflicts,
        // returns null assignment if there is any.
        if (!timedUnitPropagation(formula, assignment)) return null;

        // Loop until the assignment is complete.
        while (!assignment.isComplete()) {
            VariableValue branchVar = timedBranchPicker(assignment);

            assignment.incrementDecisionLevel();
            assignment.add(branchVar.variable, branchVar.value, NIL);

            // Loop until there's no more conflict.
            while (!timedUnitPropagation(formula, assignment)) {
                Clause learnedClause = timedConflictAnalysis(formula, assignment);

                int newDecisionLevel = backtrack(assignment, learnedClause);

                if (newDecisionLevel < 0) return null;
                formula.add(learnedClause, true);
            }
        }

        return assignment;
    }

    private Formula preprocessFormula(Formula formula, Assignment assignment) {
        if (this.formulaPreprocessor != null) {
            int originalCount = formula.getClauseSize();
            formula = this.formulaPreprocessor.preprocess(formula, assignment);
            if (formula.getClauseSize() < originalCount) {
                Logger.debug("Formula after preprocessing:", formula);
                Logger.debug("Assignment after preprocessing:", assignment, "\n");
            }

        }

        registerListener(formula, assignment, branchPicker);
        registerListener(formula, assignment, unitPropagator);

        return formula;
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

        Logger.debug("Learned clause = " + ret);
        return ret;
    }

    private VariableValue timedBranchPicker(Assignment assignment) {
        Metrics.startTimer(BRANCH_PICKING);
        VariableValue ret = branchPicker.select(assignment);
        Metrics.stopTimer(BRANCH_PICKING);
        Metrics.incrementCounter(BRANCH_PICKING);

        Logger.debug("\n======\nPicked: " + ret);
        return ret;
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

        Logger.debug("Backtrack level = " + newDecisionLevel);
        Logger.debug("Assignment after backtrack: " + assignment);

        return newDecisionLevel;
    }
}
