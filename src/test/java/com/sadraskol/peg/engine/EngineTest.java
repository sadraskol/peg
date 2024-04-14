package com.sadraskol.peg.engine;

import com.sadraskol.peg.TestUtils;
import com.sadraskol.peg.parser.Declaration;
import com.sadraskol.peg.parser.Parser;
import com.sadraskol.peg.parser.ParserTestCase;
import com.sadraskol.peg.scanner.Scanner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class EngineTest {
    @ParameterizedTest
    @MethodSource("engineTestCases")
    void engineTestCase(String filename, List<Proposition> expectedPropositions) {
        var source = TestUtils.readFile(filename);
        var scanner = new Scanner(source);
        var parser = new Parser(scanner.scan());
        var engine = new Engine(parser.parse());

        Assertions.assertEquals(expectedPropositions, engine.propositions());
    }

    @Test
    void allStatementsAreIncludedInTheTests() {
        var testStatements =
                getArgumentsStream()
                        .map(EngineTestCase::expectedPropositions)
                        .flatMap(Collection::stream)
                        .map(Proposition::getClass)
                        .sorted(Comparator.comparing(Class::toString))
                        .distinct()
                        .toList();

        var implementedPropositions =
                Arrays.stream(Proposition.class.getPermittedSubclasses())
                        .sorted(Comparator.comparing(Class::toString))
                        .toList();

        Assertions.assertEquals(implementedPropositions, testStatements);
    }

    private static Stream<Arguments> engineTestCases() {
        return getArgumentsStream().map(EngineTestCase::toPair);
    }

    private static Stream<EngineTestCase> getArgumentsStream() {
        return Stream.of(
                new EngineTestCase("engine/class_attribution.peg",
                        List.of(
                                new Proposition.Binary(Operator.Equal, new Value.Set(new Set.Named("Lesson")), new Value.Set(new Set.Universe())),
                                new Proposition.Binary(
                                        Operator.Equal,
                                        new Value.Set(new Set.Named("Lesson#slot")),
                                        new Value.Set(new Set.Product(new Value.Set(new Set.Named("Lesson")), new Value.Set(new Set.Named("Slot"))))),
                                new Proposition.Binary(
                                        Operator.Equal,
                                        new Value.Set(new Set.Named("Lesson#room")),
                                        new Value.Set(new Set.Product(new Value.Set(new Set.Named("Lesson")), new Value.Set(new Set.Named("Room"))))),
                                new Proposition.Binary(
                                        Operator.Equal, new Value.Set(new Set.Named("Room")), new Value.Set(new Set.Universe())),
                                new Proposition.Binary(
                                        Operator.Equal, new Value.Set(new Set.Named("Slot")), new Value.Set(new Set.Universe())),
                                new Proposition.Binary(
                                        Operator.Equal,
                                        new Value.Set(new Set.Named("Room")),
                                        new Value.Set(new Set.Literal(List.of(new Value.Str("Room A"), new Value.Str("Room B"))))),
                                new Proposition.Binary(
                                        Operator.Equal,
                                        new Value.Set(new Set.Named("Slot")),
                                        new Value.Set(new Set.Literal(List.of(
                                                new Value.Tuple(List.of(new Value.Str("Monday"), new Value.Tuple(List.of(new Value.Number(8), new Value.Number(30))), new Value.Tuple(List.of(new Value.Number(9), new Value.Number(30))))),
                                                new Value.Tuple(List.of(new Value.Str("Monday"), new Value.Tuple(List.of(new Value.Number(9), new Value.Number(30))), new Value.Tuple(List.of(new Value.Number(10), new Value.Number(30))))),
                                                new Value.Tuple(List.of(new Value.Str("Tuesday"), new Value.Tuple(List.of(new Value.Number(8), new Value.Number(30))), new Value.Tuple(List.of(new Value.Number(9), new Value.Number(30))))),
                                                new Value.Tuple(List.of(new Value.Str("Tuesday"), new Value.Tuple(List.of(new Value.Number(9), new Value.Number(30))), new Value.Tuple(List.of(new Value.Number(10), new Value.Number(30)))))
                                        )))),
                                new Proposition.Binary(
                                        Operator.Equal,
                                        new Value.Set(new Set.Named("Lesson")),
                                        new Value.Set(new Set.Literal(List.of(
                                                new Value.Tuple(List.of(new Value.Str("Math"), new Value.Str("A. Turing"), new Value.Str("9th grade"))),
                                                new Value.Tuple(List.of(new Value.Str("Math"), new Value.Str("A. Turing"), new Value.Str("9th grade"))),
                                                new Value.Tuple(List.of(new Value.Str("Biology"), new Value.Str("C. Darwin"), new Value.Str("9th grade"))),
                                                new Value.Tuple(List.of(new Value.Str("Spanish"), new Value.Str("P. Cruz"), new Value.Str("9th grade"))),
                                                new Value.Tuple(List.of(new Value.Str("Math"), new Value.Str("A. Turing"), new Value.Str("10th grade"))),
                                                new Value.Tuple(List.of(new Value.Str("Math"), new Value.Str("A. Turing"), new Value.Str("10th grade"))),
                                                new Value.Tuple(List.of(new Value.Str("Geography"), new Value.Str("C. Darwin"), new Value.Str("10th grade"))),
                                                new Value.Tuple(List.of(new Value.Str("Spanish"), new Value.Str("P. Cruz"), new Value.Str("10th grade")))
                                        ))
                                        )
                                ),
                                new Proposition.Forall(
                                        List.of(new Value.Variable("l1"), new Value.Variable("l2")),
                                        new Value.Set(new Set.Named("Lesson")),
                                        new Proposition.Implies(
                                                new Proposition.And(
                                                        new Proposition.Binary(Operator.Different, new Value.Variable("l1"), new Value.Variable("l2")),
                                                        new Proposition.Binary(Operator.Equal, new Value.Curried(new Value.Set(new Set.Named("Lesson#slot")), new Value.Variable("l1")), new Value.Curried(new Value.Set(new Set.Named("Lesson#slot")), new Value.Variable("l2")))
                                                ),
                                                new Proposition.And(
                                                        new Proposition.Binary(Operator.Different, new Value.Curried(new Value.Set(new Set.Named("Lesson#room")), new Value.Variable("l1")), new Value.Curried(new Value.Set(new Set.Named("Lesson#room")), new Value.Variable("l2"))),
                                                        new Proposition.And(
                                                                new Proposition.Binary(Operator.Different, new Value.Curried(new Value.Set(new Set.Named("Lesson#teacher")), new Value.Variable("l1")), new Value.Curried(new Value.Set(new Set.Named("Lesson#teacher")), new Value.Variable("l2"))),
                                                                new Proposition.Binary(Operator.Different, new Value.Curried(new Value.Set(new Set.Named("Lesson#studentGroup")), new Value.Variable("l1")), new Value.Curried(new Value.Set(new Set.Named("Lesson#studentGroup")), new Value.Variable("l2"))))
                                                )
                                        )
                                )
                        )
                )
                ,
                new EngineTestCase("engine/simple_spec.peg",
                        List.of(
                                new Proposition.Binary(Operator.Equal, new Value.Set(new Set.Named("Room")), new Value.Set(new Set.Universe())),
                                new Proposition.Binary(
                                        Operator.Equal,
                                        new Value.Set(new Set.Named("Room#teacher")),
                                        new Value.Set(new Set.Product(new Value.Set(new Set.Named("Room")), new Value.Set(new Set.Named("Teacher"))))),
                                new Proposition.Binary(
                                        Operator.Equal, new Value.Set(new Set.Named("Teacher")), new Value.Set(new Set.Universe())),
                                new Proposition.Binary(
                                        Operator.Equal,
                                        new Value.Set(new Set.Named("Room")),
                                        new Value.Set(new Set.Literal(List.of(new Value.Str("Room A"), new Value.Str("Room B"))))),
                                new Proposition.Binary(
                                        Operator.Equal,
                                        new Value.Set(new Set.Named("Teacher")),
                                        new Value.Set(new Set.Literal(List.of(new Value.Str("Gerber"), new Value.Str("Damasio"))))),
                                new Proposition.Forall(
                                        List.of(new Value.Variable("t")),
                                        new Value.Set(new Set.Named("Teacher")),
                                        new Proposition.Exists(
                                                List.of(new Value.Variable("r")),
                                                new Value.Set(new Set.Named("Room")),
                                                new Proposition.Binary(
                                                        Operator.In,
                                                        new Value.Tuple(
                                                                List.of(new Value.Variable("r"), new Value.Variable("t"))),
                                                        new Value.Set(new Set.Named("Room#teacher"))))),
                                new Proposition.Forall(
                                        List.of(new Value.Variable("r")),
                                        new Value.Set(new Set.Named("Room")),
                                        new Proposition.Exists(
                                                List.of(new Value.Variable("t")),
                                                new Value.Set(new Set.Named("Teacher")),
                                                new Proposition.Binary(
                                                        Operator.In,
                                                        new Value.Tuple(
                                                                List.of(new Value.Variable("r"), new Value.Variable("t"))),
                                                        new Value.Set(new Set.Named("Room#teacher"))))))
        ));
    }
}
