package com.kentnek.cdcl;

import com.kentnek.cdcl.algo.CdclSolver;
import com.kentnek.cdcl.algo.SatSolver;
import com.kentnek.cdcl.algo.analyzer.ClauseLearning;
import com.kentnek.cdcl.algo.picker.VsidsPicker;
import com.kentnek.cdcl.algo.propagator.DefaultUnitPropagator;
import com.kentnek.cdcl.model.Assignment;
import com.kentnek.cdcl.model.Formula;

public class Main {

    private static final String INPUT_FILE_PATH = "inputs/zebra.cnf";

    static {
        Logger.setShowDebug(false);
    }

    public static void main(String[] args) {
        Logger.log("File:", INPUT_FILE_PATH);

        Formula formula = Formula.parseFromFile(INPUT_FILE_PATH);
        if (formula == null) return;

        if (formula.getClauseSize() <= 10) {
            Logger.log("Formula:", formula);
        } else {
            Logger.log("The formula is too big to be displayed.");
        }

        SatSolver solver = new CdclSolver()
                .with(new VsidsPicker())
                .with(new DefaultUnitPropagator())
                .with(new ClauseLearning());

        Assignment result = solver.solve(formula);
        Logger.log("\nResult =", result == null ? "UNSAT" : result);
    }

}
