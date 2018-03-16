package com.kentnek.cdcl;

import com.kentnek.cdcl.model.Clause;
import com.kentnek.cdcl.model.Formula;
import com.kentnek.cdcl.model.Literal;

import java.io.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Helper methods to parse/generate formula.
 * <p>
 *
 * @author kentnek
 */

public class FormulaHelper {

    private static Random rand = new Random();

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


    public static void generateCnf(int variables, int literalPerClause, int clauses) {
        rand.setSeed(System.currentTimeMillis());

        String fileName = String.format(
                "N%d_K%d_L%d_%d.cnf",
                variables, literalPerClause, clauses, System.currentTimeMillis()
        );

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("inputs/generated/" + fileName))) {
            writer.write("c FILE: " + fileName + "\n");
            writer.write("c\n");
            writer.write("c Generated randomly by kentnek.\n");
            writer.write("c\n");
            writer.write(String.format("c N = %d, K = %d, L = %d.\n", variables, literalPerClause, clauses));
            writer.write("c\n");
            writer.write("c NOTE: \n");
            writer.write("c\n");

            writer.write(String.format("p cnf %d %d\n", variables, clauses));

            Set<Integer> set = new HashSet<>();

            for (int i = 0; i < clauses; i++) {
                set.clear();
                while (set.size() < literalPerClause) {
                    set.add(rand.nextInt(variables) + 1);
                }

                for (int l : set) {
                    if (rand.nextBoolean()) writer.write("-");
                    writer.write(l + " ");
                }
                writer.write("0\n");
            }

        } catch (IOException e) {
            System.out.println("Unable to write to file.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            FormulaHelper.generateCnf(50, 3, 300);
        }
    }
}
