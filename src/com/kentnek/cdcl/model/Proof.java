package com.kentnek.cdcl.model;

import com.kentnek.cdcl.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class contains a proof of unsatisfiability for a formula.
 * <p>
 *
 * @author kentnek
 */

public class Proof {

    public final List<Clause> clauses;
    public final List<Resolution> resolutions;


    public Proof(List<Clause> clauses, List<Resolution> resolutions) {
        this.clauses = clauses;
        this.resolutions = resolutions;
    }

    public Proof() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    /**
     * A class to represent a resolution operation on {@link #inputs} clauses to produce the {@link #output} clause.
     * <p>
     * If input = [w1, w2, ..., wn], the resolution rule is applied repeatedly from left to right: ((w1 @ w2) @ w3 ...)
     * <p>
     * inputs and output are proofIds of clauses.
     */
    public static class Resolution {
        final List<Integer> inputs;
        final Integer output;

        public Resolution(List<Integer> inputs, Integer output) {
            assert (inputs.size() >= 2); // inputs must have at least 2 clauses.
            this.inputs = inputs;
            this.output = output;
        }
    }

    private List<Integer> sortAtomicResolutionInputs(int first, int second) {
        return first < second ? Arrays.asList(first, second) : Arrays.asList(second, first);
    }

    /**
     * Expands n-resolutions so all are 2-resolutions, and generates intermediate clauses as necessary, which are added
     * to the end of {@link #clauses}.
     *
     * @return a new {@link Proof} where all resolutions are 2-resolutions.
     */
    public Proof expandResolutions() {
        List<Clause> expandedClauses = new ArrayList<>(clauses);
        List<Resolution> expandedResolutions = new ArrayList<>();

        for (Resolution resolution : resolutions) {
            List<Integer> inputs = new ArrayList<>(resolution.inputs);

            if (inputs.size() == 2) {
                List<Integer> newInput = sortAtomicResolutionInputs(inputs.get(0), inputs.get(1));
                expandedResolutions.add(new Resolution(newInput, resolution.output));
                continue;
            }

            // extract the last resolvent
            int lastId = inputs.remove(inputs.size() - 1);

            Clause current = null;
            Clause resolvent;

            int prevId = -1;

            // Scans the inputs from left to right...
            for (int id : inputs) {
                // first id -> set to current
                if (current == null) {
                    prevId = id;
                    current = expandedClauses.get(id);
                    continue;
                }

                // 2nd id onwards -> set to resolvent, then resolve with current
                resolvent = expandedClauses.get(id);
                current = current.resolve(resolvent);

                // we assign id (latest clause id) + 1 to the new clause for now
                int newClauseId = expandedClauses.size();
                expandedClauses.add(current.copy(newClauseId));

                // For 2-resolutions, we can sort the two id values.
                List<Integer> newTrace = sortAtomicResolutionInputs(prevId, id);

                // create a new resolution from clauses with prevId and id.
                expandedResolutions.add(new Resolution(newTrace, newClauseId));
                prevId = newClauseId;
            }



            // Now we chain output as the last resolution
            expandedResolutions.add(new Resolution(
                    sortAtomicResolutionInputs(prevId, lastId),
                    resolution.output
            ));
        }

        return new Proof(expandedClauses, expandedResolutions);
    }

    /**
     * Due to resolution expansion, intermediate clauses are added to the end, therefore their ids are higher than that
     * of the resolvent clauses. This function will renumber the clauses so their ids will have natural orders from
     * resolution.
     *
     * @return a new {@link Proof} where all clauses have the correct natural order.
     */
    public Proof renumberClauses() {
        List<Clause> renumberedClauses = new ArrayList<>();
        List<Resolution> renumberedResolutions = new ArrayList<>();

        Map<Integer, Integer> originalToNewIdMap = new HashMap<>();

        // initial clauses don't need to renumber
        for (Clause clause : clauses) {
            if (clause.getTrace() != null) break;
            renumberedClauses.add(clause);
        }

        int expectedId = renumberedClauses.size();
        for (Resolution resolution : resolutions) {
            int currentId = resolution.output;
            Clause currentClause = clauses.get(currentId);

            if (currentId == expectedId) {
                renumberedClauses.add(currentClause);
                renumberedResolutions.add(resolution);

            } else {
                originalToNewIdMap.put(currentId, expectedId);

                Clause newClause = currentClause.copy(expectedId);
                newClause.setTrace(remapTrace(resolution.inputs, originalToNewIdMap));
                renumberedClauses.add(newClause);

                renumberedResolutions.add(new Resolution(newClause.getTrace(), expectedId));
            }

            expectedId++;
        }

        return new Proof(renumberedClauses, renumberedResolutions);
    }

    /**
     * Given a trace [id1, id2, ...], map them to new ids using idMap.
     */
    public static List<Integer> remapTrace(List<Integer> trace, Map<Integer, Integer> idMap) {
        return trace.stream().map(x -> idMap.getOrDefault(x, x)).collect(Collectors.toList());
    }

    /**
     * Verify this proof by checking whether each resolution is correct.
     * @return true if this proof is correct, false otherwise.
     */
    public boolean verify() {
        for (Resolution resolution : resolutions) {
            List<Integer> inputs = new ArrayList<>(resolution.inputs);

            if (inputs.get(0) == -1) {
                Logger.log("ouch", resolution.inputs, resolution.output);
            }

             // extract the first clause
            Clause current = clauses.get(inputs.remove(0)).copy();

            for (int id : inputs) {
                current = current.resolve(clauses.get(id));
            }

            // if the resulting clause is different from the expected output,
            // the proof is wrong.
            if (!current.equals(clauses.get(resolution.output))) return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Proof:\n");

        for (Clause clause : clauses) {
            if (clause.getTrace() != null) break;

            builder.append("w").append(clause.id + 1).append(" ").append(clause);
            builder.append("\n");
        }

        builder.append("\nResolution:\n");

        for (Resolution resolution : resolutions) {
            int clauseId = resolution.output;
            Clause clause = clauses.get(clauseId);
            if (!clause.isEmpty()) builder.append("w").append(clauseId + 1).append(" ");

            builder.append(clause).append(" <- ")
                    .append(resolution.inputs.stream().map(s -> "w" + (s + 1)).collect(Collectors.toList()))
                    .append("\n");
        }

        return builder.toString();
    }

    public void writeToFile(String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("proofs/" + fileName))) {
            writer.write(String.format("# Proof for '%s':\n", fileName));

            writer.write(String.format("v %d\n\n", clauses.size()));

            boolean separated = false;
            for (Clause clause : clauses) {
                if (!separated && clause.getTrace() != null) {
                    separated = true;
                    writer.write("\n");
                }

                writer.write(String.format("%d %s\n", clause.id + 1, clause.toSimpleString()));
            }

            writer.write("\n# Resolutions:\n");

            for (Resolution resolution : resolutions) {
                for (int id : resolution.inputs) {
                    writer.write(id + 1 + " ");
                }
                writer.write(resolution.output + 1 + "\n");
            }
        } catch (IOException e) {
            System.out.println("Unable to write to file.");
            e.printStackTrace();
        }
    }

}
