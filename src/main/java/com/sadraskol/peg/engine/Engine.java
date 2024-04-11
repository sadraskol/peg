package com.sadraskol.peg.engine;

import com.sadraskol.peg.parser.Declaration;
import com.sadraskol.peg.parser.Expression;
import com.sadraskol.peg.parser.QualifiedName;

import java.util.*;

public class Engine {
    private final List<Declaration> declarations;

    private final List<QualifiedName> imports;
    private final AbstractSet<String> sets;

    public Engine(List<Declaration> declarations) {
        this.declarations = declarations;
        this.imports = new ArrayList<>();
        this.sets = new HashSet<>();
    }

    public List<Proposition> propositions() {
        var propositions = new ArrayList<Proposition>();
        for (var declaration : declarations) {
            switch (declaration) {
                case Declaration.Import imp -> imports.add(imp.name());
                case Declaration.Record record -> {
                    sets.add(record.name());
                    propositions.add(new Proposition.Primary(new BinaryOp.Equal(new Set.Named(record.name()), new Set.Universe())));
                    for (var relation : record.relations()) {
                        sets.add(record.name() + "#" + relation.name());
                        propositions.add(new Proposition.Primary(new BinaryOp.Equal(new Set.Named(record.name() + "#" + relation.name()), new Set.Product(new Set.Named(record.name()), new Set.Named(relation.name())))));
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

    public Proposition evaluatePredicate(Expression expression) {
        switch (expression) {
            case Expression.Equal equal -> {
                switch (evaluateValue(equal.left())) {
                    case Value.NamedSet left -> {
                        var right = (Value.Set) evaluateValue(equal.right());
                        if (!(right.set() instanceof Set.Literal)) {
                            throw new IllegalStateException("Expected literal set with values, got: " + right.set());
                        }
                        return new Proposition.Primary(new BinaryOp.Equal(new Set.Named(left.name()), right.set()));
                    }
                    case Value.Curried left -> {
                        var right = evaluateValue(equal.right());
                        var relation = new Value.Tuple(List.of(left.value(), right));
                        return new Proposition.Primary(new BinaryOp.In(relation, left.set()));
                    }
                    default ->
                            throw new IllegalStateException("Expected left equal expression, but got: " + equal.left());
                }
            }
            case Expression.Forall forall -> {
                var vars = (Expression.Tuple) forall.elements();

                var set = new Value.NamedSet(forall.set().name());
                var proposition = evaluatePredicate(forall.predicate());
                return new Proposition.Forall(vars.tuples().stream().map(va -> (Expression.Variable) va).map(va -> va.name()).map(va -> new Value.Variable(va)).toList(), set, proposition);
            }
            case Expression.Exists exists -> {
                var vars = (Expression.Tuple) exists.elements();

                var set = new Value.NamedSet(exists.set().name());
                var proposition = evaluatePredicate(exists.predicate());
                return new Proposition.Exists(vars.tuples().stream().map(va -> (Expression.Variable) va).map(va -> va.name()).map(va -> new Value.Variable(va)).toList(), set, proposition);
            }
            default -> throw new IllegalStateException("Expected a predicate, got: " + expression);
        }
    }

    // TODO remove value expression here. Engine translates declarations and expression to Peg Internal Representation
    private Value evaluateValue(Expression expr) {
        switch (expr) {
            case Expression.Set set -> {
                var members = set.tuples().stream().map(this::evaluateValue).toList();

                return new Value.Set(new Set.Literal(members));
            }
            case Expression.Member member -> {
                var relation = (Value.Variable) evaluateValue(member.relation());
                var set = sets.stream().filter(key -> key.endsWith(relation.name())).findFirst().orElseThrow(() -> new IllegalStateException("Expected to find a relation with name, but none find: " + relation.name()));
                return new Value.Curried(new Value.NamedSet(set), evaluateValue(member.callee()));
            }
            case Expression.String string -> {
                return new Value.Str(string.str());
            }
            case Expression.Symbol symbol -> {
                return new Value.NamedSet(symbol.name());
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
