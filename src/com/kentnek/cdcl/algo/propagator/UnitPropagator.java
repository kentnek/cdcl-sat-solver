package com.kentnek.cdcl.algo.propagator;

import com.kentnek.cdcl.model.Assignment;
import com.kentnek.cdcl.model.Formula;
import com.kentnek.cdcl.model.GenericListener;

/**
 * Unit propagation module.
 * <p>
 *
 * @author kentnek
 */

public interface UnitPropagator extends GenericListener{

    /**
     * Performs unit propagation and assigns values to unit literals if any.
     *
     * @param formula    the formula to perform unit propagation on.
     * @param assignment the current assignment for the formula.
     * @return true if a conflict is found, false otherwise.
     */
    boolean propagate(Formula formula, Assignment assignment);
}
