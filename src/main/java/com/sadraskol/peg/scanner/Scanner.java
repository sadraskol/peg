package com.sadraskol.peg.scanner;

import java.util.ArrayList;
import java.util.List;

public class Scanner {
  private int current;
  private String matchedStr;

  private int line;
  private int start;

  private final String source;
  private final List<Token> tokens;

  public Scanner(String source) {
    this.source = source;
    this.current = 0;
    this.matchedStr = "";
    this.line = 1;
    this.start = 0;
    this.tokens = new ArrayList<>();
  }

  public List<Token> scan() {
    while (!endOfSource()) {
      skipWhiteSpaces();
      if (endOfSource()) {
        break;
      }
      if (matches("==")) {
        addToken(TokenType.EqualEqual);
      } else if (matches("!=")) {
        addToken(TokenType.BangEqual);
      } else if (matches("(")) {
        addToken(TokenType.LeftParen);
      } else if (matches(")")) {
        addToken(TokenType.RightParen);
      } else if (matches("{")) {
        addToken(TokenType.LeftBrace);
      } else if (matches("}")) {
        addToken(TokenType.RightBrace);
      } else if (matches("=")) {
        addToken(TokenType.Equal);
      } else if (matches(",")) {
        addToken(TokenType.Comma);
      } else if (matches(".")) {
        addToken(TokenType.Dot);
      } else if (matches(":")) {
        addToken(TokenType.Colon);
      } else if (matches("\"")) {
        scanString();
      } else if (matches("//")) {
        scanComment();
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

    addToken(TokenType.Eof);

    return tokens;
  }

  private void scanComment() {
    while (!endOfSource() && source.charAt(current) != '\n') {
      current += 1;
    }
    this.matchedStr = "";
  }

  private void skipWhiteSpaces() {
    while (!endOfSource() && Character.isWhitespace(source.charAt(current))) {
      current += 1;
    }
  }

  private boolean matches(String str) {
    var doesMatch = source.substring(current).startsWith(str);
    if (doesMatch) {
      matchedStr = str;
    }
    return doesMatch;
  }

  void addToken(TokenType type) {
    tokens.add(new Token(start, line, matchedStr, type));
    current += matchedStr.length();
    matchedStr = "";
  }

  private void scanNumber() {
    var builder = new StringBuilder();
    while (Character.isDigit(source.charAt(current))) {
      builder.append(source.charAt(current));
      current += 1;
    }
    tokens.add(new Token(start, line, builder.toString(), TokenType.Number));
  }

  private void scanSymbol() {
    var builder = new StringBuilder();
    while (Character.isLetterOrDigit(source.charAt(current))) {
      builder.append(source.charAt(current));
      current += 1;
    }
    tokens.add(new Token(start, line, builder.toString(), TokenType.Symbol));
  }

  private void scanIdentifier() {
    var builder = new StringBuilder();
    while (Character.isLetterOrDigit(source.charAt(current))) {
      builder.append(source.charAt(current));
      current += 1;
    }
    var content = builder.toString();
    switch (content) {
      case "import" -> addToken(TokenType.Import);
      case "facts" -> addToken(TokenType.Facts);
      case "record" -> addToken(TokenType.Record);
      case "exists" -> addToken(TokenType.Exists);
      case "constraint" -> addToken(TokenType.Constraint);
      case "forall" -> addToken(TokenType.Forall);
      case "injective" -> addToken(TokenType.Injective);
      case "in" -> addToken(TokenType.In);
      case "implies" -> addToken(TokenType.Implies);
      case "and" -> addToken(TokenType.And);
      case "or" -> addToken(TokenType.Or);
      case "identity" -> addToken(TokenType.Identity);
      case "relation" -> addToken(TokenType.Relation);
      default -> tokens.add(new Token(start, line, content, TokenType.Identifier));
    }
  }

  private void scanString() {
    var builder = new StringBuilder();
    var quoteCount = 0;
    while (quoteCount < 2) {
      var c = source.charAt(current);
      builder.append(c);
      current += 1;

      if (c == '"') {
        quoteCount += 1;
      }
    }
    tokens.add(new Token(start, line, builder.toString(), TokenType.String));
  }

  private boolean endOfSource() {
    return current >= source.length();
  }
}
