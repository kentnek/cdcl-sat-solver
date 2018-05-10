package com.kentnek.cdcl.model;

import java.util.*;
import java.util.stream.Stream;

/**
 * A clause is a disjunction over a list of literals: x_1 v x_2 v ... x_n.
 * <p>
 *
 * @author kentnek
 */

public class Clause implements Iterable<Literal> {
    int id;
    private final int variableCount;
    protected final LinkedHashSet<Literal> literals;
    private List<Integer> trace; // list of clause id used in resolution to produce this clause, if it's learned.

    public Clause(int variableCount) {
        this(variableCount, new LinkedHashSet<>(), null);
    }

    public Clause(int variableCount, LinkedHashSet<Literal> literals) {
        this(variableCount, literals, null);
    }

    public Clause(int variableCount, LinkedHashSet<Literal> literals, List<Integer> trace) {
        assert (variableCount > 0);

        this.id = -1;
        this.variableCount = variableCount;
        this.literals = literals;
        this.trace = trace;
    }

    public int getId() {
        return id;
    }

    public int getVariableCount() {
        return variableCount;
    }

    public List<Integer> getTrace() {
        return trace;
    }

    public void setTrace(List<Integer> trace) {
        assert (this.trace == null); // only set once
        this.trace = trace;
    }

    public Clause copy() {
        return new Clause(this.variableCount, new LinkedHashSet<>(this.literals), this.getTrace());
    }

    public Clause copy(int newId) {
        Clause copied = copy();
        copied.id = newId;
        return copied;
    }

    //region Literal access

    public int getLiteralSize() {
        return literals.size();
    }

    public Literal get(int index) {
        return literals.stream().skip(index).findFirst().get();
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

    /**
     * Performs resolution between this and another clause.
     * <p>
     * For every variable x such that one clause contains x and the other has -x, the resulting clause contains all
     * literals off w1 and w2 with the exception of x and -x.
     *
     * @param other clause to resolve with
     * @return a new clause which is the result of the resolution between the two clauses.
     */
    public Clause resolve(Clause other) {
        Clause result = this.copy();

        for (Literal literal : other) {
            if (result.contains(literal.negate())) {
                // if w2 contains x and w1 contains -x then remove -x from the result
                result.remove(literal.negate());
            } else if (!result.contains(literal)) {
                result.add(literal);
            }
        }

        return result;
    }


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

        int count = 0;
        for (Literal literal : literals) {
            builder.append(literal);
            if (count++ < literals.size() - 1) builder.append(" v ");
        }

        builder.append(")");
        return builder.toString();
    }

    public String toSimpleString() {
        if (literals.isEmpty()) return "0";

        StringBuilder builder = new StringBuilder();

        literals.forEach(
                literal -> builder.append(literal.toLiteralNum()).append(" ")
        );

        return builder.append("0").toString().trim();
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

    public Stream<Literal> stream() {
        return this.literals.stream();
    }
}
