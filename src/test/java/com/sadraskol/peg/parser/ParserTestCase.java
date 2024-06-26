package com.sadraskol.peg.parser;

import java.util.List;
import org.junit.jupiter.params.provider.Arguments;

public record ParserTestCase(String filename, List<Declaration> expectedDeclarations) {
  public Arguments toPair() {
    return Arguments.of(filename, expectedDeclarations);
  }
}
