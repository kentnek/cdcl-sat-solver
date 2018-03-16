package com.kentnek.cdcl.model;

/**
 * [Description]
 * <p>
 *
 * @author kentnek
 */

public interface GenericListener {
    /**
     * Initializes with the formula and the current assignment.
     */
    default void init(Formula formula, Assignment assignment) {
    }
}
