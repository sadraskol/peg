package com.sadraskol.peg.engine;

import java.util.List;

public sealed interface Value {
    record Tuple(List<Value> values) implements Value {}

    record Str(String value) implements Value {}

    record Set(List<Value> values) implements Value {}

    record NamedSet(String name) implements Value {}

    record Curried(NamedSet set, Value value) implements Value {}

    record Variable(String name) implements Value {}
}
