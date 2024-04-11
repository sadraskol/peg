package com.sadraskol.peg.backend;

import com.sadraskol.peg.engine.BinaryOp;
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
      case Proposition.Primary primary -> {
        return evaluateBinaryOp(primary.truth(), reify);
      }
      case Proposition.Forall forall -> {
        var propositions = new ArrayList<Proposition>();
        for (var arg : forall.args()) {
          Set set = resolveSet(forall.set().name()).set();
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
          Set set = resolveSet(exists.set().name()).set();
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
        var primary = (Proposition.Primary) not.other();
        return evaluateBinaryOp(primary.truth().negate(), reify);
      }
      default -> throw new IllegalStateException("Cannot evaluate proposition: " + proposition);
    }
  }

  private Proposition evaluateBinaryOp(BinaryOp op, boolean reify) {
    switch (op) {
      case BinaryOp.Equal equal -> {
        var left = (Set.Named) equal.left();
        var right = equal.right();
        current().put(left.name(), new Value.Set(right));
        return new Proposition.True();
      }
      case BinaryOp.In in -> {
        var value = resolveValue(in.value());
        var setName = in.set();

        var set = resolveSet(setName.name());

        switch (set.set()) {
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
              return new Proposition.Primary(new BinaryOp.In(value, setName));
            }
          }
          default -> {
            return new Proposition.Primary(new BinaryOp.In(value, setName));
          }
        }
      }
      case BinaryOp.NotIn in -> {
        var value = resolveValue(in.value());
        var setName = in.set();

        var set = resolveSet(setName.name());

        switch (set.set()) {
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
              return new Proposition.Primary(new BinaryOp.In(value, setName));
            }
          }
          default -> {
            return new Proposition.Primary(new BinaryOp.NotIn(value, setName));
          }
        }
      }
      default -> throw new IllegalStateException("Unhandled binary op: " + op);
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

  public Value.Set resolveSet(String set) {
    for (var stack : variables) {
      if (stack.get(set) != null) {
        return (Value.Set) stack.get(set);
      }
    }
    throw new IllegalStateException("Cannot find set with name: " + set);
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
              var left = resolveSet(product.leftSet().name()).set();
              var right = resolveSet(product.rightSet().name()).set();

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
