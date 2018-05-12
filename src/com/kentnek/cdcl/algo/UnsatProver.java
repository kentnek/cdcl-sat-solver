package com.kentnek.cdcl.algo;

import com.kentnek.cdcl.model.Clause;
import com.kentnek.cdcl.model.Formula;
import com.kentnek.cdcl.model.Proof;

import java.util.*;

/**
 * Proof generation engine for unsatisfiable assignments.
 * <p>
 *
 * @author kentnek
 */

public class UnsatProver {

    private final Formula formula;

    public UnsatProver(Formula formula) {
        this.formula = formula;
    }

    public Proof prove() {
        Clause bottomClause = formula.getBottomClause();
        assert (bottomClause != null);

        Proof proof = new Proof();
        List<Integer> neededClauses = findClausesForProof();

        // Since we might have ignore clauses in the formula that are not needed in the proof,
        // we need to remap the ids (counting from 0 onwards)
        Map<Integer, Integer> formulaToProofIdMap = new HashMap<>();

        int proofId = 0;
        for (int id : neededClauses) {
            // Clone the clause and updates its id
            Clause proofClause = formula.getClause(id).copy(proofId);
            proof.clauses.add(proofClause);

            // put (old id -> new id) in the map
            formulaToProofIdMap.put(id, proofId);

            // If this is a learned clause, append a new Resolution to the proof
            List<Integer> trace = proofClause.getTrace();
            if (trace != null) {
                proof.resolutions.add(new Proof.Resolution(
                        Proof.remapTrace(trace, formulaToProofIdMap),
                        proofId
                ));
            }

            proofId++;
        }

        return proof;
    }

    /**
     * Perform BFS from the bottom clause to find all clauses used in resolution.
     *
     * @return a sorted list of clause id needed for the proof.
     */
    private List<Integer> findClausesForProof() {
        Set<Integer> visited = new HashSet<>();

        // The bottom clause is the last one in the proof
        visited.add(formula.getBottomClause().getId());

        LinkedList<Integer> queue = new LinkedList<>();
        queue.addAll(formula.getBottomClause().getTrace());

        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (visited.contains(current)) continue;

            visited.add(current);

            Clause clause = formula.getClause(current);
            if (clause.getTrace() == null) continue;

            // if this clause has a trace, i.e. it was produced by resolution.
            // appends all the clauses in the trace to the queue
            clause.getTrace().stream()
                    .filter(x -> !visited.contains(x))
                    .forEach(queue::add);
        }

        List<Integer> list = new ArrayList<>(visited);
        Collections.sort(list);
        return list;
    }

}
