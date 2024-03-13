package com.sadraskol.peg.parser;

import java.util.List;

public sealed interface ConstraintExpr {
  record Forall(Tuple elements, Symbol set, ConstraintExpr predicate) implements ConstraintExpr {}

  record Tuple(List<ConstraintExpr> tuples) implements ConstraintExpr {}

  record Grouping(ConstraintExpr expr) implements ConstraintExpr {}

  record Variable(String name) implements ConstraintExpr {}

  record Symbol(String name) implements ConstraintExpr {}

  record Member(Variable callee, ConstraintExpr relation) implements ConstraintExpr {}

  record Implies(ConstraintExpr left, ConstraintExpr right) implements ConstraintExpr {}

  record Equal(ConstraintExpr left, ConstraintExpr right) implements ConstraintExpr {}

  record NotEqual(ConstraintExpr left, ConstraintExpr right) implements ConstraintExpr {}

  record And(ConstraintExpr left, ConstraintExpr right) implements ConstraintExpr {}
}
