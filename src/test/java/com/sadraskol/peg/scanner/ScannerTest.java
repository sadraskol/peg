package com.sadraskol.peg.scanner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class ScannerTest {
    @ParameterizedTest
    @MethodSource("parserTestSet")
    void eof(String filename, List<Token> expectedTokens) throws URISyntaxException, IOException {
        URL resource = getUrl(filename);
        var fileUri = resource.toURI();
        var source = Files.readString(Path.of(fileUri));

        var parser = new Scanner(source);

        Assertions.assertEquals(expectedTokens, parser.scan());
    }

    @Test
    void allTokensAreIncludedInTheTests() {
        var testedTokens = getArgumentsStream()
                .map(ScannerPair::expectedTokens)
                .flatMap(Collection::stream)
                .map(Token::getClass)
                .map(Class::toString)
                .sorted()
                .distinct()
                .toList();

        List<String> implementedTokens = Arrays.stream(Token.class.getPermittedSubclasses())
                .map(Class::toString)
                .sorted()
                .toList();

        Assertions.assertEquals(implementedTokens, testedTokens);
    }

    private static Stream<Arguments> parserTestSet() {
        return getArgumentsStream().map(ScannerPair::toPair);
    }

    private static Stream<ScannerPair> getArgumentsStream() {
        return Stream.of(
                new ScannerPair("scanner/eof.peg", List.of(new Token.Eof())),
                new ScannerPair("scanner/imports.peg", List.of(new Token.Import(), new Token.Identifier("org"), new Token.Dot(), new Token.Identifier("peg"), new Token.Dot(), new Token.Symbol("String"), new Token.Eof())),
                new ScannerPair("scanner/records.peg", List.of(new Token.Record(), new Token.Symbol("Lesson"), new Token.LeftParen(), new Token.Identity(), new Token.Identifier("subject"), new Token.Colon(), new Token.Symbol("Subject"), new Token.RightParen(), new Token.LeftBrace(),
                        new Token.Injective(), new Token.Relation(), new Token.Identifier("slot"), new Token.Colon(), new Token.Symbol("Slot"),
                        new Token.Injective(), new Token.Relation(), new Token.Identifier("room"), new Token.Colon(), new Token.Symbol("Room"),
                        new Token.RightBrace(),
                        new Token.Eof())),
                new ScannerPair("scanner/facts.peg", List.of(new Token.Facts(), new Token.LeftBrace(),
                        new Token.Symbol("Slot"), new Token.Equal(), new Token.LeftBrace(),
                        new Token.LeftParen(), new Token.String("Monday"), new Token.Comma(), new Token.Symbol("LocalTime"), new Token.Dot(), new Token.Identifier("of"), new Token.LeftParen(), new Token.Number(8), new Token.Comma(), new Token.Number(30), new Token.RightParen(), new Token.RightParen(), new Token.Comma(),
                        new Token.RightBrace(),
                        new Token.RightBrace(),
                        new Token.Eof())),
                new ScannerPair("scanner/constraint.peg", List.of(new Token.Constraint(), new Token.LeftBrace(),
                        new Token.Forall(), new Token.LeftParen(), new Token.Identifier("l1"), new Token.Comma(), new Token.Identifier("l2"), new Token.RightParen(), new Token.In(), new Token.Symbol("Lesson"), new Token.Colon(),
                        new Token.LeftParen(), new Token.Identifier("l1"), new Token.BangEqual(), new Token.Identifier("l2"), new Token.And(), new Token.Identifier("l1"), new Token.Dot(), new Token.Identifier("slot"), new Token.EqualEqual(), new Token.Identifier("l2"), new Token.Dot(), new Token.Identifier("slot"), new Token.RightParen(),
                        new Token.Implies(), new Token.Identifier("l1"), new Token.Dot(), new Token.Identifier("room"), new Token.BangEqual(), new Token.Identifier("l2"), new Token.Dot(), new Token.Identifier("room"),
                        new Token.RightBrace(),
                        new Token.Eof()))
        );
    }

    private URL getUrl(String name) {
        URL resource = getClass().getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Cannot find file " + name + " in current class loader");
        }
        return resource;
    }
}
