package com.kentnek.cdcl.algo.propagator;

import com.kentnek.cdcl.Logger;
import com.kentnek.cdcl.model.*;

import java.util.*;

/**
 * An implementation of the 2-watched-literal heuristic for unit propagation.
 * <p>
 * This propagator listens to the "add" event of the {@link Assignment} and "learn" event of the {@link Formula}.
 *
 * @author kentnek
 * @see <a href="http://people.mpi-inf.mpg.de/~mfleury/sat_twl.pdf"/>
 */

public class TwoWatchedLiteralPropagator implements UnitPropagator, Assignment.Listener, Formula.Listener {

    // Stores the two literals being watched of a clause
    private class LiteralPair {
        int first;
        int second;

        // set the first or second value of this pair
        void set(int index, int value) {
            if (index == 0) first = value;
            else second = value;
        }

        // Replace the slot that contains oldValue with newValue
        void replace(int oldValue, int newValue) {
            if (first == oldValue) first = newValue;
            else if (second == oldValue) second = newValue;
        }

        @Override
        public String toString() {
            return String.format("<%d, %d>", first, second);
        }
    }

    // Map of literal -> clauses that are watching that literals
    private Map<Integer, List<Integer>> watchLists;

    // Map of clause id -> watch literals
    private Map<Integer, LiteralPair> watchedPairs;

    // Queue of literals to propagate that have recently been set to FALSE
    private LinkedList<Integer> literalsToPropagate;

    // Mark the learned clause to be analyzed in propagate()
    private Clause recentlyLearnedClause = null;

    @Override
    public void init(Formula formula, Assignment assignment) {
        watchLists = new HashMap<>();
        watchedPairs = new HashMap<>();
        literalsToPropagate = new LinkedList<>();

        // First, attempt to find and assign all unit clause.
        formula.forEach(clause -> {
            if (clause.getLiteralSize() == 1) {
                Literal unit = clause.get(0);
                assignment.add(new Assignment.SingleAssignment(
                        unit.variable, !unit.isNegated, 0, clause.getId()
                ));
            } else {
                // if not a unit clause, we find two watched literals.
                this.addClause(clause);
            }
        });

        Logger.debug("Initial watched pairs: ", watchedPairs);
    }

    /**
     * Add a clause, and make it watch the first two literals.
     */
    private void addClause(Clause clause) {
        if (clause.getLiteralSize() <= 1) return;

        int clauseId = clause.getId();

        // Get the first two literals of the clause
        for (int i = 0; i <= 1; i++) {
            int literalNum = clause.get(i).toLiteralNum();
            getWatchList(literalNum).add(clauseId);
            watchedPairs.computeIfAbsent(clauseId, k -> new LiteralPair()).set(i, literalNum);
        }
    }

    private List<Integer> getWatchList(int literal) {
        return watchLists.computeIfAbsent(literal, k -> new ArrayList<>());
    }


    // Given a clause and a watched literal, return the other watched literal
    public int findOtherWatchedLiteral(int clauseId, int literal) {
        LiteralPair watching = watchedPairs.get(clauseId);
        return literal != watching.first ? watching.first : watching.second;
    }

    @Override
    public void learn(Clause clause) {
        // a new clause is added to the formula. We'll just set the first two literals to be watched, regardless of
        // their values.
        addClause(clause);
        recentlyLearnedClause = clause;
        Logger.debug("New watched pairs after learning:", watchedPairs);
    }

    @Override
    public void add(int variable, boolean value, int antecedent) {
        // When a literal L becomes true, the solver needs to iterate only through the watch list for −L.
        // adds -L to our literal queue to be propagated later in propagate().
        literalsToPropagate.push(value ? -variable : variable);
    }

    @Override
    public boolean propagate(Formula formula, Assignment assignment) {

        // we check if there is any recently learned clause
        if (recentlyLearnedClause != null) {
            // if it's a unit clause, just assign it right away.
            if (recentlyLearnedClause.getLiteralSize() == 1) {
                Literal unitLiteral = recentlyLearnedClause.get(0);
                assignment.add(new Assignment.SingleAssignment(
                        unitLiteral.variable, !unitLiteral.isNegated, 0, recentlyLearnedClause.getId()
                ));
            } else {
                // since the newly added clause might be tracking false variables, we need to propagate them
                // as necessary
                LiteralPair pair = watchedPairs.get(recentlyLearnedClause.getId());
                if (assignment.getLiteralValue(pair.first) == Logic.FALSE) literalsToPropagate.add(pair.first);
                if (assignment.getLiteralValue(pair.second) == Logic.FALSE) literalsToPropagate.add(pair.second);
            }
            recentlyLearnedClause = null;
        }

        // Loop until our queue is empty
        while (!literalsToPropagate.isEmpty()) {
            int falseLiteral = literalsToPropagate.pop();

            if (!watchLists.containsKey(falseLiteral)) continue;
            Logger.debug("Considering falseLiteral:", falseLiteral, ", watchList =", watchLists.get(falseLiteral));

            // iterate the watch list for -L
            ListIterator<Integer> clauseCandidates = watchLists.get(falseLiteral).listIterator();

            while (clauseCandidates.hasNext()) {
                int clauseId = clauseCandidates.next();

                int otherLiteralNum = findOtherWatchedLiteral(clauseId, falseLiteral);
                Literal otherLiteral = new Literal(otherLiteralNum);
                Logic otherLiteralValue = assignment.getLiteralValue(otherLiteral);

                // 1. If the other watched literal is true, do nothing.
                if (otherLiteralValue == Logic.TRUE) continue;

                // 2. If one of the unwatched literals L is not false, restore
                // the invariant by updating the clause so that it watches L'
                // instead of −L.
                Clause currentClause = formula.getClause(clauseId);
                boolean hasUpdatedWatch = false;

                for (Literal unwatched : currentClause) {
                    int unwatchedNum = unwatched.toLiteralNum();
                    // if this literal is truly unwatched and is NOT false
                    if (unwatchedNum != falseLiteral && unwatchedNum != otherLiteralNum
                            && assignment.getLiteralValue(unwatchedNum) != Logic.FALSE) {

                        hasUpdatedWatch = true;

                        // replace falseLiteral with unwatchedNum
                        watchedPairs.get(clauseId).replace(falseLiteral, unwatchedNum);

                        Logger.debug(String.format("New pair for clause %d = %s", clauseId, watchedPairs.get(clauseId)));

                        // remove the clause from the watchList for falseLiteral
                        clauseCandidates.remove();

                        // add the clause to unwatchedNum's new watchList
                        getWatchList(unwatchedNum).add(clauseId);
                        break; // we're done for this clause
                    }
                }

                // 3. Otherwise, consider the other watched literal L' in the clause:
                if (!hasUpdatedWatch) {
                    if (otherLiteralValue == Logic.UNDEFINED) {
                        // 3.1. If it is not set, propagate L′
                        Logger.debug("Propagate:", otherLiteral);
                        assignment.add(otherLiteral.variable, !otherLiteral.isNegated, clauseId);
                    } else {
                        // 3.2. Otherwise, L' is false, and we have found a conflict.
                        Logger.debug("UCP: Conflict at clause", clauseId);
                        assignment.setKappaAntecedent(clauseId);
                        literalsToPropagate.clear();
                        return false;
                    }
                }
            }

            Logger.debug("UCP: watched pairs after considering", falseLiteral, "=", watchedPairs);
        }

        return true;
    }

}
