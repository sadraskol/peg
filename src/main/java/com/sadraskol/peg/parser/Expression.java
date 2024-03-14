package com.sadraskol.peg.parser;

import java.util.List;

public sealed interface Expression {
  record Forall(Tuple elements, Symbol set, Expression predicate) implements Expression {}

  record Tuple(List<Expression> tuples) implements Expression {}

  record Grouping(Expression expr) implements Expression {}

  record Variable(String name) implements Expression {}

  record Symbol(String name) implements Expression {}

  record Member(Variable callee, Expression relation) implements Expression {}

  record Implies(Expression left, Expression right) implements Expression {}

  record Equal(Expression left, Expression right) implements Expression {}

  record NotEqual(Expression left, Expression right) implements Expression {}

  record And(Expression left, Expression right) implements Expression {}
}
