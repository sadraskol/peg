package com.sadraskol.peg.engine;

import java.util.List;
import org.junit.jupiter.params.provider.Arguments;

public record EngineTestCase(String filename, List<Proposition> expectedPropositions) {
  public Arguments toPair() {
    return Arguments.of(filename, expectedPropositions);
  }
}
