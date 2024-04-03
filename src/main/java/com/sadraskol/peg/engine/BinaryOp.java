package com.sadraskol.peg.engine;

public sealed interface BinaryOp {
    record In(Value value, Value.NamedSet set) implements BinaryOp {}

    record Equal(Set left, Set right) implements BinaryOp {}
}
