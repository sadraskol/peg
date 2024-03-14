package com.sadraskol.peg.scanner;

public record Token(int start, int line, String content, TokenType type) {
  int size() {
    return content.length();
  }
}
