package com.sadraskol.peg.engine;

public sealed interface BinaryOp {
    record In(Value value, Value.NamedSet set) implements BinaryOp {
        public String toString() {
            return value.toString() + " in " + set.name();
        }
    }

    record Equal(Set left, Set right) implements BinaryOp {
        public String toString() {
            return left.toString() + " = " + right.toString();
        }
    }
}
