package com.kentnek.cdcl.model;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An {@link Assignment} stores the {@link Logic} value assigned to variables in a formula.
 * <p>
 * Created on Mar 12, 2018.
 *
 * @author itaqm
 */

public class Assignment implements Iterable<Assignment.SingleAssignment> {
    public static final int NIL = -1;
    private final int variableCount;

    /**
     * Maps variables (>0) to their assigned values (true and false).
     * This map does not store unassigned variables.
     */
    private final Map<Integer, SingleAssignment> map = new ConcurrentHashMap<>();

    private int kappaAntecedent;
    private int currentDecisionLevel;

    public static class SingleAssignment {
        public final int variable;
        public final boolean value;
        public final int decisionLevel;
        public final int antecedent;

        public SingleAssignment(int variable, boolean value, int decisionLevel, int antecedent) {
            this.variable = variable;
            this.value = value;
            this.decisionLevel = decisionLevel;
            this.antecedent = antecedent;
        }
    }

    public Assignment(int variableCount) {
        this.variableCount = variableCount;
        this.kappaAntecedent = NIL;
        this.currentDecisionLevel = 0;
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

    private Listener listener = null;

    public interface Listener {

        void add(int variable, boolean value);

        void remove(int variable, boolean value);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
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

    //endregion


    //region Literal access

    private void checkVariable(int variable) {
        assert (variable > 0 && variable <= variableCount);
    }

    public boolean contains(int variable) {
        checkVariable(variable);
        return map.containsKey(variable);
    }

    public void add(int variable, boolean value, int antecedent) {
        add(new SingleAssignment(variable, value, this.getCurrentDecisionLevel(), antecedent));
    }

    public void add(SingleAssignment single) {
        checkVariable(single.variable);

        map.put(single.variable, single);

        if (listener != null) listener.add(single.variable, single.value);
    }

    public void remove(int variable) {
        checkVariable(variable);

        boolean value = map.get(variable).value;
        map.remove(variable);

        if (listener != null) listener.remove(variable, value);
    }

    public Logic getLiteralValue(Literal literal) {
        int variable = literal.variable;
        checkVariable(variable);
        if (!map.containsKey(variable)) return Logic.UNDEFINED;

        Logic value = Logic.fromBoolean(map.get(variable).value);
        return literal.isNegated ? value.not() : value;
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

        StringBuilder builder = new StringBuilder();

        map.forEach((var, single) -> builder
                .append("x").append(var)
                .append(" -> ")
                .append(single.value ? "T" : "F")
                .append(", ")
        );

        String ret = builder.toString();
        return ret.substring(0, ret.lastIndexOf(","));
    }

    public String toStringFull() {
        if (map.isEmpty()) return "<empty>";

        StringBuilder builder = new StringBuilder();

        map.forEach(
                (var, single) -> {
                    builder.append(single.value ? "" : "¬").append("x").append(var)
                            .append("@").append(single.decisionLevel);
                    if (single.antecedent != NIL) builder.append(" (w").append(single.antecedent + 1).append(")");
                    builder.append(", ");
                }
        );

        String ret = builder.toString();
        return ret.substring(0, ret.lastIndexOf(","));
    }
}
