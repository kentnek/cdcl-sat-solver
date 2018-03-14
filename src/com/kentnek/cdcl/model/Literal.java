package com.kentnek.cdcl.model;

/**
 * Represents a boolean literal.
 * <p>
 *
 * @author kentnek
 */

public class Literal {
    public final int variable;
    public final boolean isNegated;

    public Literal(int literalNum) {
        assert (literalNum != 0);
        this.variable = Math.abs(literalNum);
        this.isNegated = literalNum < 0;
    }

    public int toLiteralNum() {
        return variable * (isNegated ? -1 : 1);
    }

    public Literal negate() {
        return new Literal(toLiteralNum() * -1);
    }

    @Override
    public String toString() {
        return (isNegated ? "¬" : "") + String.valueOf(variable);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Literal) {
            Literal other = (Literal) obj;
            return this.variable == other.variable && this.isNegated == other.isNegated;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return toLiteralNum();
    }
}
