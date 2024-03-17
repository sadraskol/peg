package com.sadraskol.peg.parser;

import com.sadraskol.peg.TestUtils;
import com.sadraskol.peg.scanner.Scanner;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ParserTest {
  @ParameterizedTest
  @MethodSource("parserTestCases")
  void parserTestCase(String filename, List<Declaration> expectedDeclarations) {
    var source = TestUtils.readFile(filename);
    var scanner = new Scanner(source);
    var parser = new Parser(scanner.scan());

    Assertions.assertEquals(expectedDeclarations, parser.parse());
  }

  @Test
  void allStatementsAreIncludedInTheTests() {
    var testStatements =
        getArgumentsStream()
            .map(ParserTestCase::expectedDeclarations)
            .flatMap(Collection::stream)
            .map(Declaration::getClass)
            .sorted(Comparator.comparing(Class::toString))
            .distinct()
            .toList();

    var implementedTokens =
        Arrays.stream(Declaration.class.getPermittedSubclasses())
            .sorted(Comparator.comparing(Class::toString))
            .toList();

    Assertions.assertEquals(implementedTokens, testStatements);
  }

  private static Stream<Arguments> parserTestCases() {
    return getArgumentsStream().map(ParserTestCase::toPair);
  }

  private static Stream<ParserTestCase> getArgumentsStream() {
    return Stream.of(
        new ParserTestCase("scanner/eof.peg", List.of()),
        new ParserTestCase(
            "scanner/imports.peg",
            List.of(new Declaration.Import(List.of("org", "peg", "String")))),
        new ParserTestCase(
            "scanner/records.peg",
            List.of(
                new Declaration.Record(
                    "Lesson",
                    List.of(new RecordMember(true, "subject", "Subject")),
                    List.of(
                        new RecordRelation(true, "slot", "Slot"),
                        new RecordRelation(false, "room", "Room"))))),
        new ParserTestCase(
            "scanner/facts.peg",
            List.of(
                new Declaration.Facts(
                    List.of(
                        new Expression.Assignment(
                            new Expression.Symbol("Slot"),
                            new Expression.Array(
                                List.of(
                                    new Expression.Tuple(
                                        List.of(
                                            new Expression.String("Monday"),
                                            new Expression.Call(
                                                new Expression.Symbol("LocalTime"),
                                                new Expression.Variable("of"),
                                                List.of(
                                                    new Expression.Number(8),
                                                    new Expression.Number(30)))))))))))),
        new ParserTestCase(
            "scanner/constraint.peg",
            List.of(
                new Declaration.Constraint(
                    new Expression.Forall(
                        new Expression.Tuple(
                            List.of(new Expression.Variable("l1"), new Expression.Variable("l2"))),
                        new Expression.Symbol("Lesson"),
                        new Expression.Implies(
                            new Expression.Grouping(
                                new Expression.And(
                                    new Expression.NotEqual(
                                        new Expression.Variable("l1"),
                                        new Expression.Variable("l2")),
                                    new Expression.Equal(
                                        new Expression.Member(
                                            new Expression.Variable("l1"),
                                            new Expression.Variable("slot")),
                                        new Expression.Member(
                                            new Expression.Variable("l2"),
                                            new Expression.Variable("slot"))))),
                            new Expression.NotEqual(
                                new Expression.Member(
                                    new Expression.Variable("l1"), new Expression.Variable("room")),
                                new Expression.Member(
                                    new Expression.Variable("l2"),
                                    new Expression.Variable("room")))))))));
  }
}
