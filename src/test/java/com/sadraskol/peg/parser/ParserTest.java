package com.sadraskol.peg.parser;

import com.sadraskol.peg.TestUtils;
import com.sadraskol.peg.scanner.Scanner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class ParserTest {
    @ParameterizedTest
    @MethodSource("parserTestSet")
    void eof(String filename, List<Statement> expectedStatements) {
        var source = TestUtils.readFile(filename);
        var scanner = new Scanner(source);
        var parser = new Parser(scanner.scan());

        Assertions.assertEquals(expectedStatements, parser.parse());
    }

    @Test
    void allTokensAreIncludedInTheTests() {
        var testedTokens = getArgumentsStream()
                .map(ParserPair::expectedStatements)
                .flatMap(Collection::stream)
                .map(Statement::getClass)
                .map(Class::toString)
                .sorted()
                .distinct()
                .toList();

        List<String> implementedTokens = Arrays.stream(Statement.class.getPermittedSubclasses())
                .map(Class::toString)
                .sorted()
                .toList();

        Assertions.assertEquals(implementedTokens, testedTokens);
    }

    private static Stream<Arguments> parserTestSet() {
        return getArgumentsStream().map(ParserPair::toPair);
    }

    private static Stream<ParserPair> getArgumentsStream() {
        return Stream.of(
                new ParserPair("scanner/eof.peg", List.of()),
                new ParserPair("scanner/imports.peg", List.of(new Statement.Import(List.of("org", "peg", "String")))),
                new ParserPair("scanner/records.peg", List.of(new Statement.Record("Lesson", List.of(new RecordMember(true, "subject", "Subject")), List.of(new RecordRelation(true, "slot", "Slot"), new RecordRelation(false, "room", "Room")))))
        );
    }
}
