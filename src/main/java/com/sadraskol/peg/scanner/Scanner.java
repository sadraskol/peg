package com.sadraskol.peg.scanner;

import com.sadraskol.peg.scanner.tokens.Token;

import java.util.ArrayList;
import java.util.List;

public class Scanner {
    private int current;
    private final String source;
    private final List<Token> tokens;

    public Scanner(String source) {
        this.source = source;
        this.current = 0;
        this.tokens = new ArrayList<>();
    }

    public List<Token> parse() {
        while (!endOfSource()) {
            skipWhiteSpaces();
            if (endOfSource()) {
                break;
            }
            if (startsWith("import")) {
                tokens.add(new Token.Import());
                current += 6;
            } else if (startsWith("facts")) {
                tokens.add(new Token.Facts());
                current += 5;
            } else if (startsWith("record")) {
                tokens.add(new Token.Record());
                current += 6;
            } else if (startsWith("constraint")) {
                tokens.add(new Token.Constraint());
                current += 10;
            } else if (startsWith("forall")) {
                tokens.add(new Token.Forall());
                current += 6;
            } else if (startsWith("injective")) {
                tokens.add(new Token.Injective());
                current += 9;
            } else if (startsWith("in")) {
                tokens.add(new Token.In());
                current += 2;
            } else if (startsWith("implies")) {
                tokens.add(new Token.Implies());
                current += 7;
            } else if (startsWith("and")) {
                tokens.add(new Token.And());
                current += 3;
            } else if (startsWith("identity")) {
                tokens.add(new Token.Identity());
                current += 8;
            } else if (startsWith("relation")) {
                tokens.add(new Token.Relation());
                current += 8;
            } else if (startsWith("==")) {
                tokens.add(new Token.EqualEqual());
                current += 2;
            } else if (startsWith("!=")) {
                tokens.add(new Token.BangEqual());
                current += 2;
            } else if (startsWith("(")) {
                tokens.add(new Token.LeftParen());
                current += 1;
            } else if (startsWith(")")) {
                tokens.add(new Token.RightParen());
                current += 1;
            } else if (startsWith("{")) {
                tokens.add(new Token.LeftBrace());
                current += 1;
            } else if (startsWith("}")) {
                tokens.add(new Token.RightBrace());
                current += 1;
            } else if (startsWith("=")) {
                tokens.add(new Token.Equal());
                current += 1;
            } else if (startsWith(",")) {
                tokens.add(new Token.Comma());
                current += 1;
            } else if (startsWith(".")) {
                tokens.add(new Token.Dot());
                current += 1;
            } else if (startsWith(":")) {
                tokens.add(new Token.Colon());
                current += 1;
            } else if (startsWith("\"")) {
                parseString();
            } else if (Character.isUpperCase(source.charAt(current))) {
                parseSymbol();
            } else if (Character.isLowerCase(source.charAt(current))) {
                parseIdentifier();
            } else if (Character.isDigit(source.charAt(current))) {
                parseNumber();
            } else {
                throw new RuntimeException("Unknown char (" + source.charAt(current) + ") at " + current);
            }
        }

        tokens.add(new Token.Eof());

        return tokens;
    }

    private void skipWhiteSpaces() {
        while (!endOfSource() && Character.isWhitespace(source.charAt(current))) {
            current += 1;
        }
    }

    private boolean startsWith(String substr) {
        return source.substring(current).startsWith(substr);
    }

    private void parseNumber() {
        var content = new StringBuilder();
        while (Character.isDigit(source.charAt(current))) {
            content.append(source.charAt(current));
            current += 1;
        }
        tokens.add(new Token.Number(Integer.parseInt(content.toString())));
    }

    private void parseSymbol() {
        var content = new StringBuilder();
        while (Character.isLetterOrDigit(source.charAt(current))) {
            content.append(source.charAt(current));
            current += 1;
        }
        tokens.add(new Token.Symbol(content.toString()));
    }

    private void parseIdentifier() {
        var content = new StringBuilder();
        while (Character.isLetterOrDigit(source.charAt(current))) {
            content.append(source.charAt(current));
            current += 1;
        }
        tokens.add(new Token.Identifier(content.toString()));
    }

    private void parseString() {
        current += 1; // remove init "
        var content = new StringBuilder();
        while (source.charAt(current) != '"') {
            content.append(source.charAt(current));
            current += 1;
        }
        current += 1; // remove trailing "
        tokens.add(new Token.String(content.toString()));
    }

    private boolean endOfSource() {
        return current >= source.length();
    }
}
