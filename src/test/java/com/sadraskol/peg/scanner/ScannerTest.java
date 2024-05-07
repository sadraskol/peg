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
  void scannerTest(String filename, List<Token> expectedTokens) {
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
        new ScannerTestCase("scanner/comment.peg", List.of(new Token(27, 1, "", TokenType.Eof))),
        new ScannerTestCase(
            "scanner/imports.peg",
            List.of(
                new Token(0, 1, "import", TokenType.Import),
                new Token(7, 1, "org", TokenType.Identifier),
                new Token(10, 1, ".", TokenType.Dot),
                new Token(11, 1, "peg", TokenType.Identifier),
                new Token(14, 1, ".", TokenType.Dot),
                new Token(15, 1, "String", TokenType.Symbol),
                new Token(22, 1, "", TokenType.Eof))),
        new ScannerTestCase(
            "scanner/records.peg",
            List.of(
                new Token(0, 1, "record", TokenType.Record),
                new Token(7, 1, "Lesson", TokenType.Symbol),
                new Token(13, 1, "(", TokenType.LeftParen),
                new Token(14, 1, "identity", TokenType.Identity),
                new Token(23, 1, "subject", TokenType.Identifier),
                new Token(30, 1, ":", TokenType.Colon),
                new Token(32, 1, "Subject", TokenType.Symbol),
                new Token(39, 1, ")", TokenType.RightParen),
                new Token(41, 1, "{", TokenType.LeftBrace),
                new Token(47, 1, "injective", TokenType.Injective),
                new Token(57, 1, "relation", TokenType.Relation),
                new Token(66, 1, "slot", TokenType.Identifier),
                new Token(70, 1, ":", TokenType.Colon),
                new Token(72, 1, "Slot", TokenType.Symbol),
                new Token(81, 1, "relation", TokenType.Relation),
                new Token(90, 1, "room", TokenType.Identifier),
                new Token(94, 1, ":", TokenType.Colon),
                new Token(96, 1, "Room", TokenType.Symbol),
                new Token(101, 1, "}", TokenType.RightBrace),
                new Token(102, 1, "", TokenType.Eof))),
        new ScannerTestCase(
            "scanner/facts.peg",
            List.of(
                new Token(0, 1, "facts", TokenType.Facts),
                new Token(6, 1, "{", TokenType.LeftBrace),
                new Token(12, 1, "Slot", TokenType.Symbol),
                new Token(17, 1, "=", TokenType.Equal),
                new Token(19, 1, "{", TokenType.LeftBrace),
                new Token(29, 1, "(", TokenType.LeftParen),
                new Token(30, 1, "\"Monday\"", TokenType.String),
                new Token(38, 1, ",", TokenType.Comma),
                new Token(40, 1, "LocalTime", TokenType.Symbol),
                new Token(49, 1, ".", TokenType.Dot),
                new Token(50, 1, "of", TokenType.Identifier),
                new Token(52, 1, "(", TokenType.LeftParen),
                new Token(53, 1, "8", TokenType.Number),
                new Token(54, 1, ",", TokenType.Comma),
                new Token(56, 1, "30", TokenType.Number),
                new Token(58, 1, ")", TokenType.RightParen),
                new Token(59, 1, ")", TokenType.RightParen),
                new Token(60, 1, ",", TokenType.Comma),
                new Token(66, 1, "}", TokenType.RightBrace),
                new Token(68, 1, "}", TokenType.RightBrace),
                new Token(69, 1, "", TokenType.Eof))),
        new ScannerTestCase(
            "scanner/constraint.peg",
            List.of(
                new Token(1, 1, "constraint", TokenType.Constraint),
                new Token(12, 1, "{", TokenType.LeftBrace),
                new Token(18, 1, "forall", TokenType.Forall),
                new Token(25, 1, "l1", TokenType.Identifier),
                new Token(27, 1, ",", TokenType.Comma),
                new Token(29, 1, "l2", TokenType.Identifier),
                new Token(32, 1, "in", TokenType.In),
                new Token(35, 1, "Lesson", TokenType.Symbol),
                new Token(41, 1, ":", TokenType.Colon),
                new Token(51, 1, "(", TokenType.LeftParen),
                new Token(52, 1, "l1", TokenType.Identifier),
                new Token(55, 1, "!=", TokenType.BangEqual),
                new Token(58, 1, "l2", TokenType.Identifier),
                new Token(61, 1, "and", TokenType.And),
                new Token(65, 1, "l1", TokenType.Identifier),
                new Token(67, 1, ".", TokenType.Dot),
                new Token(68, 1, "slot", TokenType.Identifier),
                new Token(73, 1, "==", TokenType.EqualEqual),
                new Token(76, 1, "l2", TokenType.Identifier),
                new Token(78, 1, ".", TokenType.Dot),
                new Token(79, 1, "slot", TokenType.Identifier),
                new Token(83, 1, ")", TokenType.RightParen),
                new Token(93, 1, "implies", TokenType.Implies),
                new Token(101, 1, "(", TokenType.LeftParen),
                new Token(102, 1, "l1", TokenType.Identifier),
                new Token(104, 1, ".", TokenType.Dot),
                new Token(105, 1, "room", TokenType.Identifier),
                new Token(110, 1, "!=", TokenType.BangEqual),
                new Token(113, 1, "l2", TokenType.Identifier),
                new Token(115, 1, ".", TokenType.Dot),
                new Token(116, 1, "room", TokenType.Identifier),
                new Token(121, 1, "or", TokenType.Or),
                new Token(124, 1, "l1", TokenType.Identifier),
                new Token(127, 1, "==", TokenType.EqualEqual),
                new Token(130, 1, "l2", TokenType.Identifier),
                new Token(132, 1, ")", TokenType.RightParen),
                new Token(134, 1, "}", TokenType.RightBrace),
                new Token(135, 1, "", TokenType.Eof))),
        new ScannerTestCase(
            "scanner/exists.peg",
            List.of(
                new Token(1, 1, "constraint", TokenType.Constraint),
                new Token(12, 1, "{", TokenType.LeftBrace),
                new Token(18, 1, "exists", TokenType.Exists),
                new Token(25, 1, "l", TokenType.Identifier),
                new Token(27, 1, "in", TokenType.In),
                new Token(30, 1, "Lesson", TokenType.Symbol),
                new Token(36, 1, ":", TokenType.Colon),
                new Token(46, 1, "l", TokenType.Identifier),
                new Token(47, 1, ".", TokenType.Dot),
                new Token(48, 1, "room", TokenType.Identifier),
                new Token(53, 1, "==", TokenType.EqualEqual),
                new Token(56, 1, "\"Room A\"", TokenType.String),
                new Token(65, 1, "}", TokenType.RightBrace),
                new Token(66, 1, "", TokenType.Eof))));
  }
}
