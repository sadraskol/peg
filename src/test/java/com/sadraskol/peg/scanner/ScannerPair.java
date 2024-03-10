package com.sadraskol.peg.scanner;

import org.junit.jupiter.params.provider.Arguments;

import java.util.List;

public record ScannerPair(String filename, List<Token> expectedTokens) {
    public Arguments toPair() {
        return Arguments.of(filename, expectedTokens);
    }
}
