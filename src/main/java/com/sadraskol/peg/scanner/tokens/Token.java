package com.sadraskol.peg.scanner.tokens;

public sealed interface Token {
    record String(java.lang.String content) implements Token {
    }

    record Facts() implements Token {
    }

    record Import() implements Token {
    }

    record Record() implements Token {
    }

    record Constraint() implements Token {
    }

    record Forall() implements Token {
    }

    record In() implements Token {
    }

    record Implies() implements Token {
    }

    record And() implements Token {
    }

    record Identity() implements Token {
    }

    record Relation() implements Token {
    }

    record Injective() implements Token {
    }

    record EqualEqual() implements Token {
    }

    record BangEqual() implements Token {
    }

    record LeftParen() implements Token {
    }

    record RightParen() implements Token {
    }

    record LeftBrace() implements Token {
    }

    record RightBrace() implements Token {
    }

    record Equal() implements Token {
    }

    record Comma() implements Token {
    }

    record Dot() implements Token {
    }

    record Colon() implements Token {
    }

    record Identifier(java.lang.String content) implements Token {
    }

    record Symbol(java.lang.String content) implements Token {
    }

    record Number(int i) implements Token {
    }

    record Eof() implements Token {
    }
}
