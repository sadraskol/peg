package com.sadraskol.peg.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class PropositionTest {
  @Nested
  class ConjunctiveNormalForm {
    // (x /\ y) \/ z <=> (x \/ z) /\ (y \/ z)
    @Test
    void distributeOnTheLeft() {
      var x = new Proposition.Binary(Operator.Equal, new Value.Str("x"), new Value.Str("x"));
      var y = new Proposition.Binary(Operator.Equal, new Value.Str("y"), new Value.Str("y"));
      var z = new Proposition.Binary(Operator.Equal, new Value.Str("z"), new Value.Str("z"));

      assertEquals(
          new Proposition.And(new Proposition.Or(x, z), new Proposition.Or(y, z)),
          new Proposition.Or(new Proposition.And(x, y), z).conjunctiveNormalForm());
    }

    //  z \/ (x /\ y) <=> (z \/ x) /\ (z \/ y)
    @Test
    void distributeOnTheRight() {
      var x = new Proposition.Binary(Operator.Equal, new Value.Str("x"), new Value.Str("x"));
      var y = new Proposition.Binary(Operator.Equal, new Value.Str("y"), new Value.Str("y"));
      var z = new Proposition.Binary(Operator.Equal, new Value.Str("z"), new Value.Str("z"));

      assertEquals(
          new Proposition.And(new Proposition.Or(z, x), new Proposition.Or(z, y)),
          new Proposition.Or(z, new Proposition.And(x, y)).conjunctiveNormalForm());
    }

    // (x /\ y) \/ (z /\ w) <=> (x \/ (z /\ w)) /\ (y \/ (z /\ w))
    //                      <=> (x \/ z) /\ (x /\ w) /\ (y \/ z) /\ (y /\ w)
    @Test
    void distributeOnBothSide() {
      var x = new Proposition.Binary(Operator.Equal, new Value.Str("x"), new Value.Str("x"));
      var y = new Proposition.Binary(Operator.Equal, new Value.Str("y"), new Value.Str("y"));
      var z = new Proposition.Binary(Operator.Equal, new Value.Str("z"), new Value.Str("z"));
      var w = new Proposition.Binary(Operator.Equal, new Value.Str("w"), new Value.Str("w"));

      assertEquals(
          new Proposition.And(
              new Proposition.And(new Proposition.Or(x, z), new Proposition.Or(x, w)),
              new Proposition.And(new Proposition.Or(y, z), new Proposition.Or(y, w))),
          new Proposition.Or(new Proposition.And(x, y), new Proposition.And(z, w))
              .conjunctiveNormalForm());
    }

    // (x /\ (y \/ z)) \/ w <=> ((x \/ y) /\ (x \/ z)) \/ w
    //                      <=> (x \/ y \/ w) /\ (x \/ z \/ w)
    @Test
    void nestedDistributionOnLeft() {
      var x = new Proposition.Binary(Operator.Equal, new Value.Str("x"), new Value.Str("x"));
      var y = new Proposition.Binary(Operator.Equal, new Value.Str("y"), new Value.Str("y"));
      var z = new Proposition.Binary(Operator.Equal, new Value.Str("z"), new Value.Str("z"));
      var w = new Proposition.Binary(Operator.Equal, new Value.Str("w"), new Value.Str("w"));

      assertEquals(
          new Proposition.And(
              new Proposition.Or(new Proposition.Or(x, y), w),
              new Proposition.Or(new Proposition.Or(x, z), w)),
          new Proposition.Or(new Proposition.And(x, new Proposition.Or(y, z)), w)
              .conjunctiveNormalForm());
    }
  }

  @Nested
  class SplitConjunctiveNormalForm {
    @Test
    void splitNestedConjunctions() {
      var x = new Proposition.Binary(Operator.Equal, new Value.Str("x"), new Value.Str("x"));
      var y = new Proposition.Binary(Operator.Equal, new Value.Str("y"), new Value.Str("y"));
      var z = new Proposition.Binary(Operator.Equal, new Value.Str("z"), new Value.Str("z"));
      var w = new Proposition.Binary(Operator.Equal, new Value.Str("w"), new Value.Str("w"));

      assertEquals(
          List.of(
              new Proposition.Or(x, z),
              new Proposition.Or(x, w),
              new Proposition.Or(y, z),
              new Proposition.Or(y, w)),
          new Proposition.And(
                  new Proposition.And(new Proposition.Or(x, z), new Proposition.Or(x, w)),
                  new Proposition.And(new Proposition.Or(y, z), new Proposition.Or(y, w)))
              .splitConjonctiveNormalForm()
              .toList());
    }
  }
}
