package com.kentnek.cdcl.model;

import com.kentnek.cdcl.Logger;

import java.util.*;

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
    private final LinkedHashMap<Integer, Clause> clauses;

    // If this formula is unsatisfiable, we store the final empty clause for proof generation
    private Clause bottomClause = null;

    private int clauseId = 0;

    public Formula(int variableCount) {
        assert (variableCount > 0);
        this.variableCount = variableCount;
        this.clauses = new LinkedHashMap<>();
    }

    public int getVariableCount() {
        return variableCount;
    }

    public Clause getClause(int id) {
        return clauses.get(id);
    }

    public int getClauseSize() {
        return clauses.size();
    }

    public void add(Clause clause) {
        clause.id = clauseId++;
        clauses.put(clause.id, clause);
    }

    /**
     * Learning a clause adds it to the formula, and notifies the listeners as well.
     */
    public void learn(Clause clause) {
        add(clause);
        listeners.forEach(l -> l.learn(clause));
    }

    public void remove(Clause clause) {
        clauses.remove(clause.getId());
    }

    public Clause getBottomClause() {
        return bottomClause;
    }

    public void setBottomClause(Clause bottomClause) {
        assert (this.bottomClause == null && bottomClause.isEmpty());
        this.add(bottomClause);
        this.bottomClause = bottomClause;
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

        for (Clause clause : clauses.values()) {
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

        int i = 0;
        for (Clause clause : clauses.values()) {
            builder.append(clause.toString());
            if (i++ < clauses.size() - 1) builder.append(" ∧ ");
        }

        return builder.toString();
    }

    @Override
    public Iterator<Clause> iterator() {
        return this.clauses.values().iterator();
    }
}
