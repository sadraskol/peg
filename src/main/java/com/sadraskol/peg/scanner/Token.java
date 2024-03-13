package com.sadraskol.peg.scanner;

public sealed interface Token {

  int size();

  record String(java.lang.String str) implements Token {
    public int size() {
      return str.length() + 2;
    }
  }

  record Facts() implements Token {
    public int size() {
      return 5;
    }
  }

  record Import() implements Token {
    public int size() {
      return 6;
    }
  }

  record Record() implements Token {
    public int size() {
      return 6;
    }
  }

  record Constraint() implements Token {
    public int size() {
      return 10;
    }
  }

  record Forall() implements Token {
    public int size() {
      return 6;
    }
  }

  record In() implements Token {
    public int size() {
      return 2;
    }
  }

  record Implies() implements Token {
    public int size() {
      return 7;
    }
  }

  record And() implements Token {
    public int size() {
      return 3;
    }
  }

  record Identity() implements Token {
    public int size() {
      return 8;
    }
  }

  record Relation() implements Token {
    public int size() {
      return 8;
    }
  }

  record Injective() implements Token {
    public int size() {
      return 9;
    }
  }

  record EqualEqual() implements Token {
    public int size() {
      return 2;
    }
  }

  record BangEqual() implements Token {
    public int size() {
      return 2;
    }
  }

  record LeftParen() implements Token {
    public int size() {
      return 1;
    }
  }

  record RightParen() implements Token {
    public int size() {
      return 1;
    }
  }

  record LeftBrace() implements Token {
    public int size() {
      return 1;
    }
  }

  record RightBrace() implements Token {
    public int size() {
      return 1;
    }
  }

  record Equal() implements Token {
    public int size() {
      return 1;
    }
  }

  record Comma() implements Token {
    public int size() {
      return 1;
    }
  }

  record Dot() implements Token {
    public int size() {
      return 1;
    }
  }

  record Colon() implements Token {
    public int size() {
      return 1;
    }
  }

  record Identifier(java.lang.String name) implements Token {
    public int size() {
      return name.length();
    }
  }

  record Symbol(java.lang.String name) implements Token {
    public int size() {
      return name.length();
    }
  }

  record Number(int i) implements Token {
    public int size() {
      return Integer.toString(i).length();
    }
  }

  record Eof() implements Token {
    public int size() {
      return 0;
    }
  }
}
