package com.sadraskol.peg.engine;

import com.sadraskol.peg.parser.Declaration;
import com.sadraskol.peg.parser.Expression;

import java.util.*;

public class Engine {
    private final List<Declaration> declarations;

    private final java.util.Set<Type> types;

    public Engine(List<Declaration> declarations) {
        this.declarations = declarations;
        this.types = new HashSet<>();
    }

    public List<Proposition> propositions() {
        var propositions = new ArrayList<Proposition>();
        for (var declaration : declarations) {
            switch (declaration) {
                case Declaration.Record record -> {
                    var type = new Type(record.name(), new ArrayList<>(), new ArrayList<>());
                    propositions.add(
                            new Proposition.Binary(
                                    Operator.Equal, new Value.Set(new Set.Named(record.name())), new Value.Set(new Set.Universe())));
                    for (var field : record.members()) {
                        type.fields().add(new TypedRef(field.type(), field.name()));
                    }
                    for (var relation : record.relations()) {
                        type.relations().add(new TypedRef(relation.type(), relation.name()));
                        String relationName = record.name() + "#" + relation.name();
                        propositions.add(
                                new Proposition.Binary(
                                        Operator.Equal,
                                        new Value.Set(new Set.Named(relationName)),
                                        new Value.Set(new Set.Product(
                                                new Value.Set(new Set.Named(record.name())), new Value.Set(new Set.Named(relation.type()))))));
                    }
                    types.add(type);
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
                Value left = evaluateValue(equal.left());
                switch (left) {
                    case Value.Set _ -> {
                        var right = (Value.Set) evaluateValue(equal.right());
                        if (!(right.set() instanceof Set.Literal)) {
                            throw new IllegalStateException(
                                    "Expected literal set with values, got: " + right.set());
                        }
                        return new Proposition.Binary(Operator.Equal, left, right);
                    }
                    case Value.Variable _, Value.Member _ -> {
                        return new Proposition.Binary(Operator.Equal, left, evaluateValue(equal.right()));
                    }
                    case Value.Curried curried -> {
                        var right = evaluateValue(equal.right());
                        if (right instanceof Value.Curried) {
                            var rightNamed = (Set.Named) ((Value.Curried) right).set().set();
                            if (!curried.set().set().equals(rightNamed)) {
                                throw new IllegalStateException("Expected both curried values to be the same set, got: " + rightNamed + " and: " + curried.set().set());
                            }
                            var set = rightNamed.name().split("#")[0];
                            var relation = rightNamed.name().split("#")[1];
                            var type = types.stream().filter(t -> t.name().equals(set)).findFirst().get();
                            var setType = type.findTypeOf(relation).get();

                            Value.Variable commonVariable = new Value.Variable("_named");
                            var leftTuple = new Value.Tuple(List.of(curried.value(), commonVariable));
                            var rightTuple = new Value.Tuple(List.of(curried.value(), commonVariable));

                            var tupleSet = ((Value.Curried) right).set();

                            return new Proposition.Exists(List.of(commonVariable), new Value.Set(new Set.Named(setType.type())), new Proposition.And(
                                    new Proposition.Binary(Operator.In, leftTuple, tupleSet),
                                    new Proposition.Binary(Operator.In, rightTuple, tupleSet)
                            ));
                        } else {
                            var relation = new Value.Tuple(List.of(curried.value(), right));
                            return new Proposition.Binary(Operator.In, relation, curried.set());
                        }
                    }
                    default -> throw new IllegalStateException(
                            "Expected left equal expression, but got: " + equal.left());
                }
            }
            case Expression.NotEqual notEqual -> {
                Value left = evaluateValue(notEqual.left());
                switch (left) {
                    case Value.Set _ -> {
                        var right = (Value.Set) evaluateValue(notEqual.right());
                        if (!(right.set() instanceof Set.Literal)) {
                            throw new IllegalStateException(
                                    "Expected literal set with values, got: " + right.set());
                        }
                        return new Proposition.Binary(
                                Operator.Different, left, right);
                    }
                    case Value.Variable _, Value.Member _ -> {
                        return new Proposition.Binary(Operator.Different, left, evaluateValue(notEqual.right()));
                    }
                    case Value.Curried curried -> {
                        var right = evaluateValue(notEqual.right());
                        if (right instanceof Value.Curried) {
                            var rightNamed = (Set.Named) ((Value.Curried) right).set().set();
                            if (!curried.set().set().equals(rightNamed)) {
                                throw new IllegalStateException("Expected both curried values to be the same set, got: " + rightNamed + " and: " + curried.set().set());
                            }
                            var set = rightNamed.name().split("#")[0];
                            var relation = rightNamed.name().split("#")[1];
                            var type = types.stream().filter(t -> t.name().equals(set)).findFirst().get();
                            var setType = type.findTypeOf(relation).get();

                            Value.Variable commonVariable = new Value.Variable("_named");
                            var leftTuple = new Value.Tuple(List.of(curried.value(), commonVariable));
                            var rightTuple = new Value.Tuple(List.of(curried.value(), commonVariable));

                            var tupleSet = ((Value.Curried) right).set();

                            return new Proposition.Forall(List.of(commonVariable), new Value.Set(new Set.Named(setType.type())), new Proposition.Or(
                                    new Proposition.And(new Proposition.Binary(Operator.In, leftTuple, tupleSet), new Proposition.Binary(Operator.NotIn, rightTuple, tupleSet)),
                                    new Proposition.And(new Proposition.Binary(Operator.In, rightTuple, tupleSet), new Proposition.Binary(Operator.NotIn, leftTuple, tupleSet))
                            ));
                        } else {
                            var relation = new Value.Tuple(List.of(curried.value(), right));
                            return new Proposition.Binary(Operator.NotIn, relation, left);
                        }
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
            case Expression.Or or -> {
                return new Proposition.Or(evaluatePredicate(or.left()), evaluatePredicate(or.right()));
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
                var maybeRelation =
                        types.stream()
                                .flatMap(type -> type.findRelation(relation.name()).stream())
                                .findFirst();
                var maybeField =
                        types.stream()
                                .flatMap(type -> type.findField(relation.name()).stream())
                                .findFirst();
                if (maybeRelation.isPresent()) {
                    return new Value.Curried(new Value.Set(new Set.Named(maybeRelation.get())), evaluateValue(member.callee()));
                } else if (maybeField.isPresent()) {
                    return new Value.Member(evaluateValue(member.callee()), maybeField.get());
                } else {
                    throw new IllegalStateException(
                            "Expected to find a relation or a field with name, but none find: "
                                    + relation.name());
                }

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
