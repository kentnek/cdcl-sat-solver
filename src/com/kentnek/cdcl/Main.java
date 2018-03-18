package com.kentnek.cdcl;

import com.kentnek.cdcl.algo.CdclSolver;
import com.kentnek.cdcl.algo.SatSolver;
import com.kentnek.cdcl.algo.analyzer.ClauseLearningWithUip;
import com.kentnek.cdcl.algo.picker.HybridVsidsPicker;
import com.kentnek.cdcl.algo.preprocessor.PureLiteralElimination;
import com.kentnek.cdcl.algo.propagator.TwoWatchedLiteralPropagator;
import com.kentnek.cdcl.model.Assignment;
import com.kentnek.cdcl.model.Formula;

import java.util.Objects;

import static com.kentnek.cdcl.Metrics.Key.*;

public class Main {

    private static final String INPUT_FILE_PATH = "inputs/others/factor_sat.cnf";

    static {
        Logger.setShowDebug(false);
    }

    public static void main(String[] args) {
        Logger.log("File:", INPUT_FILE_PATH);

        Formula formula = FormulaHelper.parseFromFile(INPUT_FILE_PATH);
        if (formula == null) return;

        if (formula.getClauseSize() <= 10) {
            Logger.log("Formula:", formula);
        } else {
            Logger.log("The formula is too big to be displayed.");
        }

        SatSolver solver = new CdclSolver()
                .with(new PureLiteralElimination())
                .with(new HybridVsidsPicker(0.1f))
                .with(new TwoWatchedLiteralPropagator())
                .with(new ClauseLearningWithUip());

        Metrics.startTimer(TOTAL);
        Assignment assignment = solver.solve(formula);
        Metrics.stopTimer(TOTAL);

        Logger.log("\nAssignment =", assignment == null ? "UNSAT" : assignment.toMinisatString());

        Logger.log("\nTotal time:", Metrics.getElapsedTimeMillis(TOTAL), "ms");
        Logger.log("Unit propagation time:", Metrics.getElapsedTimeMillis(UNIT_PROPAGATION), "ms");
        Logger.log("Branch picking invocation count:", Metrics.getCounter(BRANCH_PICKING));
        Logger.log("Branch picking time:", Metrics.getElapsedTimeMillis(BRANCH_PICKING), "ms");
        Logger.log("Conflict analysis time:", Metrics.getElapsedTimeMillis(CONFLICT_ANALYSIS), "ms");
        Logger.log("\nFinal formula size:", formula.getClauseSize());

        if (assignment != null) {
            Formula originalFormula = FormulaHelper.parseFromFile(INPUT_FILE_PATH);
            Logger.log("\nTest assignment:", Objects.requireNonNull(originalFormula).evaluate(assignment));
        }

    }
}
