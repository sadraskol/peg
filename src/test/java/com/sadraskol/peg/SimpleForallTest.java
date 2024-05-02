package com.sadraskol.peg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sadraskol.peg.backend.Evaluator;
import com.sadraskol.peg.engine.Engine;
import com.sadraskol.peg.engine.Proposition;
import com.sadraskol.peg.parser.Parser;
import com.sadraskol.peg.scanner.Scanner;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SimpleForallTest {
  @Test
  void whatTheHellIsGoingOn() {
    var scanner = new Scanner(TestUtils.readFile("forall_and_record_member.peg"));
    var parser = new Parser(scanner.scan());
    var engine = new Engine(parser.parse());
    var evaluator = new Evaluator();

    // evaluate and cnf
    var specs = new ArrayList<Proposition>();
    for (var proposition : engine.propositions()) {
      var res = evaluator.evaluate(proposition);
      specs.addAll(res.conjunctiveNormalForm().splitConjonctiveNormalForm().toList());
    }

    assertEquals(
        List.of(
            new Proposition.True(),
            new Proposition.True(),
            new Proposition.True(),
            new Proposition.True(),
            new Proposition.True()),
        specs);
  }
}
