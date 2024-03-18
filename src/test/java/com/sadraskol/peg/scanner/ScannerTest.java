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
            .map(Token::type)
            .sorted()
            .distinct()
            .toList();

    List<TokenType> implementedTokens = Arrays.stream(TokenType.values()).sorted().toList();

    Assertions.assertEquals(implementedTokens, testedTokens);
  }

  private static Stream<Arguments> scannerTestCases() {
    return getArgumentsStream().map(ScannerTestCase::toPair);
  }

  private static Stream<ScannerTestCase> getArgumentsStream() {
    return Stream.of(
        new ScannerTestCase("scanner/eof.peg", List.of(new Token(0, 1, "", TokenType.Eof))),
        new ScannerTestCase(
            "scanner/imports.peg",
            List.of(
                new Token(0, 1, "import", TokenType.Import),
                new Token(0, 1, "org", TokenType.Identifier),
                new Token(0, 1, ".", TokenType.Dot),
                new Token(0, 1, "peg", TokenType.Identifier),
                new Token(0, 1, ".", TokenType.Dot),
                new Token(0, 1, "String", TokenType.Symbol),
                new Token(0, 1, "", TokenType.Eof))),
        new ScannerTestCase(
            "scanner/records.peg",
            List.of(
                new Token(0, 1, "record", TokenType.Record),
                new Token(0, 1, "Lesson", TokenType.Symbol),
                new Token(0, 1, "(", TokenType.LeftParen),
                new Token(0, 1, "identity", TokenType.Identity),
                new Token(0, 1, "subject", TokenType.Identifier),
                new Token(0, 1, ":", TokenType.Colon),
                new Token(0, 1, "Subject", TokenType.Symbol),
                new Token(0, 1, ")", TokenType.RightParen),
                new Token(0, 1, "{", TokenType.LeftBrace),
                new Token(0, 1, "injective", TokenType.Injective),
                new Token(0, 1, "relation", TokenType.Relation),
                new Token(0, 1, "slot", TokenType.Identifier),
                new Token(0, 1, ":", TokenType.Colon),
                new Token(0, 1, "Slot", TokenType.Symbol),
                new Token(0, 1, "relation", TokenType.Relation),
                new Token(0, 1, "room", TokenType.Identifier),
                new Token(0, 1, ":", TokenType.Colon),
                new Token(0, 1, "Room", TokenType.Symbol),
                new Token(0, 1, "}", TokenType.RightBrace),
                new Token(0, 1, "", TokenType.Eof))),
        new ScannerTestCase(
            "scanner/facts.peg",
            List.of(
                new Token(0, 1, "facts", TokenType.Facts),
                new Token(0, 1, "{", TokenType.LeftBrace),
                new Token(0, 1, "Slot", TokenType.Symbol),
                new Token(0, 1, "=", TokenType.Equal),
                new Token(0, 1, "{", TokenType.LeftBrace),
                new Token(0, 1, "(", TokenType.LeftParen),
                new Token(0, 1, "\"Monday\"", TokenType.String),
                new Token(0, 1, ",", TokenType.Comma),
                new Token(0, 1, "LocalTime", TokenType.Symbol),
                new Token(0, 1, ".", TokenType.Dot),
                new Token(0, 1, "of", TokenType.Identifier),
                new Token(0, 1, "(", TokenType.LeftParen),
                new Token(0, 1, "8", TokenType.Number),
                new Token(0, 1, ",", TokenType.Comma),
                new Token(0, 1, "30", TokenType.Number),
                new Token(0, 1, ")", TokenType.RightParen),
                new Token(0, 1, ")", TokenType.RightParen),
                new Token(0, 1, ",", TokenType.Comma),
                new Token(0, 1, "}", TokenType.RightBrace),
                new Token(0, 1, "}", TokenType.RightBrace),
                new Token(0, 1, "", TokenType.Eof))),
        new ScannerTestCase(
            "scanner/constraint.peg",
            List.of(
                new Token(0, 1, "constraint", TokenType.Constraint),
                new Token(0, 1, "{", TokenType.LeftBrace),
                new Token(0, 1, "forall", TokenType.Forall),
                new Token(0, 1, "l1", TokenType.Identifier),
                new Token(0, 1, ",", TokenType.Comma),
                new Token(0, 1, "l2", TokenType.Identifier),
                new Token(0, 1, "in", TokenType.In),
                new Token(0, 1, "Lesson", TokenType.Symbol),
                new Token(0, 1, ":", TokenType.Colon),
                new Token(0, 1, "(", TokenType.LeftParen),
                new Token(0, 1, "l1", TokenType.Identifier),
                new Token(0, 1, "!=", TokenType.BangEqual),
                new Token(0, 1, "l2", TokenType.Identifier),
                new Token(0, 1, "and", TokenType.And),
                new Token(0, 1, "l1", TokenType.Identifier),
                new Token(0, 1, ".", TokenType.Dot),
                new Token(0, 1, "slot", TokenType.Identifier),
                new Token(0, 1, "==", TokenType.EqualEqual),
                new Token(0, 1, "l2", TokenType.Identifier),
                new Token(0, 1, ".", TokenType.Dot),
                new Token(0, 1, "slot", TokenType.Identifier),
                new Token(0, 1, ")", TokenType.RightParen),
                new Token(0, 1, "implies", TokenType.Implies),
                new Token(0, 1, "l1", TokenType.Identifier),
                new Token(0, 1, ".", TokenType.Dot),
                new Token(0, 1, "room", TokenType.Identifier),
                new Token(0, 1, "!=", TokenType.BangEqual),
                new Token(0, 1, "l2", TokenType.Identifier),
                new Token(0, 1, ".", TokenType.Dot),
                new Token(0, 1, "room", TokenType.Identifier),
                new Token(0, 1, "}", TokenType.RightBrace),
                new Token(0, 1, "", TokenType.Eof))),
        new ScannerTestCase(
            "scanner/exists.peg",
            List.of(
                new Token(0, 1, "constraint", TokenType.Constraint),
                new Token(0, 1, "{", TokenType.LeftBrace),
                new Token(0, 1, "exists", TokenType.Exists),
                new Token(0, 1, "l", TokenType.Identifier),
                new Token(0, 1, "in", TokenType.In),
                new Token(0, 1, "Lesson", TokenType.Symbol),
                new Token(0, 1, ":", TokenType.Colon),
                new Token(0, 1, "l", TokenType.Identifier),
                new Token(0, 1, ".", TokenType.Dot),
                new Token(0, 1, "room", TokenType.Identifier),
                new Token(0, 1, "==", TokenType.EqualEqual),
                new Token(0, 1, "\"Room A\"", TokenType.String),
                new Token(0, 1, "}", TokenType.RightBrace),
                new Token(0, 1, "", TokenType.Eof))));
  }
}
