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
        Set set = resolveSet(forall.set());
        if (!(set instanceof Set.Literal)) {
          throw new IllegalStateException("Expected literal set with values, got: " + set);
        }
        if (forall.args().size() == 1) {
          for (var value : ((Set.Literal) set).values()) {
            variables.push(Map.of(forall.args().getFirst().name(), value));
            propositions.add(evaluate(forall.predicate(), reify));
            variables.pop();
          }
        } else if (forall.args().size() == 2) {
          var values = ((Set.Literal) set).values();
          var pairs =
              values.stream()
                  .flatMap(v1 -> values.stream().map(v2 -> new Value.Tuple(List.of(v1, v2))))
                  .toList();

          for (var pair : pairs) {
            variables.push(
                Map.of(
                    forall.args().get(0).name(),
                    pair.values().get(0),
                    forall.args().get(1).name(),
                    pair.values().get(1)));
            propositions.add(evaluate(forall.predicate(), reify));
            variables.pop();
          }
        } else {
          throw new IllegalStateException(
              "Does not support more than 2 args in forall/exists loops");
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
        return evaluate(not.other().negate(), reify);
      }
      case Proposition.Or or -> {
        return new Proposition.Or(evaluate(or.left(), reify), evaluate(or.right(), reify));
      }
      case Proposition.And and -> {
        return new Proposition.And(evaluate(and.left(), reify), evaluate(and.right(), reify));
      }
      default -> throw new IllegalStateException("Cannot evaluate proposition: " + proposition);
    }
  }

  private Proposition evaluateBinaryOp(Proposition.Binary binary, boolean reify) {
    switch (binary.op()) {
      case Operator.Equal -> {
        switch (binary.left()) {
          case Value.Set set -> {
            var left = (Set.Named) set.set();
            var right = binary.right();
            current().put(left.name(), right);
            return new Proposition.True();
          }
          case Value.Variable _, Value.Member _ -> {
            var left = resolveValue(binary.left());
            var right = resolveValue(binary.right());
            if (left.equals(right)) {
              return new Proposition.True();
            } else {
              return new Proposition.False();
            }
          }
          default ->
              throw new IllegalStateException(
                  "Cannot evaluate equality for value: " + binary.left());
        }
      }
      case Operator.Different -> {
        switch (binary.left()) {
          case Value.Variable _, Value.Member _ -> {
            var left = resolveValue(binary.left());
            var right = resolveValue(binary.right());
            if (left.equals(right)) {
              return new Proposition.False();
            } else {
              return new Proposition.True();
            }
          }
          default ->
              throw new IllegalStateException(
                  "Cannot evaluate equality for value: " + binary.left());
        }
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
      case Value.Number _, Value.Str _ -> {
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
      case Value.Member member -> {
        var tuple = (Value.Tuple) resolveValue(member.value());
        return tuple.values().get(member.index());
      }
      default -> throw new IllegalStateException("Cannot resolve value: " + value);
    }
  }

  public Map<String, Value> current() {
    return variables.peek();
  }

  public Set resolveSet(Value.Set set) {
    switch (set.set()) {
      case Set.Literal _ -> {
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
      case Set.Universe _ -> throw new IllegalStateException("Cannot resolve Universe set");
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
            default ->
                throw new IllegalStateException("Expected a reified set, but got: " + set.set());
          }
        }
        default -> throw new IllegalStateException("Expected a final value, but got: " + val);
      }
    }
    return result;
  }
}
