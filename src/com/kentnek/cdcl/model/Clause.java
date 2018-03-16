package com.kentnek.cdcl.model;

import java.util.*;

/**
 * A clause is a disjunction over a list of literals: x_1 v x_2 v ... x_n.
 * <p>
 *
 * @author kentnek
 */

public class Clause implements Iterable<Literal> {
    int id;
    private final int variableCount;
    private final List<Literal> literals;

    public Clause(int variableCount) {
        this(variableCount, new ArrayList<>());
    }

    public Clause(int variableCount, List<Literal> literals) {
        assert (variableCount > 0);

        this.id = -1;
        this.variableCount = variableCount;
        this.literals = literals;
    }

    public int getId() {
        return id;
    }

    public int getVariableCount() {
        return variableCount;
    }


    public Clause copy() {
        return new Clause(this.variableCount, new ArrayList<>(this.literals));
    }

    //region Literal access

    public int getLiteralSize() {
        return literals.size();
    }

    public Literal get(int index) {
        return literals.get(index);
    }

    public boolean isEmpty() {
        return this.literals.isEmpty();
    }

    public void add(Literal literal) {
        assert (literal.variable <= variableCount);
        this.literals.add(literal);
    }

    public void remove(Literal literal) {
        this.literals.remove(literal);
    }

    public boolean contains(Literal literal) {
        return this.literals.contains(literal);
    }

    //endregion


    public Logic evaluate(Assignment assignment) {
        Logic result = Logic.UNDEFINED;

        for (Literal literal : literals) {
            Logic value = assignment.getLiteralValue(literal);
            result = (result == Logic.UNDEFINED) ? value : result.or(value);
            if (result == Logic.TRUE || result == Logic.UNDEFINED) return result;
        }

        return result;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("(");

        for (int i = 0; i < literals.size(); i++) {
            builder.append(literals.get(i));
            if (i < literals.size() - 1) builder.append(" v ");
        }

        builder.append(")");
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Clause) {
            Clause other = (Clause) obj;
            if (this.literals.size() != other.literals.size()) return false;

            Set<Literal> s1 = new HashSet<>(this.literals);
            Set<Literal> s2 = new HashSet<>(other.literals);
            return s1.equals(s2);
        } else {
            return false;
        }
    }

    @Override
    public Iterator<Literal> iterator() {
        return this.literals.iterator();
    }
}
