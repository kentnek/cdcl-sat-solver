package com.kentnek.cdcl.model;

import com.kentnek.cdcl.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An {@link Assignment} stores the {@link Logic} value assigned to variables in a formula.
 * <p>
 * This object emits two events to an attached {@link Listener}: "add" when a variable is assigned and "remove" when a
 * variable is unassigned.
 * <p>
 *
 * @author kentnek
 */

public class Assignment implements Iterable<Assignment.SingleAssignment> {
    public static final int NIL = -1;
    private final int variableCount;
    private int assignmentOrder;

    /**
     * Maps variables (>0) to their assigned values (true and false). This map does not store unassigned variables.
     */
    private final Map<Integer, SingleAssignment> map = new ConcurrentHashMap<>();

    private int kappaAntecedent;
    private int currentDecisionLevel;

    public static class SingleAssignment {
        public final int variable;
        public final boolean value;
        public final int decisionLevel;
        public final int antecedent;
        public final int order;

        public SingleAssignment(int variable, boolean value, int decisionLevel, int antecedent, int order) {
            this.variable = variable;
            this.value = value;
            this.decisionLevel = decisionLevel;
            this.antecedent = antecedent;
            this.order = order;
        }
    }

    public Assignment(int variableCount) {
        this.variableCount = variableCount;
        this.kappaAntecedent = NIL;
        this.currentDecisionLevel = 0;
        this.assignmentOrder = 0;
    }

    public int getVariableCount() {
        return variableCount;
    }

    public int getKappaAntecedent() {
        return kappaAntecedent;
    }

    public void setKappaAntecedent(int kappaAntecedent) {
        this.kappaAntecedent = kappaAntecedent;
    }

    public boolean isComplete() {
        return map.keySet().size() == variableCount;
    }

    //region Listener

    private List<Listener> listeners = new ArrayList<>();

    public interface Listener extends GenericListener {
        default void add(int variable, boolean value, int antecedent) {
        }

        default void remove(int variable, boolean value) {
        }
    }

    public void register(GenericListener listener) {
        if (listener instanceof Listener) {
            Listener casted = (Listener) listener;

            this.listeners.add(casted);
        }
    }

    //endregion


    //region Decision Level

    public void setCurrentDecisionLevel(int decisionLevel) {
        this.currentDecisionLevel = decisionLevel;
    }

    public int getCurrentDecisionLevel() {
        return this.currentDecisionLevel;
    }

    public void incrementDecisionLevel() {
        this.currentDecisionLevel++;
    }

    public boolean isSatisfiable() {
        return getKappaAntecedent() == NIL;
    }

    //endregion


    //region Literal access

    private void checkVariable(int variable) {
        assert (variable > 0 && variable <= variableCount);
    }

    public boolean contains(int variable) {
        checkVariable(variable);
        return map.containsKey(variable);
    }

    public void add(int variable, boolean value, int antecedent, int decisionLevel) {
        checkVariable(variable);

        SingleAssignment single = new SingleAssignment(
                variable, value, decisionLevel, antecedent, assignmentOrder
        );

        this.assignmentOrder++;

        map.put(single.variable, single);
        listeners.forEach(l -> l.add(single.variable, single.value, single.antecedent));
    }

    public void add(int variable, boolean value, int antecedent) {
        int level = this.getCurrentDecisionLevel();
        add(variable, value, antecedent, level);
    }

    public void remove(int variable) {
        checkVariable(variable);

        boolean value = map.get(variable).value;
        map.remove(variable);

        listeners.forEach(l -> l.remove(variable, value));
    }

    public Logic getLiteralValue(Literal literal) {
        int variable = literal.variable;
        checkVariable(variable);
        if (!map.containsKey(variable)) return Logic.UNDEFINED;

        Logic value = Logic.fromBoolean(map.get(variable).value);
        return literal.isNegated ? value.not() : value;
    }

    public Logic getLiteralValue(int literalNum) {
        return getLiteralValue(new Literal(literalNum));
    }

    public SingleAssignment getSingle(Literal literal) {
        return map.get(literal.variable);
    }

    //endregion

    @Override
    public Iterator<SingleAssignment> iterator() {
        return this.map.values().iterator();
    }

    @Override
    public String toString() {
        if (map.isEmpty()) return "<empty>";
        if (Logger.isDebugging()) return toStringFull();

        StringBuilder builder = new StringBuilder();

        if (getVariableCount() <= 10) {
            map.forEach((var, single) -> builder
                    .append("x").append(var)
                    .append(" -> ")
                    .append(single.value ? "T" : "F")
                    .append(", ")
            );
        } else {
            map.forEach((var, single) -> builder
                    .append(single.value ? "" : "¬")
                    .append("x").append(var)
                    .append(", ")
            );
        }


        String ret = builder.toString();
        return ret.substring(0, ret.lastIndexOf(","));
    }

    public String toMinisatString() {
        if (map.isEmpty()) return "<empty>";

        StringBuilder builder = new StringBuilder();

        map.forEach((var, single) -> builder
                .append(single.value ? "" : "-")
                .append(var)
                .append(" ")
        );

        return builder.toString();
    }

    public String toStringFull() {
        if (map.isEmpty()) return "<empty>";

        StringBuilder builder = new StringBuilder();

        map.forEach(
                (var, single) -> {
                    builder.append(single.value ? "" : "¬").append("x").append(var)
                            .append("@").append(single.decisionLevel);
                    if (single.antecedent != NIL) builder.append(" (w").append(single.antecedent).append(")");
                    builder.append(", ");
                }
        );

        String ret = builder.toString();
        return ret.substring(0, ret.lastIndexOf(","));
    }
}
