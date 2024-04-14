package com.sadraskol.peg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sadraskol.peg.engine.Set;
import com.sadraskol.peg.engine.Value;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

public class RunnerTest {
  @Test
  public void worksOnSimpleSpec() throws ContradictionException, TimeoutException {
    var model = new Runner(TestUtils.readFile("engine/simple_spec.peg")).run();

    assertEquals(
        new Set.Literal(List.of(new Value.Str("Gerber"), new Value.Str("Damasio"))),
        model.get("Teacher"));

    assertEquals(
        new Set.Literal(List.of(new Value.Str("Room A"), new Value.Str("Room B"))),
        model.get("Room"));
    assertEquals(
        new Set.Literal(
            List.of(
                new Value.Tuple(List.of(new Value.Str("Room B"), new Value.Str("Gerber"))),
                new Value.Tuple(List.of(new Value.Str("Room A"), new Value.Str("Damasio"))))),
        model.get("Room#teacher"));
  }
}
