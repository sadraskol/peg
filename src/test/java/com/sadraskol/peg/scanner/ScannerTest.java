package com.sadraskol.peg.scanner;

import com.sadraskol.peg.TestUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ScannerTest {
  @ParameterizedTest
  @MethodSource("scannerTestCases")
  void eof(String filename, List<Token> expectedTokens) {
    var source = TestUtils.readFile(filename);

    var scanner = new Scanner(source);

    Assertions.assertEquals(expectedTokens, scanner.scan());
  }

  @Test
  void allTokensAreIncludedInTheTests() {
    var testedTokens =
        getArgumentsStream()
            .map(ScannerTestCase::expectedTokens)
            .flatMap(Collection::stream)
            .map(Token::getClass)
            .map(Class::toString)
            .sorted()
            .distinct()
            .toList();

    List<String> implementedTokens =
        Arrays.stream(Token.class.getPermittedSubclasses()).map(Class::toString).sorted().toList();

    Assertions.assertEquals(implementedTokens, testedTokens);
  }

  private static Stream<Arguments> scannerTestCases() {
    return getArgumentsStream().map(ScannerTestCase::toPair);
  }

  private static Stream<ScannerTestCase> getArgumentsStream() {
    return Stream.of(
        new ScannerTestCase("scanner/eof.peg", List.of(new Token.Eof())),
        new ScannerTestCase(
            "scanner/imports.peg",
            List.of(
                new Token.Import(),
                new Token.Identifier("org"),
                new Token.Dot(),
                new Token.Identifier("peg"),
                new Token.Dot(),
                new Token.Symbol("String"),
                new Token.Eof())),
        new ScannerTestCase(
            "scanner/records.peg",
            List.of(
                new Token.Record(),
                new Token.Symbol("Lesson"),
                new Token.LeftParen(),
                new Token.Identity(),
                new Token.Identifier("subject"),
                new Token.Colon(),
                new Token.Symbol("Subject"),
                new Token.RightParen(),
                new Token.LeftBrace(),
                new Token.Injective(),
                new Token.Relation(),
                new Token.Identifier("slot"),
                new Token.Colon(),
                new Token.Symbol("Slot"),
                new Token.Relation(),
                new Token.Identifier("room"),
                new Token.Colon(),
                new Token.Symbol("Room"),
                new Token.RightBrace(),
                new Token.Eof())),
        new ScannerTestCase(
            "scanner/facts.peg",
            List.of(
                new Token.Facts(),
                new Token.LeftBrace(),
                new Token.Symbol("Slot"),
                new Token.Equal(),
                new Token.LeftBrace(),
                new Token.LeftParen(),
                new Token.String("Monday"),
                new Token.Comma(),
                new Token.Symbol("LocalTime"),
                new Token.Dot(),
                new Token.Identifier("of"),
                new Token.LeftParen(),
                new Token.Number(8),
                new Token.Comma(),
                new Token.Number(30),
                new Token.RightParen(),
                new Token.RightParen(),
                new Token.Comma(),
                new Token.RightBrace(),
                new Token.RightBrace(),
                new Token.Eof())),
        new ScannerTestCase(
            "scanner/constraint.peg",
            List.of(
                new Token.Constraint(),
                new Token.LeftBrace(),
                new Token.Forall(),
                new Token.Identifier("l1"),
                new Token.Comma(),
                new Token.Identifier("l2"),
                new Token.In(),
                new Token.Symbol("Lesson"),
                new Token.Colon(),
                new Token.LeftParen(),
                new Token.Identifier("l1"),
                new Token.BangEqual(),
                new Token.Identifier("l2"),
                new Token.And(),
                new Token.Identifier("l1"),
                new Token.Dot(),
                new Token.Identifier("slot"),
                new Token.EqualEqual(),
                new Token.Identifier("l2"),
                new Token.Dot(),
                new Token.Identifier("slot"),
                new Token.RightParen(),
                new Token.Implies(),
                new Token.Identifier("l1"),
                new Token.Dot(),
                new Token.Identifier("room"),
                new Token.BangEqual(),
                new Token.Identifier("l2"),
                new Token.Dot(),
                new Token.Identifier("room"),
                new Token.RightBrace(),
                new Token.Eof())));
  }
}
