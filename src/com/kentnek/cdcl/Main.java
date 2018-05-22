package com.kentnek.cdcl;

import com.kentnek.cdcl.algo.CdclSolver;
import com.kentnek.cdcl.algo.SatSolver;
import com.kentnek.cdcl.algo.UnsatProver;
import com.kentnek.cdcl.algo.analyzer.ClauseLearningWithUip;
import com.kentnek.cdcl.algo.picker.HybridVsidsPicker;
import com.kentnek.cdcl.algo.preprocessor.PureLiteralElimination;
import com.kentnek.cdcl.algo.propagator.TwoWatchedLiteralPropagator;
import com.kentnek.cdcl.model.Assignment;
import com.kentnek.cdcl.model.Formula;
import com.kentnek.cdcl.model.Proof;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static com.kentnek.cdcl.Metrics.Key.*;

public class Main {

    // relative to "/inputs"
    private static final String INPUT_FILE_PATH = "simple/unsat_simple_N3.cnf";

    private static boolean shouldGenerateProof;

    // This will check if the Java Debug Wire Protocol agent is used.
    private static boolean isDebugMode = java.lang.management.ManagementFactory.getRuntimeMXBean()
            .getInputArguments().toString().contains("jdwp");

    // Configs
    static {
        Logger.setShowDebug(isDebugMode);
        Metrics.setEnabled(true);
        shouldGenerateProof = true;
    }

    public static void main(String[] args) {
        Logger.log("File:", INPUT_FILE_PATH);

        Path inputPath = Paths.get("inputs", INPUT_FILE_PATH);

        Formula formula = FormulaHelper.parseFromFile(inputPath.toString());
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
                .with(new ClauseLearningWithUip())
                .withTracing(shouldGenerateProof);

        Metrics.startTimer(TOTAL);
        Assignment assignment = solver.solve(formula);
        Metrics.stopTimer(TOTAL);

        if (assignment != null) {
            if (assignment.getVariableCount() <= 100) {
                Logger.log("\nAssignment =", assignment.toMinisatString());
            } else {
                Logger.log("\nThe assignment is too big to be displayed.");
            }
        } else {
            Logger.log("\nAssignment = UNSAT");
        }

        if (Metrics.isEnabled()) {
            Logger.log("\nTotal time:", Metrics.getElapsedTimeMillis(TOTAL), "ms");
            Logger.log("Unit propagation time:", Metrics.getElapsedTimeMillis(UNIT_PROPAGATION), "ms");
            Logger.log("Branch picking invocation count:", Metrics.getCounter(BRANCH_PICKING));
            Logger.log("Branch picking time:", Metrics.getElapsedTimeMillis(BRANCH_PICKING), "ms");
            Logger.log("Conflict analysis time:", Metrics.getElapsedTimeMillis(CONFLICT_ANALYSIS), "ms");
            Logger.log("\nFinal formula size:", formula.getClauseSize());
        }

        if (assignment != null) {
            Formula originalFormula = FormulaHelper.parseFromFile(inputPath.toString());
            Logger.log("\nTest assignment:", Objects.requireNonNull(originalFormula).evaluate(assignment));
        } else if (shouldGenerateProof) {
            proveAndVerify(formula, inputPath);
        }

    }

    private static void proveAndVerify(Formula formula, Path inputPath) {
        UnsatProver prover = new UnsatProver(formula);
        Proof proof = prover.prove()
                .expandResolutions()
                .renumberClauses();

        if (proof.clauses.size() <= 20) {
            Logger.log("\n" + proof);
        } else {
            Logger.log("The refutation proof is too big to be displayed.");
        }

        boolean isProofCorrect = false;

        try {
            isProofCorrect = proof.verify();
        } catch (Exception err) {
            System.err.println(err.toString());
        }

        Logger.log(String.format(
                "The proof is verified to be %s.",
                isProofCorrect ? "CORRECT" : "WRONG"
        ));

        if (isProofCorrect) {
            String proofFilename = inputPath.getFileName().toString().replace(".cnf", ".txt");
            proof.writeToFile(proofFilename);
            Logger.log(String.format("\nProof has been written to 'proofs/%s'.", proofFilename));
        }
    }
}
