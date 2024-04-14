package com.sadraskol.peg.backend;

import com.sadraskol.peg.engine.Operator;
import com.sadraskol.peg.engine.Proposition;
import com.sadraskol.peg.engine.Set;
import com.sadraskol.peg.engine.Value;

import java.util.*;

public class Evaluator {
    private final Deque<Map<String, Value>> variables;

    public Evaluator() {
        this.variables = new ArrayDeque<>();
        this.variables.push(new HashMap<>());
    }

    public Proposition evaluate(Proposition proposition) {
        return evaluate(proposition, false);
    }

    public Proposition evaluate(Proposition proposition, boolean reify) {
        switch (proposition) {
            case Proposition.Binary primary -> {
                return evaluateBinaryOp(primary, reify);
            }
            case Proposition.Forall forall -> {
                var propositions = new ArrayList<Proposition>();
                for (var arg : forall.args()) {
                    Set set = resolveSet(forall.set());
                    if (!(set instanceof Set.Literal)) {
                        throw new IllegalStateException("Expected literal set with values, got: " + set);
                    }
                    for (var value : ((Set.Literal) set).values()) {
                        variables.push(Map.of(arg.name(), value));
                        propositions.add(evaluate(forall.predicate(), reify));
                        variables.pop();
                    }
                }
                return propositions.stream().reduce(new Proposition.True(), Proposition.And::new);
            }
            case Proposition.Exists exists -> {
                var propositions = new ArrayList<Proposition>();
                for (var arg : exists.args()) {
                    Set set = resolveSet(exists.set());
                    if (!(set instanceof Set.Literal)) {
                        throw new IllegalStateException("Expected literal set with values, got: " + set);
                    }
                    for (var value : ((Set.Literal) set).values()) {
                        variables.push(Map.of(arg.name(), value));
                        propositions.add(evaluate(exists.predicate(), false));
                        variables.pop();
                    }
                }
                return propositions.stream().reduce(new Proposition.False(), Proposition.Or::new);
            }
            case Proposition.Not not -> {
                var binary = (Proposition.Binary) not.other();
                return evaluateBinaryOp(binary.negate(), reify);
            }
            default -> throw new IllegalStateException("Cannot evaluate proposition: " + proposition);
        }
    }

    private Proposition evaluateBinaryOp(Proposition.Binary binary, boolean reify) {
        switch (binary.op()) {
            case Operator.Equal -> {
                var left = (Set.Named) ((Value.Set) binary.left()).set();
                var right = binary.right();
                current().put(left.name(), right);
                return new Proposition.True();
            }
            case Operator.In -> {
                var value = resolveValue(binary.left());

                var set = resolveSet((Value.Set) binary.right());

                switch (set) {
                    case Set.Literal literal -> {
                        if (literal.values().contains(value)) {
                            return new Proposition.True();
                        } else {
                            return new Proposition.False();
                        }
                    }
                    case Set.Product product -> {
                        if (reify) {
                            product.including(value);
                            return new Proposition.True();
                        } else {
                            return new Proposition.Binary(Operator.In, value, binary.right());
                        }
                    }
                    default -> {
                        return new Proposition.Binary(Operator.In, value, binary.right());
                    }
                }
            }
            case Operator.NotIn -> {
                var value = resolveValue(binary.left());

                var set = resolveSet((Value.Set) binary.right());

                switch (set) {
                    case Set.Literal literal -> {
                        if (literal.values().contains(value)) {
                            return new Proposition.False();
                        } else {
                            return new Proposition.True();
                        }
                    }
                    case Set.Product product -> {
                        if (reify) {
                            product.excluding(value);
                            return new Proposition.True();
                        } else {
                            return new Proposition.Binary(Operator.NotIn, value, binary.right());
                        }
                    }
                    default -> {
                        return new Proposition.Binary(Operator.NotIn, value, binary.right());
                    }
                }
            }
            default -> throw new IllegalStateException("Unhandled binary op: " + binary.op());
        }
    }

    private Value resolveValue(Value value) {
        switch (value) {
            case Value.Str ignored -> {
                return value;
            }
            case Value.Tuple tuple -> {
                return new Value.Tuple(tuple.values().stream().map(this::resolveValue).toList());
            }
            case Value.Variable variable -> {
                for (var stack : variables) {
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

    public Set resolveSet(Value.Set set) {
        switch (set.set()) {
            case Set.Literal literal -> {
                return set.set();
            }
            case Set.Named named -> {
                for (var stack : variables) {
                    if (stack.get(named.name()) != null) {
                        return ((Value.Set) stack.get(named.name())).set();
                    }
                }
                throw new IllegalStateException("Cannot find set with name: " + set);
            }
            case Set.Product product -> {
                return product;
            }
            case Set.Universe universe -> throw new IllegalStateException("Cannot resolve Universe set");
        }
    }

    public Map<String, Set> reify() {
        var result = new HashMap<String, Set>();
        for (var stack : current().entrySet()) {
            var val = stack.getValue();
            switch (val) {
                case Value.Set set -> {
                    switch (set.set()) {
                        case Set.Literal literal -> result.put(stack.getKey(), literal);
                        case Set.Product product -> {
                            var scalar = new HashSet<Value>();
                            var left = resolveSet(product.leftSet());
                            var right = resolveSet(product.rightSet());

                            var leftElements = ((Set.Literal) left).values();
                            var rightElements = ((Set.Literal) right).values();

                            for (var l : leftElements) {
                                for (var r : rightElements) {
                                    scalar.add(new Value.Tuple(List.of(l, r)));
                                }
                            }

                            scalar.removeAll(product.excludes());

                            if (product.including().containsAll(scalar)) {
                                result.put(stack.getKey(), new Set.Literal(scalar.stream().toList()));
                            } else {
                                throw new IllegalStateException("Could not resolve scalar set: " + product);
                            }
                        }
                        default -> throw new IllegalStateException("Expected a reified set, but got: " + set.set());
                    }
                }
                default -> throw new IllegalStateException("Expected a final value, but got: " + val);
            }
        }
        return result;
    }
}
