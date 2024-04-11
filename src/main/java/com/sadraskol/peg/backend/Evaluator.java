package com.sadraskol.peg.backend;

import com.sadraskol.peg.engine.BinaryOp;
import com.sadraskol.peg.engine.Proposition;
import com.sadraskol.peg.engine.Set;
import com.sadraskol.peg.engine.Value;

import java.util.*;

public class Evaluator {
    private final Deque<Map<String, Value>> variables;
    private final List<Proposition> propositions;
    public Evaluator() {
        this.propositions = new ArrayList<>();
        this.variables = new ArrayDeque<>();
        this.variables.push(new HashMap<>());
    }

    public Proposition evaluate(Proposition proposition) {
        switch(proposition) {
            case Proposition.Primary primary -> {
                return evaluateBinaryOp(primary.truth());
            }
            case Proposition.Forall forall -> {
                var propositions = new ArrayList<Proposition>();
                for (var arg: forall.args()) {
                    Set set = resolveSet(forall.set()).orElseThrow(() -> new IllegalStateException("Cannot find variable named: " + forall.set().name())).set();
                    if (!(set instanceof Set.Literal)) {
                        throw new IllegalStateException("Expected literal set with values, got: " + set);
                    }
                    for (var value: ((Set.Literal) set).values()) {
                        variables.push(Map.of(arg.name(), value));
                        propositions.add(evaluate(forall.predicate()));
                        variables.pop();
                    }
                }
                return propositions.stream().reduce(new Proposition.True(), Proposition.And::new);
            }
            case Proposition.Exists exists -> {
                var propositions = new ArrayList<Proposition>();
                for (var arg: exists.args()) {
                    Set set = resolveSet(exists.set()).orElseThrow(() -> new IllegalStateException("Cannot find variable named: " + exists.set().name())).set();
                    if (!(set instanceof Set.Literal)) {
                        throw new IllegalStateException("Expected literal set with values, got: " + set);
                    }
                    for (var value: ((Set.Literal) set).values()) {
                        variables.push(Map.of(arg.name(), value));
                        propositions.add(evaluate(exists.predicate()));
                        variables.pop();
                    }
                }
                return propositions.stream().reduce(new Proposition.False(), Proposition.Or::new);
            }
            case Proposition.Not not -> {
                var primary = (Proposition.Primary) not.other();
                return evaluateBinaryOp(primary.truth().negate());
            }
            default -> throw new IllegalStateException("Cannot evaluate proposition: " + proposition);
        }
    }

    private Proposition evaluateBinaryOp(BinaryOp op) {
        switch(op) {
            case BinaryOp.Equal equal -> {
                var left = (Set.Named) equal.left();
                var right = equal.right();
                current().put(left.name(), new Value.Set(right));
                return new Proposition.True();
            }
            case BinaryOp.In in -> {
                var value = resolveValue(in.value());
                var setName = in.set();

                var maybeValues = resolveSet(setName);

                if (maybeValues.isPresent()) {
                    var set = maybeValues.get().set();
                    switch (set) {
                        case Set.Literal literal -> {
                            if (literal.values().contains(value)) {
                                return new Proposition.True();
                            } else {
                                return new Proposition.False();
                            }
                        }
                        default -> {
                            return new Proposition.Primary(new BinaryOp.In(value, setName));
                        }
                    }
                } else {
                    throw new IllegalStateException("Expected an existing set, got: " + setName);
                }
            }
            case BinaryOp.NotIn in -> {
                var value = resolveValue(in.value());
                var setName = in.set();

                var maybeValues = resolveSet(setName);

                if (maybeValues.isPresent()) {
                    var set = maybeValues.get().set();
                    switch (set) {
                        case Set.Literal literal -> {
                            if (literal.values().contains(value)) {
                                return new Proposition.False();
                            } else {
                                return new Proposition.True();
                            }
                        }
                        default -> {
                            return new Proposition.Primary(new BinaryOp.NotIn(value, setName));
                        }
                    }
                } else {
                    throw new IllegalStateException("Expected an existing set, got: " + setName);
                }
            }
            default -> throw new IllegalStateException("Unhandled binary op: " + op);
        }
    }

    private Value resolveValue(Value value) {
        switch(value) {
            case Value.Str ignored -> {
                return value;
            }
            case Value.Tuple tuple -> {
                return new Value.Tuple(tuple.values().stream().map(this::resolveValue).toList());
            }
            case Value.Variable variable -> {
                for (var stack: variables) {
                    if (stack.get(variable.name()) != null) {
                        return stack.get(variable.name());
                    }
                }
                throw new IllegalStateException("Cannot resolve value named: " + variable);
            }
            default -> throw new IllegalStateException("Cannot resolve value: " + value);
        }
    }

    public Map<String, Value> current() {
        return variables.peek();
    }

    public Deque<Map<String, Value>> getVariables() {
        return variables;
    }

    public List<Proposition> getPropositions() {
        return propositions;
    }

    public Optional<Value.Set> resolveSet(Value.NamedSet set) {
        for (var stack: variables) {
            if (stack.get(set.name()) != null) {
                return Optional.of((Value.Set) stack.get(set.name()));
            }
        }
        return Optional.empty();
    }
}
