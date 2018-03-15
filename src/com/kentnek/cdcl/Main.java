package com.kentnek.cdcl;

import com.kentnek.cdcl.algo.CdclSolver;
import com.kentnek.cdcl.algo.SatSolver;
import com.kentnek.cdcl.algo.analyzer.ClauseLearningWithUip;
import com.kentnek.cdcl.algo.picker.HybridVsidsPicker;
import com.kentnek.cdcl.algo.preprocessor.PureLiteralElimination;
import com.kentnek.cdcl.algo.propagator.DefaultUnitPropagator;
import com.kentnek.cdcl.model.Assignment;
import com.kentnek.cdcl.model.Formula;

public class Main {

    private static final String INPUT_FILE_PATH = "inputs/generated/N150_K3_L650_sat.cnf";

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
                .with(new DefaultUnitPropagator())
                .with(new ClauseLearningWithUip());

        long startTime = System.nanoTime();
        Assignment assignment = solver.solve(formula);
        long endTime = System.nanoTime();

        long duration = (endTime - startTime);

        Logger.log("\nTime:", duration / 1000000, "ms");
        Logger.log("Assignment =", assignment == null ? "UNSAT" : assignment);

        if (assignment != null) {
            Logger.log("\nTest assignment:", formula.evaluate(assignment));
        }

    }
}
