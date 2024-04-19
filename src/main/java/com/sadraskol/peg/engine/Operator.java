package com.sadraskol.peg.engine;

public enum Operator {
    In("in"), NotIn("not in"), Equal("=="), Different("!=");

    private final String str;
    Operator(String str) {
        this.str = str;
    }

    public String toString() {
        return str;
    }

    public Operator negate() {
        switch (this) {
            case In -> {
                return NotIn;
            }
            case NotIn -> {
                return In;
            }
            case Equal -> {
                return Different;
            }
            case Different -> {
                return Equal;
            }
            default -> throw new IllegalStateException("cannot negate: " + this);
        }
    }
}
