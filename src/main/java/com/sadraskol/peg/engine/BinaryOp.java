package com.sadraskol.peg.engine;

public sealed interface BinaryOp {
    BinaryOp negate();

    record In(Value value, Value.NamedSet set) implements BinaryOp {
        public String toString() {
            return value.toString() + " in " + set.name();
        }

        public BinaryOp negate() {
            return new NotIn(value, set);
        }
    }

    record NotIn(Value value, Value.NamedSet set) implements BinaryOp {
        public String toString() {
            return value.toString() + " not in " + set.name();
        }

        public BinaryOp negate() {
            return new In(value, set);
        }
    }

    record Equal(Set left, Set right) implements BinaryOp {
        public String toString() {
            return left.toString() + " = " + right.toString();
        }

        public BinaryOp negate() {
            return new Different(left, right);
        }
    }

    record Different(Set left, Set right) implements BinaryOp {
        public String toString() {
            return left.toString() + " != " + right.toString();
        }

        public BinaryOp negate() {
            return new Equal(left, right);
        }
    }
}
