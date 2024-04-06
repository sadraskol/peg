package com.sadraskol.peg;

import com.sadraskol.peg.backend.Evaluator;
import com.sadraskol.peg.engine.Engine;
import com.sadraskol.peg.engine.Proposition;
import com.sadraskol.peg.parser.Parser;
import com.sadraskol.peg.scanner.Scanner;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import java.util.*;

public class TreeExplorer {
    public static void main(String[] args) throws ContradictionException, TimeoutException {
        var source = TestUtils.readFile("engine/simple_spec.peg");
        var scanner = new Scanner(source);
        var parser = new Parser(scanner.scan());
        var engine = new Engine(parser.parse());
        var evaluator = new Evaluator();

        var propositions = new ArrayList<Proposition>();
        for (var proposition : engine.propositions()) {
            var res = evaluator.evaluate(proposition);
            propositions.addAll(res.conjunctiveNormalForm().splitConjonctiveNormalForm().toList());
        }

        var specs = propositions.stream().filter(p -> !(p instanceof Proposition.True)).toList();
        System.out.println(specs);

        var termSet = new LinkedHashSet<Proposition>();
        for (var phrase : specs) {
            termSet.addAll(phrase.terms());
        }

        var terms = termSet.stream().toList();
        System.out.println(terms);

        var sat = satTranslate(specs, terms);

        System.out.println(Arrays.toString(executeSat(terms.size(), sat)));
    }

    private static int[] executeSat(int terms, List<int[]> sat) throws TimeoutException, ContradictionException {
        System.out.println(sat.stream().map(Arrays::toString).toList());
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
            vecs.add(proposition.substituteTerms(terms).stream().mapToInt(Integer::intValue).toArray());
        }
        return vecs;
    }
}
