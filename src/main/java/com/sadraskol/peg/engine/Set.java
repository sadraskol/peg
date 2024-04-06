package com.sadraskol.peg.engine;

import java.util.List;

public sealed interface Set {
    record Literal(List<Value> values) implements Set {}

    record Universe() implements Set {}

    record Product(Set leftSet, Set rightSet) implements Set {}

    record Named(String name) implements Set {}
}
