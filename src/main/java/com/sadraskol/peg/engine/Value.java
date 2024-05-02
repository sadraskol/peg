package com.sadraskol.peg.engine;

import java.util.List;
import java.util.stream.Collectors;

public sealed interface Value {
  record Tuple(List<Value> values) implements Value {
    public String toString() {
      return "(" + values.stream().map(Object::toString).collect(Collectors.joining(", ")) + ")";
    }
  }

  record Str(String value) implements Value {
    public String toString() {
      return "\"" + value + "\"";
    }
  }

  record Number(int i) implements Value {
    public String toString() {
      return Integer.toString(i);
    }
  }

  record Set(com.sadraskol.peg.engine.Set set) implements Value {}

  record Curried(Value.Set set, Value value) implements Value {}

  record Variable(String name) implements Value {}

  record Member(Value value, Integer index) implements Value {
    public String toString() {
      return value.toString() + "[" + index + "]";
    }
  }
}
