package com.sadraskol.peg.engine;

public enum Operator {
    In, NotIn, Equal, Different;

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
