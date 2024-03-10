package com.sadraskol.peg.parser;

import com.sadraskol.peg.scanner.Token;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Parser {
    private final Deque<Token> tokens;
    private final List<Statement> statements;

    public Parser(List<Token> tokens) {
        this.tokens = new ArrayDeque<>(tokens);
        this.statements = new ArrayList<>();
    }

    List<Statement> parse() {
        while (!(tokens.getFirst() instanceof Token.Eof())) {
            if (tokens.getFirst() instanceof Token.Import) {
                parseImportStatement();
            } else if (tokens.getFirst() instanceof Token.Record) {
                parseRecordStatement();
            } else {
                throw new IllegalStateException("Unexpected token " + tokens.getFirst() + ", expected a statement");
            }
        }
        return statements;
    }

    private void parseImportStatement() {
        tokens.pop(); // pop import
        var domain = new ArrayList<String>();
        loop: while (true) {
            var identifier = tokens.pop();
            switch (identifier) {
                case Token.Identifier sdl -> domain.add(sdl.content());
                case Token.Symbol tld -> {
                    domain.add(tld.content());
                    break loop;
                }
                default -> throw new IllegalStateException("Unexpected token type for an import: " + identifier);
            }
            tokens.pop(); // remove .
        }
        statements.add(new Statement.Import(domain));
    }

    private void parseRecordStatement() {
        tokens.pop(); // pop record
        var name = getSymbol(tokens.pop());

        tokens.pop(); // pop (
        var members = new ArrayList<RecordMember>();
        while (!(tokens.peek() instanceof Token.RightParen)) {
            var isIdentity = false;
            if (tokens.peek() instanceof Token.Identity) {
                isIdentity = true;
                tokens.pop(); // pop ,
            }
            var memberName = getIdentifier(tokens.pop());
            tokens.pop(); // pop :
            var memberType = getSymbol(tokens.pop());
            if (tokens.peek() instanceof Token.Comma) {
                tokens.pop(); // pop ,
            }
            members.add(new RecordMember(isIdentity, memberName, memberType));
        }
        tokens.pop(); // pop )
        tokens.pop(); // pop {

        var relations = new ArrayList<RecordRelation>();
        while (!(tokens.peek() instanceof Token.RightBrace)) {
            var isInjective = false;
            if (tokens.peek() instanceof Token.Injective) {
                isInjective = true;
                tokens.pop(); // pop ,
            }
            tokens.pop(); // pop relation

            var memberName = getIdentifier(tokens.pop());
            tokens.pop(); // pop :

            var memberType = getSymbol(tokens.pop());
            relations.add(new RecordRelation(isInjective, memberName, memberType));
        }
        tokens.pop(); // pop }

        statements.add(new Statement.Record(name, members, relations));
    }

    private static String getIdentifier(Token identifier) {
        if (identifier instanceof Token.Identifier) {
            return ((Token.Identifier) identifier).content();
        } else {
            throw new IllegalStateException("Expected an identifier, got: " + identifier);
        }
    }

    private static String getSymbol(Token symbol) {
        if (symbol instanceof Token.Symbol) {
            return ((Token.Symbol) symbol).content();
        } else {
            throw new IllegalStateException("Expected a type, got: " + symbol);
        }
    }
}
