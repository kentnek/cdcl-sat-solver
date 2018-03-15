package com.kentnek.cdcl.algo.picker;

import com.kentnek.cdcl.model.Assignment;
import com.kentnek.cdcl.model.Clause;
import com.kentnek.cdcl.model.Formula;

/**
 * A {@link BranchPicker} selects a variable and its value to be assigned, based on the information from
 * add/remove events of an attached {@link Assignment}.
 * <p>
 *
 * @author kentnek
 */

public interface BranchPicker {
    /**
     * Choose the next variable to assign.
     *
     * @return a pair {@link VariableValue}.
     */
    VariableValue select(Assignment assignment);

    void init(Formula formula, Assignment assignment);

    default void learn(Clause learnedClause) {
    }
}
