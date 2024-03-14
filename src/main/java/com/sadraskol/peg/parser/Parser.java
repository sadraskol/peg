package com.sadraskol.peg.parser;

import com.sadraskol.peg.scanner.Token;
import com.sadraskol.peg.scanner.TokenType;
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
    while (!(tokens.getFirst().type() == TokenType.Eof)) {
      if (tokens.getFirst().type() == TokenType.Import) {
        parseImportStatement();
      } else if (tokens.getFirst().type() == TokenType.Record) {
        parseRecordStatement();
      } else if (tokens.getFirst().type() == TokenType.Constraint) {
        parseConstraintStatement();
      } else {
        throw new IllegalStateException(
            "Unexpected token " + tokens.getFirst() + ", expected a statement");
      }
    }
    return statements;
  }

  private void parseImportStatement() {
    pop(TokenType.Import);
    var domain = new ArrayList<String>();
    loop:
    while (true) {
      var identifier = tokens.pop();
      switch (identifier.type()) {
        case TokenType.Identifier -> domain.add(identifier.content());
        case TokenType.Symbol -> {
          domain.add(identifier.content());
          break loop;
        }
        default ->
            throw new IllegalStateException("Unexpected token type for an import: " + identifier);
      }
      pop(TokenType.Dot);
    }
    statements.add(new Statement.Import(domain));
  }

  private void parseRecordStatement() {
    pop(TokenType.Record);
    var name = getSymbolName(tokens.pop());

    pop(TokenType.LeftParen);
    var members = new ArrayList<RecordMember>();
    while (!(tokens.peek().type() == TokenType.RightParen)) {
      var isIdentity = false;
      if (tokens.peek().type() == TokenType.Identity) {
        isIdentity = true;
        pop(TokenType.Identity);
      }
      var memberName = getIdentifierName(tokens.pop());
      pop(TokenType.Colon);
      var memberType = getSymbolName(tokens.pop());
      if (tokens.peek().type() == TokenType.Comma) {
        pop(TokenType.Comma);
      }
      members.add(new RecordMember(isIdentity, memberName, memberType));
    }
    pop(TokenType.RightParen);
    pop(TokenType.LeftBrace);

    var relations = new ArrayList<RecordRelation>();
    while (!(tokens.peek().type() == TokenType.RightBrace)) {
      var isInjective = false;
      if (tokens.peek().type() == TokenType.Injective) {
        isInjective = true;
        pop(TokenType.Injective);
      }

      pop(TokenType.Relation);

      var memberName = getIdentifierName(tokens.pop());
      pop(TokenType.Colon);

      var memberType = getSymbolName(tokens.pop());
      relations.add(new RecordRelation(isInjective, memberName, memberType));
    }
    pop(TokenType.RightBrace);

    statements.add(new Statement.Record(name, members, relations));
  }

  private void parseConstraintStatement() {
    pop(TokenType.Constraint);
    pop(TokenType.LeftBrace);

    var expr = constraintExpr();

    pop(TokenType.RightBrace);

    statements.add(new Statement.Constraint(expr));
  }

  private ConstraintExpr constraintExpr() {
    return impliesExpr();
  }

  private ConstraintExpr impliesExpr() {
    var left = orExpr();

    if (tokens.peek().type() == TokenType.Implies) {
      pop(TokenType.Implies);

      var right = impliesExpr();

      return new ConstraintExpr.Implies(left, right);
    } else {
      return left;
    }
  }

  private ConstraintExpr orExpr() {
    var left = andExpr();

    // if (tokens.peek().type() == TokenType.Or) {
    //         pop(TokenType.Or);

    //         var right = orExpr();

    //         return new ConstraintExpr.Or(left, right);
    // } else {
    return left;
    // }
  }

  private ConstraintExpr andExpr() {
    var left = equalityExpr();

    if (tokens.peek().type() == TokenType.And) {
      pop(TokenType.And);

      var right = andExpr();

      return new ConstraintExpr.And(left, right);
    } else {
      return left;
    }
  }

  private ConstraintExpr equalityExpr() {
    var left = unaryExpr();

    if (tokens.peek().type() == TokenType.EqualEqual) {
      pop(TokenType.EqualEqual);

      var right = equalityExpr();

      return new ConstraintExpr.Equal(left, right);
    } else if (tokens.peek().type() == TokenType.BangEqual) {
      pop(TokenType.BangEqual);

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

    if (tokens.peek().type() == TokenType.Dot) {
      pop(TokenType.Dot);

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
    if (tokens.peek().type() == TokenType.Number) {
    } else if (tokens.peek().type() == TokenType.String) {
    } else if (tokens.peek().type() == TokenType.LeftParen) {
      pop(TokenType.LeftParen);

      var expr = constraintExpr();

      pop(TokenType.RightParen);

      return new ConstraintExpr.Grouping(expr);
    } else if (tokens.peek().type() == TokenType.LeftBrace) {
    } else if (tokens.peek().type() == TokenType.Identifier) {
      var identifier = getIdentifierName(tokens.pop());
      return new ConstraintExpr.Variable(identifier);
    } else if (tokens.peek().type() == TokenType.Symbol) {
      var symbol = getSymbolName(tokens.pop());
      return new ConstraintExpr.Symbol(symbol);
    } else if (tokens.peek().type() == TokenType.Forall) {
      return forallExpr();
    }
    throw new IllegalStateException("Expected expression, got: " + tokens.peek());
  }

  private ConstraintExpr forallExpr() {
    pop(TokenType.Forall);
    var members = new ArrayList<ConstraintExpr>();
    while (!(tokens.peek().type() == TokenType.In)) {
      var member = constraintExpr();
      if (tokens.peek().type() == TokenType.Comma) {
        pop(TokenType.Comma);
      }
      members.add(member);
    }
    pop(TokenType.In);
    var set = constraintExpr();
    pop(TokenType.Colon);
    var predicate = constraintExpr();
    return new ConstraintExpr.Forall(
        new ConstraintExpr.Tuple(members), (ConstraintExpr.Symbol) set, predicate);
  }

  private void pop(TokenType type) {
    var tok = tokens.pop();
    if (tok.type() != type) {
      throw new IllegalStateException("Expected to pop " + type + " but got: " + tok);
    }
  }

  private static String getIdentifierName(Token identifier) {
    if (identifier.type() == TokenType.Identifier) {
      return identifier.content();
    } else {
      throw new IllegalStateException("Expected an identifier, got: " + identifier);
    }
  }

  private static String getSymbolName(Token symbol) {
    if (symbol.type() == TokenType.Symbol) {
      return symbol.content();
    } else {
      throw new IllegalStateException("Expected a type, got: " + symbol);
    }
  }
}
