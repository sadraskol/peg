package com.sadraskol.peg.engine;

import com.sadraskol.peg.TestUtils;
import com.sadraskol.peg.parser.Parser;
import com.sadraskol.peg.scanner.Scanner;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EngineTest {

  @Test
  void simpleSpecTest() {
    var source = TestUtils.readFile("engine/simple_spec.peg");
    var scanner = new Scanner(source);
    var parser = new Parser(scanner.scan());
    var engine = new Engine(parser.parse());

    Assertions.assertEquals(
        List.of(
            new Proposition.Primary(new BinaryOp.Equal(new Set.Named("Room"), new Set.Universe())),
            new Proposition.Primary(
                new BinaryOp.Equal(
                    new Set.Named("Room#teacher"),
                    new Set.Product(new Set.Named("Room"), new Set.Named("Teacher")))),
            new Proposition.Primary(
                new BinaryOp.Equal(new Set.Named("Teacher"), new Set.Universe())),
            new Proposition.Primary(
                new BinaryOp.Equal(
                    new Set.Named("Room"),
                    new Set.Literal(List.of(new Value.Str("Room A"), new Value.Str("Room B"))))),
            new Proposition.Primary(
                new BinaryOp.Equal(
                    new Set.Named("Teacher"),
                    new Set.Literal(List.of(new Value.Str("Gerber"), new Value.Str("Damasio"))))),
            new Proposition.Forall(
                List.of(new Value.Variable("t")),
                new Value.NamedSet("Teacher"),
                new Proposition.Exists(
                    List.of(new Value.Variable("r")),
                    new Value.NamedSet("Room"),
                    new Proposition.Primary(
                        new BinaryOp.In(
                            new Value.Tuple(
                                List.of(new Value.Variable("r"), new Value.Variable("t"))),
                            new Value.NamedSet("Room#teacher"))))),
            new Proposition.Forall(
                List.of(new Value.Variable("r")),
                new Value.NamedSet("Room"),
                new Proposition.Exists(
                    List.of(new Value.Variable("t")),
                    new Value.NamedSet("Teacher"),
                    new Proposition.Primary(
                        new BinaryOp.In(
                            new Value.Tuple(
                                List.of(new Value.Variable("r"), new Value.Variable("t"))),
                            new Value.NamedSet("Room#teacher")))))),
        engine.propositions());
  }
}
