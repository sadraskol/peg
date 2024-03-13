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
            } else if (tokens.getFirst() instanceof Token.Constraint) {
                parseConstraintStatement();
            } else {
                throw new IllegalStateException("Unexpected token " + tokens.getFirst() + ", expected a statement");
            }
        }
        return statements;
    }

    private void parseImportStatement() {
        pop(Token.Import.class);
        var domain = new ArrayList<String>();
        loop:
        while (true) {
            var identifier = tokens.pop();
            switch (identifier) {
                case Token.Identifier sdl -> domain.add(sdl.name());
                case Token.Symbol tld -> {
                    domain.add(tld.name());
                    break loop;
                }
                default -> throw new IllegalStateException("Unexpected token type for an import: " + identifier);
            }
            pop(Token.Dot.class);
        }
        statements.add(new Statement.Import(domain));
    }

    private void parseRecordStatement() {
        pop(Token.Record.class);
        var name = getSymbolName(tokens.pop());

        pop(Token.LeftParen.class);
        var members = new ArrayList<RecordMember>();
        while (!(tokens.peek() instanceof Token.RightParen)) {
            var isIdentity = false;
            if (tokens.peek() instanceof Token.Identity) {
                isIdentity = true;
                pop(Token.Identity.class);
            }
            var memberName = getIdentifierName(tokens.pop());
            pop(Token.Colon.class);
            var memberType = getSymbolName(tokens.pop());
            if (tokens.peek() instanceof Token.Comma) {
                pop(Token.Comma.class);
            }
            members.add(new RecordMember(isIdentity, memberName, memberType));
        }
        pop(Token.RightParen.class);
        pop(Token.LeftBrace.class);

        var relations = new ArrayList<RecordRelation>();
        while (!(tokens.peek() instanceof Token.RightBrace)) {
            var isInjective = false;
            if (tokens.peek() instanceof Token.Injective) {
                isInjective = true;
                pop(Token.Injective.class);
            }

            pop(Token.Relation.class);

            var memberName = getIdentifierName(tokens.pop());
            pop(Token.Colon.class);

            var memberType = getSymbolName(tokens.pop());
            relations.add(new RecordRelation(isInjective, memberName, memberType));
        }
        pop(Token.RightBrace.class);

        statements.add(new Statement.Record(name, members, relations));
    }

    private void parseConstraintStatement() {
        pop(Token.Constraint.class);
        pop(Token.LeftBrace.class);

        var expr = constraintExpr();

        pop(Token.RightBrace.class);

        statements.add(new Statement.Constraint(expr));
    }

    private ConstraintExpr constraintExpr() {
	return impliesExpr();
    }

    private ConstraintExpr impliesExpr() {
	    var left = orExpr();

	    if (tokens.peek() instanceof Token.Implies) {
		    pop(Token.Implies.class);

		    var right = impliesExpr();

		    return new ConstraintExpr.Implies(left, right);
	    } else {
		    return left;
	    }
    }

    private ConstraintExpr orExpr() {
	    var left = andExpr();

	    // if (tokens.peek() instanceof Token.Or) {
	    //         pop(Token.Or.class);

	    //         var right = orExpr();

	    //         return new ConstraintExpr.Or(left, right);
	    // } else {
		    return left;
	    // }
    }

    private ConstraintExpr andExpr() {
	    var left = equalityExpr();

	    if (tokens.peek() instanceof Token.And) {
		    pop(Token.And.class);

		    var right = andExpr();

		    return new ConstraintExpr.And(left, right);
	    } else {
		    return left;
	    }
    }

    private ConstraintExpr equalityExpr() {
	    var left = unaryExpr();

	    if (tokens.peek() instanceof Token.EqualEqual) {
		    pop(Token.EqualEqual.class);

		    var right = equalityExpr();

		    return new ConstraintExpr.Equal(left, right);
	    } else if (tokens.peek() instanceof Token.BangEqual) {
		    pop(Token.BangEqual.class);

		    var right = equalityExpr();

		    return new ConstraintExpr.NotEqual(left, right);
	    } else {
		    return left;
	    }
    }

    private ConstraintExpr unaryExpr() {
	    return memberExpr();
    }

    private ConstraintExpr memberExpr() {
	    var callee = primaryExpr();

	    if (tokens.peek() instanceof Token.Dot) {
		    pop(Token.Dot.class);

		    var member = memberExpr();

		    if (!(callee instanceof ConstraintExpr.Variable)) {
			    throw new IllegalStateException("Expected member expression on variable, got: " + callee);
		    }
		    if (!(member instanceof ConstraintExpr.Member || member instanceof ConstraintExpr.Variable)) {
			    throw new IllegalStateException("Expected member expression or variable, got: " + member);
		    }

		    return new ConstraintExpr.Member((ConstraintExpr.Variable) callee, member);
	    } else {
		    return callee;
	    }
    }

    private ConstraintExpr primaryExpr() {
	    if (tokens.peek() instanceof Token.Number) {
	    } else if (tokens.peek() instanceof Token.String) {
	    } else if (tokens.peek() instanceof Token.LeftParen) {
            pop(Token.LeftParen.class);

            var expr = constraintExpr();

            pop(Token.RightParen.class);

            return new ConstraintExpr.Grouping(expr);
	    } else if (tokens.peek() instanceof Token.LeftBrace) {
	    } else if (tokens.peek() instanceof Token.Identifier) {
		    var identifier = getIdentifierName(tokens.pop());
		    return new ConstraintExpr.Variable(identifier);
	    } else if (tokens.peek() instanceof Token.Symbol) {
		    var symbol = getSymbolName(tokens.pop());
		    return new ConstraintExpr.Symbol(symbol);
	    } else if (tokens.peek() instanceof Token.Forall) {
		    return forallExpr();
	    }
	    throw new IllegalStateException("Expected expression, got: " + tokens.peek());
    }

    private ConstraintExpr forallExpr() {
	    pop(Token.Forall.class);
	    var members = new ArrayList<ConstraintExpr>();
	    while (!(tokens.peek() instanceof Token.In)) {
		    var member = constraintExpr();
		    if (tokens.peek() instanceof Token.Comma) {
			    pop(Token.Comma.class);
		    }
		    members.add(member);
	    }
	    pop(Token.In.class);
	    var set = constraintExpr();
	    pop(Token.Colon.class);
	    var predicate = constraintExpr();
	    return new ConstraintExpr.Forall(new ConstraintExpr.Tuple(members), (ConstraintExpr.Symbol) set, predicate);
    }

    private <T extends Token> void pop(Class<T> type) {
        var tok = tokens.pop();
        if (!type.isInstance(tok)) {
            throw new IllegalStateException("Expected to pop " + type + " but got: " + tok);
        }
    }

    private static String getIdentifierName(Token identifier) {
        if (identifier instanceof Token.Identifier) {
            return ((Token.Identifier) identifier).name();
        } else {
            throw new IllegalStateException("Expected an identifier, got: " + identifier);
        }
    }

    private static String getSymbolName(Token symbol) {
        if (symbol instanceof Token.Symbol) {
            return ((Token.Symbol) symbol).name();
        } else {
            throw new IllegalStateException("Expected a type, got: " + symbol);
        }
    }
}
