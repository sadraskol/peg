package com.sadraskol.peg;

import java.util.Arrays;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

public class Sat4jExample {
  public static void main(String[] args) throws ContradictionException, TimeoutException {
    var minisat = org.sat4j.minisat.SolverFactory.newDefault();

    minisat.newVar(4);

    minisat.addClause(new VecInt(new int[] {1, 2}));
    minisat.addClause(new VecInt(new int[] {3, 4}));
    minisat.addClause(new VecInt(new int[] {1, 3}));
    minisat.addClause(new VecInt(new int[] {2, 4}));

    if (minisat.isSatisfiable()) {
      System.out.println("model: " + Arrays.toString(minisat.model()));
    } else {
      System.out.println("Unsatisfiable model");
    }
  }
}
