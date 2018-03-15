package com.kentnek.cdcl.model;

/**
 * Represents a 3-state logic value: true, false and undefined.
 * <p>
 *
 * @author kentnek
 */

public enum Logic {
    TRUE, FALSE, UNDEFINED;

    public Logic and(Logic other) {
        if (this == UNDEFINED || other == UNDEFINED) return UNDEFINED;
        if (this == TRUE && other == TRUE) return TRUE;
        return FALSE;
    }

    public Logic or(Logic other) {
        if (this == UNDEFINED || other == UNDEFINED) return UNDEFINED;
        if (this == TRUE || other == TRUE) return TRUE;
        return FALSE;
    }

    public Logic not() {
        if (this == UNDEFINED) return UNDEFINED;
        if (this == TRUE) return FALSE;
        return TRUE;
    }

    public static Logic fromBoolean(boolean value) {
        return value ? TRUE : FALSE;
    }
}
