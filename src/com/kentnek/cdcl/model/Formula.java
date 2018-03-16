package com.kentnek.cdcl.model;

import com.kentnek.cdcl.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a CNF formula, which is a conjunction over clauses: c_1 ∧ c_2 ∧ ... c_n.
 * <p>
 * This object emits one event to an attached {@link Listener}: "learn" when a new clause is added to the formula after
 * conflict analysis.
 * <p>
 *
 * @author kentnek
 */

public class Formula implements Iterable<Clause> {
    private final int variableCount;
    private final List<Clause> clauses;

    public Formula(int variableCount) {
        assert (variableCount > 0);
        this.variableCount = variableCount;
        this.clauses = new ArrayList<>();
    }

    public int getVariableCount() {
        return variableCount;
    }

    public Clause getClause(int i) {
        return clauses.get(i);
    }

    public int getClauseSize() {
        return clauses.size();
    }

    public void add(Clause clause) {
        add(clause, false);
    }

    public void add(Clause clause, boolean isLearning) {
        clause.id = clauses.size();
        clauses.add(clause);
        if (isLearning) listeners.forEach(l -> l.learn(clause));
    }

    public void remove(Clause clause) {
        clauses.remove(clause);
    }

    //region Listener

    private List<Listener> listeners = new ArrayList<>();

    public interface Listener extends GenericListener {
        void learn(Clause clause);
    }

    public void register(GenericListener listener) {
        if (listener instanceof Listener) {
            Listener casted = (Listener) listener;
            this.listeners.add(casted);
        }
    }

    //endregion

    public Logic evaluate(Assignment assignment) {
        Logic result = Logic.UNDEFINED;

        for (Clause clause : clauses) {
            Logic value = clause.evaluate(assignment);
            result = (result == Logic.UNDEFINED) ? value : result.and(value);
            if (result == Logic.FALSE || result == Logic.UNDEFINED) {
                Logger.log("false clause:", clause);
                return result;
            }
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < clauses.size(); i++) {
            builder.append(clauses.get(i).toString());
            if (i < clauses.size() - 1) builder.append(" ∧ ");
        }

        return builder.toString();
    }

    @Override
    public Iterator<Clause> iterator() {
        return this.clauses.iterator();
    }
}
