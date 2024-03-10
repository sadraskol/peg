package com.sadraskol.peg;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import java.util.Arrays;

public class Sat4jExample {
    public static void main(String[] args) throws ContradictionException, TimeoutException {
        var minisat = org.sat4j.minisat.SolverFactory.newDefault();

        minisat.newVar(3);

        minisat.addClause(new VecInt(new int[]{1, 2, -3}));
        minisat.addClause(new VecInt(new int[]{-2, 3}));

        VecInt assumptions = new VecInt(new int[]{-1, -2, 3});
        if (minisat.isSatisfiable(assumptions)) {
            System.out.println("model: " + Arrays.toString(minisat.model()));
        } else {
            System.out.println("Unsatisfiable assumptions: " + assumptions);
        }
    }
}
