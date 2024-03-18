package com.sadraskol.peg.parser;

import java.util.List;

public sealed interface Expression {
  record Forall(Expression elements, Symbol set, Expression predicate) implements Expression {}

  record Exists(Expression elements, Symbol set, Expression predicate) implements Expression {}

  record Tuple(List<Expression> tuples) implements Expression {}

  record Grouping(Expression expr) implements Expression {}

  record Variable(java.lang.String name) implements Expression {}

  record Symbol(java.lang.String name) implements Expression {}

  record Member(Variable callee, Expression relation) implements Expression {}

  record Implies(Expression left, Expression right) implements Expression {}

  record Equal(Expression left, Expression right) implements Expression {}

  record NotEqual(Expression left, Expression right) implements Expression {}

  record And(Expression left, Expression right) implements Expression {}

  record Number(int i) implements Expression {}

  record String(java.lang.String str) implements Expression {}

  record Call(Expression callee, Expression method, List<Expression> arguments)
      implements Expression {}

  record Array(List<Expression> tuples) implements Expression {}

  record Assignment(Expression target, Expression expr) implements Expression {}
}
