package com.sadraskol.peg.scanner;

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

  public List<Token> scan() {
    while (!endOfSource()) {
      skipWhiteSpaces();
      if (endOfSource()) {
        break;
      }
      if (startsWith("import")) {
        addToken(new Token.Import());
      } else if (startsWith("facts")) {
        addToken(new Token.Facts());
      } else if (startsWith("record")) {
        addToken(new Token.Record());
      } else if (startsWith("constraint")) {
        addToken(new Token.Constraint());
      } else if (startsWith("forall")) {
        addToken(new Token.Forall());
      } else if (startsWith("injective")) {
        addToken(new Token.Injective());
      } else if (startsWith("in")) {
        addToken(new Token.In());
      } else if (startsWith("implies")) {
        addToken(new Token.Implies());
      } else if (startsWith("and")) {
        addToken(new Token.And());
      } else if (startsWith("identity")) {
        addToken(new Token.Identity());
      } else if (startsWith("relation")) {
        addToken(new Token.Relation());
      } else if (startsWith("==")) {
        addToken(new Token.EqualEqual());
      } else if (startsWith("!=")) {
        addToken(new Token.BangEqual());
      } else if (startsWith("(")) {
        addToken(new Token.LeftParen());
      } else if (startsWith(")")) {
        addToken(new Token.RightParen());
      } else if (startsWith("{")) {
        addToken(new Token.LeftBrace());
      } else if (startsWith("}")) {
        addToken(new Token.RightBrace());
      } else if (startsWith("=")) {
        addToken(new Token.Equal());
      } else if (startsWith(",")) {
        addToken(new Token.Comma());
      } else if (startsWith(".")) {
        addToken(new Token.Dot());
      } else if (startsWith(":")) {
        addToken(new Token.Colon());
      } else if (startsWith("\"")) {
        scanString();
      } else if (Character.isUpperCase(source.charAt(current))) {
        scanSymbol();
      } else if (Character.isLowerCase(source.charAt(current))) {
        scanIdentifier();
      } else if (Character.isDigit(source.charAt(current))) {
        scanNumber();
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

  void addToken(Token token) {
    tokens.add(token);
    current += token.size();
  }

  private void scanNumber() {
    var content = new StringBuilder();
    while (Character.isDigit(source.charAt(current))) {
      content.append(source.charAt(current));
      current += 1;
    }
    tokens.add(new Token.Number(Integer.parseInt(content.toString())));
  }

  private void scanSymbol() {
    var content = new StringBuilder();
    while (Character.isLetterOrDigit(source.charAt(current))) {
      content.append(source.charAt(current));
      current += 1;
    }
    tokens.add(new Token.Symbol(content.toString()));
  }

  private void scanIdentifier() {
    var content = new StringBuilder();
    while (Character.isLetterOrDigit(source.charAt(current))) {
      content.append(source.charAt(current));
      current += 1;
    }
    tokens.add(new Token.Identifier(content.toString()));
  }

  private void scanString() {
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
