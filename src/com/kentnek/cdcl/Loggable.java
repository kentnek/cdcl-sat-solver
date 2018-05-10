package com.kentnek.cdcl;

/**
 * Loggable base class.
 * <p>
 *
 * @author kentnek
 */

public class Loggable {
    protected boolean debug = false;

    public Loggable debug() {
        return debug(true);
    }

    public Loggable debug(boolean isDebugging) {
        this.debug = isDebugging;
        return this;
    }

}
