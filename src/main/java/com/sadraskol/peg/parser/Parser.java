package com.sadraskol.peg.parser;

import com.sadraskol.peg.scanner.Token;
import com.sadraskol.peg.scanner.TokenType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Parser {
  private final Deque<Token> tokens;
  private final List<Declaration> declarations;

  public Parser(List<Token> tokens) {
    this.tokens = new ArrayDeque<>(tokens);
    this.declarations = new ArrayList<>();
  }

  public List<Declaration> parse() {
    while (!(tokens.getFirst().type() == TokenType.Eof)) {
      if (tokens.getFirst().type() == TokenType.Import) {
        parseImportStatement();
      } else if (tokens.getFirst().type() == TokenType.Record) {
        parseRecordStatement();
      } else if (tokens.getFirst().type() == TokenType.Constraint) {
        parseConstraintStatement();
      } else if (tokens.getFirst().type() == TokenType.Facts) {
        parseFactsStatement();
      } else {
        throw new IllegalStateException(
            "Unexpected token " + tokens.getFirst() + ", expected a statement");
      }
    }
    return declarations;
  }

  private void parseImportStatement() {
    pop(TokenType.Import);
    var labels = new ArrayList<String>();
    String name;
    loop:
    while (true) {
      var identifier = tokens.pop();
      switch (identifier.type()) {
        case TokenType.Identifier -> labels.add(identifier.content());
        case TokenType.Symbol -> {
          name = identifier.content();
          break loop;
        }
        default ->
            throw new IllegalStateException("Unexpected token type for an import: " + identifier);
      }
      pop(TokenType.Dot);
    }
    if (name == null) {
      throw new IllegalStateException("Unexpected end of import statement: expected a Symbol");
    }
    declarations.add(new Declaration.Import(new QualifiedName(labels, name)));
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

    declarations.add(new Declaration.Record(name, members, relations));
  }

  private void parseConstraintStatement() {
    pop(TokenType.Constraint);
    pop(TokenType.LeftBrace);

    var expr = constraintExpr();

    pop(TokenType.RightBrace);

    declarations.add(new Declaration.Constraint(expr));
  }

  private void parseFactsStatement() {
    pop(TokenType.Facts);
    pop(TokenType.LeftBrace);

    var expressions = new ArrayList<Expression>();

    while (tokens.getFirst().type() != TokenType.RightBrace) {
      expressions.add(constraintExpr());
    }

    pop(TokenType.RightBrace);

    declarations.add(new Declaration.Facts(expressions));
  }

  private Expression constraintExpr() {
    return assignmentExpr();
  }

  private Expression assignmentExpr() {
    var left = impliesExpr();

    if (tokens.peek().type() == TokenType.Equal) {
      pop(TokenType.Equal);

      var right = assignmentExpr();

      return new Expression.Equal(left, right);
    } else {
      return left;
    }
  }

  private Expression impliesExpr() {
    var left = orExpr();

    if (tokens.peek().type() == TokenType.Implies) {
      pop(TokenType.Implies);

      var right = impliesExpr();

      return new Expression.Implies(left, right);
    } else {
      return left;
    }
  }

  private Expression orExpr() {
    var left = andExpr();

    if (tokens.peek().type() == TokenType.Or) {
      pop(TokenType.Or);

      var right = orExpr();

      return new Expression.Or(left, right);
    } else {
      return left;
    }
  }

  private Expression andExpr() {
    var left = equalityExpr();

    if (tokens.peek().type() == TokenType.And) {
      pop(TokenType.And);

      var right = andExpr();

      return new Expression.And(left, right);
    } else {
      return left;
    }
  }

  private Expression equalityExpr() {
    var left = unaryExpr();

    if (tokens.peek().type() == TokenType.EqualEqual) {
      pop(TokenType.EqualEqual);

      var right = equalityExpr();

      return new Expression.Equal(left, right);
    } else if (tokens.peek().type() == TokenType.BangEqual) {
      pop(TokenType.BangEqual);

      var right = equalityExpr();

      return new Expression.NotEqual(left, right);
    } else {
      return left;
    }
  }

  private Expression unaryExpr() {
    return memberExpr();
  }

  private Expression memberExpr() {
    var callee = primaryExpr();

    if (tokens.peek().type() == TokenType.Dot) {
      pop(TokenType.Dot);

      var member = memberExpr();

      if (!(callee instanceof Expression.Variable || callee instanceof Expression.Symbol)) {
        throw new IllegalStateException("Expected member expression on variable, got: " + callee);
      }
      if (!(member instanceof Expression.Member || member instanceof Expression.Variable)) {
        throw new IllegalStateException("Expected member expression or variable, got: " + member);
      }

      if (tokens.peek().type() == TokenType.LeftParen) {
        pop(TokenType.LeftParen);
        var args = new ArrayList<Expression>();
        do {
          if (tokens.peek().type() == TokenType.Comma) {
            pop(TokenType.Comma);
          }
          if (tokens.peek().type() == TokenType.RightParen) {
            break;
          }

          args.add(constraintExpr());

        } while (tokens.peek().type() == TokenType.Comma);
        pop(TokenType.RightParen);
        return new Expression.Call(callee, member, args);
      }

      return new Expression.Member((Expression.Variable) callee, member);
    } else {
      return callee;
    }
  }

  private Expression primaryExpr() {
    if (tokens.peek().type() == TokenType.Number) {
      var str = tokens.pop().content();
      return new Expression.Number(Integer.parseInt(str));
    } else if (tokens.peek().type() == TokenType.String) {
      var str = tokens.pop().content();
      return new Expression.String(str.substring(1, str.length() - 1));
    } else if (tokens.peek().type() == TokenType.LeftParen) {
      pop(TokenType.LeftParen);

      var exprs = new ArrayList<Expression>();
      do {
        if (tokens.peek().type() == TokenType.Comma) {
          pop(TokenType.Comma);
        }
        if (tokens.peek().type() == TokenType.RightParen) {
          break;
        }

        exprs.add(constraintExpr());

      } while (tokens.peek().type() == TokenType.Comma);
      pop(TokenType.RightParen);

      if (exprs.size() == 1) {
        return new Expression.Grouping(exprs.getFirst());
      } else {
        return new Expression.Tuple(exprs);
      }
    } else if (tokens.peek().type() == TokenType.LeftBrace) {
      pop(TokenType.LeftBrace);

      var exprs = new ArrayList<Expression>();
      do {
        if (tokens.peek().type() == TokenType.Comma) {
          pop(TokenType.Comma);
        }
        if (tokens.peek().type() == TokenType.RightBrace) {
          break;
        }

        exprs.add(constraintExpr());

      } while (tokens.peek().type() == TokenType.Comma);
      pop(TokenType.RightBrace);

      return new Expression.Set(exprs);
    } else if (tokens.peek().type() == TokenType.Identifier) {
      var identifier = getIdentifierName(tokens.pop());
      return new Expression.Variable(identifier);
    } else if (tokens.peek().type() == TokenType.Symbol) {
      var symbol = getSymbolName(tokens.pop());
      return new Expression.Symbol(symbol);
    } else if (tokens.peek().type() == TokenType.Exists) {
      return existsExpr();
    } else if (tokens.peek().type() == TokenType.Forall) {
      return forallExpr();
    }
    throw new IllegalStateException("Expected expression, got: " + tokens.peek());
  }

  private Expression forallExpr() {
    pop(TokenType.Forall);
    var members = new ArrayList<Expression>();
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
    return new Expression.Forall(new Expression.Tuple(members), (Expression.Symbol) set, predicate);
  }

  private Expression existsExpr() {
    pop(TokenType.Exists);
    var members = new ArrayList<Expression>();
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
    return new Expression.Exists(new Expression.Tuple(members), (Expression.Symbol) set, predicate);
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
