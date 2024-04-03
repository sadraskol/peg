package com.sadraskol.peg.engine;

import java.util.List;

public sealed interface Proposition {
    record Or(Proposition left, Proposition right) implements Proposition {}
    record And(Proposition left, Proposition right) implements Proposition {}
    record Forall(List<Value.Variable> args, Value.NamedSet set, Proposition predicate) implements Proposition {}
    record Exists(List<Value.Variable> args, Value.NamedSet set, Proposition predicate) implements Proposition {}
    record Not(Proposition other) implements Proposition {}
    record Primary(BinaryOp truth) implements Proposition {}
    record True() implements Proposition {}
    record False() implements Proposition {}
}
