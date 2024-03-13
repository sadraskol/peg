package com.sadraskol.peg.scanner;

import java.util.List;
import org.junit.jupiter.params.provider.Arguments;

public record ScannerTestCase(String filename, List<Token> expectedTokens) {
  public Arguments toPair() {
    return Arguments.of(filename, expectedTokens);
  }
}
