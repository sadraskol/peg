package com.sadraskol.peg;

import com.sadraskol.peg.backend.Evaluator;
import com.sadraskol.peg.engine.Engine;
import com.sadraskol.peg.engine.Proposition;
import com.sadraskol.peg.engine.Set;
import com.sadraskol.peg.parser.Parser;
import com.sadraskol.peg.scanner.Scanner;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

public record Runner(String source) {

  public Map<String, Set> run() throws ContradictionException, TimeoutException {
    var scanner = new Scanner(source);
    var parser = new Parser(scanner.scan());
    var engine = new Engine(parser.parse());
    var evaluator = new Evaluator();

    // evaluate and cnf
    var specs = new ArrayList<Proposition>();
    for (var proposition : engine.propositions()) {
      var res = evaluator.evaluate(proposition);
      specs.addAll(res.conjunctiveNormalForm().splitConjonctiveNormalForm().toList());
    }

    // list terms
    var termSet = new LinkedHashSet<Proposition>();
    for (var phrase : specs) {
      termSet.addAll(phrase.terms());
    }
    var terms = termSet.stream().toList();

    // Run Sat
    var sat = satTranslate(specs, terms);
    int[] satResult = executeSat(terms.size(), sat);

    // Spit the model
    for (var term : satResult) {
      var proposition = terms.get(Math.abs(term) - 1);
      if (term < 0) {
        evaluator.evaluate(new Proposition.Not(proposition), true);
      } else {
        evaluator.evaluate(proposition, true);
      }
    }
    return evaluator.reify();
  }

  private static int[] executeSat(int terms, List<int[]> sat)
      throws TimeoutException, ContradictionException {
    var minisat = org.sat4j.minisat.SolverFactory.newDefault();

    minisat.newVar(terms);
    for (var a : sat) {
      minisat.addClause(new VecInt(a));
    }

    if (minisat.isSatisfiable()) {
      return minisat.model();
    } else {
      throw new IllegalStateException("The model is not satisfiable");
    }
  }

  private static List<int[]> satTranslate(List<Proposition> specs, List<Proposition> terms) {
    var vecs = new ArrayList<int[]>();
    for (var proposition : specs) {
      var substitute = proposition.substituteTerms(terms);
      if (!substitute.isEmpty()) {
        vecs.add(proposition.substituteTerms(terms).stream().mapToInt(Integer::intValue).toArray());
      }
    }
    return vecs;
  }
}
