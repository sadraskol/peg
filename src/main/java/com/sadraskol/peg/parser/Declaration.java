package com.sadraskol.peg.parser;

import java.util.List;

public sealed interface Declaration {
  record Import(List<String> domain) implements Declaration {}

  record Record(String name, List<RecordMember> members, List<RecordRelation> relations)
      implements Declaration {}

  record Constraint(Expression expr) implements Declaration {}
}