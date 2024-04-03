package com.sadraskol.peg.parser;

import java.util.List;

public sealed interface Declaration {
  record Import(QualifiedName name) implements Declaration {}

  record Record(String name, List<RecordMember> members, List<RecordRelation> relations)
      implements Declaration {}

  record Constraint(Expression expr) implements Declaration {}

  record Facts(List<Expression> expressions) implements Declaration {}
}
