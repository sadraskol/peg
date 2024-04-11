package com.sadraskol.peg.engine;

import com.sadraskol.peg.TestUtils;
import com.sadraskol.peg.parser.Parser;
import com.sadraskol.peg.scanner.Scanner;

public class SimpleSpecEngineTest {
  private final Engine engine;

  SimpleSpecEngineTest() {
    var source = TestUtils.readFile("engine/simple_spec.peg");
    var scanner = new Scanner(source);
    var parser = new Parser(scanner.scan());
    engine = new Engine(parser.parse());
  }
  //
  //    @Test
  //    void resolveQualifiedNames() {
  //        assertThrows(RuntimeException.class, () -> engine.resolve("String"));
  //
  //        var imports = engine.imports();
  //        assertEquals(List.of(new QualifiedName(List.of("peg", "lang"), "String")), imports);
  //
  //        assertEquals(new QualifiedName(List.of("peg", "lang"), "String"),
  // engine.resolve("String"));
  //    }
  //
  //    @Test
  //    void generateRecords() {
  //        engine.imports();
  //        var records = engine.records();
  //
  //        assertEquals(List.of(new PegRecord("Room", List.of(new QualifiedName(List.of("peg",
  // "lang"), "String"))),
  //                new PegRecord("Teacher", List.of(new QualifiedName(List.of("peg", "lang"),
  // "String")))), records);
  //    }

  //    @Test
  //    void stateFacts() {
  //        engine.imports();
  //        engine.records();
  //
  //        assertEquals(Map.of("Room", new SetValue.Universe(), "Room#teacher", new
  // SetValue.Universe(), "Teacher", new SetValue.Universe()), engine.sets());
  //
  //        engine.facts();
  //
  //        assertEquals(new SetValue.Universe(), engine.sets().get("Room#teacher"));
  //        assertEquals(new SetValue.Set(List.of("Room A", "Room B")), engine.sets().get("Room"));
  //        assertEquals(new SetValue.Set(List.of("Gerber", "Damasio")),
  // engine.sets().get("Teacher"));
  //    }
  //
  //    @Test
  //    void translateConstraints() {
  //        engine.imports();
  //        engine.records();
  //        engine.facts();
  //
  //        engine.evaluateConstraints();
  //
  //        assertEquals(List.of(
  //                new BinaryOp.In("Room#teacher", new Value.Tuple(List.of("Room A", "Gerber"))),
  //                new BinaryOp.In("Room#teacher", new Value.Tuple(List.of("Room B", "Gerber"))),
  //                new BinaryOp.In("Room#teacher", new Value.Tuple(List.of("Room A", "Damasio"))),
  //                new BinaryOp.In("Room#teacher", new Value.Tuple(List.of("Room B", "Damasio")))
  //        ), engine.propositions());
  //
  //        assertEquals(List.of(
  //                List.of(0, 1),
  //                List.of(2, 3),
  //                List.of(0, 2),
  //                List.of(1, 3)
  //        ), engine.constraints());
  //    }
}
