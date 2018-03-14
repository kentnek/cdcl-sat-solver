package com.kentnek.cdcl.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a CNF formula, which is a conjunction over clauses: c_1 ∧ c_2 ∧ ... c_n.
 * <p>
 *
 * @author kentnek
 */

public class Formula implements Iterable<Clause> {
    private final int variableCount;
    private final List<Clause> clauses;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < clauses.size(); i++) {
            builder.append(clauses.get(i).toString());
            if (i < clauses.size() - 1) builder.append(" ∧ ");
        }

        return builder.toString();
    }

    private Formula(int variableCount) {
        assert (variableCount > 0);
        this.variableCount = variableCount;
        this.clauses = new ArrayList<>();
    }

    public int getVariableCount() {
        return variableCount;
    }

    public Logic evaluate(Assignment assignment) {
        Logic result = Logic.UNDEFINED;

        for (Clause clause : clauses) {
            Logic value = clause.evaluate(assignment);
            result = (result == Logic.UNDEFINED) ? value : result.and(value);
            if (result == Logic.FALSE || result == Logic.UNDEFINED) return result;
        }

        return result;
    }

    public void add(Clause clause) {
        clause.id = clauses.size();
        clauses.add(clause);
    }

    public Clause getClause(int i) {
        return clauses.get(i);
    }

    public int getClauseSize() {
        return clauses.size();
    }

    public static Formula parseFromFile(String inputFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) {
            Formula formula = null;
            int variableNum = 0;
            int clauseNum = 0;
            Clause clause = null;

            for (String line; (line = br.readLine()) != null; ) {
                if (line.startsWith("c") || line.isEmpty()) continue; // Comment line

                String[] tokens = line.trim().split("\\s+");

                if (line.startsWith("p cnf")) { // literals definition line
                    variableNum = Integer.parseInt(tokens[2]);
                    clauseNum = Integer.parseInt(tokens[3]);
                    formula = new Formula(variableNum);
                    continue;
                } else if (formula == null) continue;

                if (clause == null) { // Clause line
                    clause = new Clause(variableNum);
                }

                for (String token : tokens) {
                    int literalNum = Integer.parseInt(token);

                    if (literalNum == 0) {
                        formula.add(clause);
                        clause = null;
                        break;
                    }

                    clause.add(new Literal(literalNum));
                }

            }

            if (formula != null && formula.getClauseSize() != clauseNum) {
                System.out.println(String.format(
                        "Clause number defined to be %d, but found %d instead.",
                        clauseNum, formula.getClauseSize()
                ));

                return null;
            }

            return formula;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Iterator<Clause> iterator() {
        return this.clauses.iterator();
    }
}
