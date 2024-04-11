package com.sadraskol.peg;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

public class TreeExplorer {
  public static void main(String[] args) throws ContradictionException, TimeoutException {
    System.out.println(new Runner(TestUtils.readFile("class_attribution.peg")).run());
  }
}
