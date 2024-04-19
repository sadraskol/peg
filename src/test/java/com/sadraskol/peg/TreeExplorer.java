package com.sadraskol.peg;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

public class TreeExplorer {
  public static void main() throws ContradictionException, TimeoutException {
    System.out.println(new Runner(TestUtils.readFile("engine/less_class_attribution.peg")).run());
  }
}
