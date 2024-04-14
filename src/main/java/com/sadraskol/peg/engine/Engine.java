package com.sadraskol.peg.engine;

import com.sadraskol.peg.parser.Declaration;
import com.sadraskol.peg.parser.Expression;

import java.util.*;

public class Engine {
    private final List<Declaration> declarations;

    private final AbstractSet<String> sets;

    public Engine(List<Declaration> declarations) {
        this.declarations = declarations;
        this.sets = new HashSet<>();
    }

    public List<Proposition> propositions() {
        var propositions = new ArrayList<Proposition>();
        for (var declaration : declarations) {
            switch (declaration) {
                case Declaration.Record record -> {
                    sets.add(record.name());
                    propositions.add(
                            new Proposition.Binary(
                                    Operator.Equal, new Value.Set(new Set.Named(record.name())), new Value.Set(new Set.Universe())));
                    for (var relation : record.relations()) {
                        String relationName = record.name() + "#" + relation.name();
                        sets.add(relationName);
                        propositions.add(
                                new Proposition.Binary(
                                        Operator.Equal,
                                        new Value.Set(new Set.Named(relationName)),
                                        new Value.Set(new Set.Product(
                                                new Value.Set(new Set.Named(record.name())), new Value.Set(new Set.Named(relation.type()))))));
                    }
                }
                case Declaration.Facts facts -> {
                    for (var expr : facts.expressions()) {
                        propositions.add(evaluatePredicate(expr));
                    }
                }
                case Declaration.Constraint constraint -> propositions.add(evaluatePredicate(constraint.expr()));
                default -> {
                }
            }
        }
        return propositions;
    }

    private Proposition evaluatePredicate(Expression expression) {
        switch (expression) {
            case Expression.Equal equal -> {
                switch (evaluateValue(equal.left())) {
                    case Value.Set left -> {
                        var right = (Value.Set) evaluateValue(equal.right());
                        if (!(right.set() instanceof Set.Literal)) {
                            throw new IllegalStateException(
                                    "Expected literal set with values, got: " + right.set());
                        }
                        return new Proposition.Binary(Operator.Equal, left, right);
                    }
                    case Value.Curried curried -> {
                        var right = evaluateValue(equal.right());
                        var relation = new Value.Tuple(List.of(curried.value(), right));
                        return new Proposition.Binary(Operator.In, relation, curried.set());
                    }
                    default -> throw new IllegalStateException(
                            "Expected left equal expression, but got: " + equal.left());
                }
            }
            case Expression.NotEqual notEqual -> {
                switch (evaluateValue(notEqual.left())) {
                    case Value.Set left -> {
                        var right = (Value.Set) evaluateValue(notEqual.right());
                        if (!(right.set() instanceof Set.Literal)) {
                            throw new IllegalStateException(
                                    "Expected literal set with values, got: " + right.set());
                        }
                        return new Proposition.Binary(
                                Operator.Different, left, right);
                    }
                    case Value.Curried left -> {
                        var right = evaluateValue(notEqual.right());
                        var relation = new Value.Tuple(List.of(left.value(), right));
                        return new Proposition.Binary(Operator.NotIn, relation, left);
                    }
                    default -> throw new IllegalStateException(
                            "Expected left equal expression, but got: " + notEqual.left());
                }
            }
            case Expression.Forall forall -> {
                var vars = (Expression.Tuple) forall.elements();

                var set = new Value.Set(new Set.Named(forall.set().name()));
                var proposition = evaluatePredicate(forall.predicate());
                return new Proposition.Forall(
                        vars.tuples().stream()
                                .map(va -> (Expression.Variable) va)
                                .map(Expression.Variable::name)
                                .map(Value.Variable::new)
                                .toList(),
                        set,
                        proposition);
            }
            case Expression.Exists exists -> {
                var vars = (Expression.Tuple) exists.elements();

                var set = new Value.Set(new Set.Named(exists.set().name()));
                var proposition = evaluatePredicate(exists.predicate());
                return new Proposition.Exists(
                        vars.tuples().stream()
                                .map(va -> (Expression.Variable) va)
                                .map(Expression.Variable::name)
                                .map(Value.Variable::new)
                                .toList(),
                        set,
                        proposition);
            }
            case Expression.Implies implies -> {
                return new Proposition.Or(
                        new Proposition.Not(evaluatePredicate(implies.left())),
                        evaluatePredicate(implies.right()));
            }
            case Expression.Grouping grouping -> {
                return evaluatePredicate(grouping.expr());
            }
            case Expression.And and -> {
                return new Proposition.And(evaluatePredicate(and.left()), evaluatePredicate(and.right()));
            }
            default -> throw new IllegalStateException("Expected a predicate, got: " + expression);
        }
    }

    // TODO remove value expression here. Engine translates declarations and expression to Peg
    // Internal Representation
    private Value evaluateValue(Expression expr) {
        switch (expr) {
            case Expression.Set set -> {
                var members = set.tuples().stream().map(this::evaluateValue).toList();

                return new Value.Set(new Set.Literal(members));
            }
            case Expression.Member member -> {
                var relation = (Value.Variable) evaluateValue(member.relation());
                var set =
                        sets.stream()
                                .filter(key -> key.endsWith(relation.name()))
                                .findFirst()
                                .orElseThrow(
                                        () ->
                                                new IllegalStateException(
                                                        "Expected to find a relation with name, but none find: "
                                                                + relation.name()));
                return new Value.Curried(new Value.Set(new Set.Named(set)), evaluateValue(member.callee()));
            }
            case Expression.String string -> {
                return new Value.Str(string.str());
            }
            case Expression.Number num -> {
                return new Value.Number(num.i());
            }
            case Expression.Symbol symbol -> {
                return new Value.Set(new Set.Named(symbol.name()));
            }
            case Expression.Tuple tuple -> {
                var members = tuple.tuples().stream().map(this::evaluateValue).toList();

                return new Value.Tuple(members);
            }
            case Expression.Variable variable -> {
                return new Value.Variable(variable.name());
            }
            default -> throw new IllegalStateException("Expected value expression, got: " + expr);
        }
    }
}
