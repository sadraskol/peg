package com.sadraskol.peg.engine;

import java.util.HashSet;
import java.util.List;

public sealed interface Set {
    record Literal(List<Value> values) implements Set {}

    record Universe() implements Set {}

    record Product(Named leftSet, Named rightSet, java.util.Set<Value> including, java.util.Set<Value> excludes) implements Set {
        public Product(Named leftSet, Named rightSet) {
            this(leftSet, rightSet, new HashSet<>(), new HashSet<>());
        }

        public void including(Value value) {
            including.add(value);
        }
        public void excluding(Value value) {
            excludes.add(value);
        }
    }

    record Named(String name) implements Set {}
}
