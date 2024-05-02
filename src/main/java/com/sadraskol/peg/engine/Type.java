package com.sadraskol.peg.engine;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Type(String name, List<TypedRef> fields, List<TypedRef> relations) {
  Optional<String> findRelation(String relationName) {
    if (relations.stream().anyMatch(relation -> relation.name().equals(relationName))) {
      return Optional.of(name + "#" + relationName);
    } else {
      return Optional.empty();
    }
  }

  Optional<Integer> findField(String fieldName) {
    OptionalInt indexOpt =
        IntStream.range(0, fields.size())
            .filter(i -> fieldName.equals(fields.get(i).name()))
            .findFirst();
    if (indexOpt.isPresent()) {
      return Optional.of(indexOpt.getAsInt());
    } else {
      return Optional.empty();
    }
  }

  public Optional<TypedRef> findTypeOf(String relation) {
    return Stream.concat(relations.stream(), fields.stream())
        .filter(t -> t.name().equals(relation))
        .findFirst();
  }
}
