package com.sadraskol.peg.engine;

import org.junit.jupiter.params.provider.Arguments;

import java.util.List;

public record EngineTestCase(String filename, List<Proposition> expectedPropositions) {
  public Arguments toPair() {
    return Arguments.of(filename, expectedPropositions);
  }
}