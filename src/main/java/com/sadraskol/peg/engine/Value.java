package com.sadraskol.peg.engine;

import java.util.List;
import java.util.stream.Collectors;

public sealed interface Value {
    record Tuple(List<Value> values) implements Value {
        public String toString() {
            return "(" + values.stream().map(Object::toString).collect(Collectors.joining(", "))+ ")";
        }
    }

    record Str(String value) implements Value {
        public String toString() {
            return "\"" + value + "\"";
        }
    }

    record Set(List<Value> values) implements Value {}

    record NamedSet(String name) implements Value {}

    record Curried(NamedSet set, Value value) implements Value {}

    record Variable(String name) implements Value {}
}
