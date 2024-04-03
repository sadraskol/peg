package com.sadraskol.peg;

import com.sadraskol.peg.backend.Evaluator;
import com.sadraskol.peg.engine.Engine;
import com.sadraskol.peg.parser.Parser;
import com.sadraskol.peg.scanner.Scanner;

public class TreeExplorer {
    public static void main(String[] args) {
        var source = TestUtils.readFile("engine/simple_spec.peg");
        var scanner = new Scanner(source);
        var parser = new Parser(scanner.scan());
        var engine = new Engine(parser.parse());
        var evaluator = new Evaluator();

        for (var proposition : engine.propositions()) {
            System.out.println("Evaluating: " + proposition);
            var res = evaluator.evaluate(proposition);
            System.out.println("\tResult: " + res);
        }
        System.out.println(evaluator.getVariables());
        System.out.println(evaluator.getPropositions());
    }
}
