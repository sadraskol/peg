package com.sadraskol.peg.scanner;

import java.util.ArrayList;
import java.util.List;

public class Scanner {
  private int current;

  private int line;
  private int start;

  private final String source;
  private final List<Token> tokens;

  public Scanner(String source) {
    this.source = source;
    this.current = 0;
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

      start = current;

      var c = advance();
      switch (c) {
        case '!' -> {
          if (matches('=')) {
            addToken(TokenType.BangEqual);
          } else {
            throw new RuntimeException("Unknown char (" + c + ") at " + current);
          }
        }
        case '(' -> addToken(TokenType.LeftParen);
        case ')' -> addToken(TokenType.RightParen);
        case '{' -> addToken(TokenType.LeftBrace);
        case '}' -> addToken(TokenType.RightBrace);
        case '=' -> {
          if (matches('=')) {
            addToken(TokenType.EqualEqual);
          } else {
            addToken(TokenType.Equal);
          }
        }
        case ',' -> addToken(TokenType.Comma);
        case '.' -> addToken(TokenType.Dot);
        case ':' -> addToken(TokenType.Colon);
        case '\"' -> scanString();
        case '/' -> {
          if (matches('/')) {
            scanComment();
          } else {
            throw new RuntimeException("Unknown char (" + c + ") at " + current);
          }
        }
        default -> {
          if (Character.isUpperCase(c)) {
            scanSymbol();
          } else if (Character.isLowerCase(c)) {
            scanIdentifier();
          } else if (Character.isDigit(c)) {
            scanNumber();
          } else {
            throw new RuntimeException("Unknown char (" + c + ") at " + current);
          }
        }
      }
    }

    start = current;
    addToken(TokenType.Eof);

    return tokens;
  }

  private char advance() {
    char c = source.charAt(current);
    current += 1;
    return c;
  }

  private void scanComment() {
    while (!endOfSource() && source.charAt(current) != '\n') {
      current += 1;
    }
  }

  private void skipWhiteSpaces() {
    while (!endOfSource() && Character.isWhitespace(source.charAt(current))) {
      current += 1;
    }
  }

  private boolean matches(char c) {
    if (source.charAt(current) == c) {
      current += 1;
      return true;
    }
    return false;
  }

  void addToken(TokenType type) {
    tokens.add(new Token(start, line, source.substring(start, current), type));
  }

  private void scanNumber() {
    while (Character.isDigit(source.charAt(current))) {
      current += 1;
    }
    addToken(TokenType.Number);
  }

  private void scanSymbol() {
    while (Character.isLetterOrDigit(source.charAt(current))) {
      current += 1;
    }
    addToken(TokenType.Symbol);
  }

  private void scanIdentifier() {
    while (Character.isLetterOrDigit(source.charAt(current))) {
      current += 1;
    }
    var matchedStr = source.substring(start, current);
    switch (matchedStr) {
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
      default -> addToken(TokenType.Identifier);
    }
  }

  private void scanString() {
    while (source.charAt(current) != '"') {
      current += 1;
    }
    current += 1;
    addToken(TokenType.String);
  }

  private boolean endOfSource() {
    return current >= source.length();
  }
}
