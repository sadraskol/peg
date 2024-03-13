package com.sadraskol.peg.parser;

import org.junit.jupiter.params.provider.Arguments;

import java.util.List;

public record ParserTestCase(String filename, List<Statement> expectedStatements) {
    public Arguments toPair() {
        return Arguments.of(filename, expectedStatements);
    }
}