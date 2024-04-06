package com.sadraskol.peg.engine;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public sealed interface Proposition {
    default Proposition conjunctiveNormalForm() {
        return this;
    }

    default Stream<Proposition> splitConjonctiveNormalForm() {
        return Stream.of(this);
    }

    default List<Proposition> terms() {
        return List.of(this);
    }

    default List<Integer> substituteTerms(List<Proposition> terms) {
        if (!terms.contains(this)) {
            throw new IllegalStateException("Could not substitute " + this + " from terms: " + terms);
        } else {
            return List.of(terms.indexOf(this) + 1);
        }
    }

    record Or(Proposition left, Proposition right) implements Proposition {
        public Proposition conjunctiveNormalForm() {
            var cnfLeft = left.conjunctiveNormalForm();
            var cnfRight = right.conjunctiveNormalForm();
            if (cnfLeft instanceof Proposition.False) {
                return cnfRight;
            }
            if (cnfRight instanceof Proposition.False) {
                return cnfLeft;
            }
            if (cnfLeft instanceof Proposition.True || cnfRight instanceof Proposition.True) {
                return new Proposition.True();
            }
            if (cnfLeft instanceof Proposition.And) {
                var l = ((And) cnfLeft).left();
                var r = ((And) cnfLeft).right();
                return new Proposition.And(
                        new Proposition.Or(l, cnfRight).conjunctiveNormalForm(),
                        new Proposition.Or(r, cnfRight).conjunctiveNormalForm()
                ).conjunctiveNormalForm();
            }
            if (right instanceof Proposition.And) {
                var l = ((And) cnfRight).left();
                var r = ((And) cnfRight).right();
                return new Proposition.And(
                        new Proposition.Or(cnfLeft, l).conjunctiveNormalForm(),
                        new Proposition.Or(cnfLeft, r).conjunctiveNormalForm()
                ).conjunctiveNormalForm();
            }
            return new Proposition.Or(cnfLeft, cnfRight);
        }
        public List<Proposition> terms() {
            return Stream.concat(left.terms().stream(), right.terms().stream()).toList();
        }

        public List<Integer> substituteTerms(List<Proposition> terms) {
            return Stream.concat(
                    left.substituteTerms(terms).stream(),
                    right.substituteTerms(terms).stream()
            ).toList();
        }

        public String toString() {
            return left.toString() + " or " + right.toString();
        }
    }

    record And(Proposition left, Proposition right) implements Proposition {
        public Proposition conjunctiveNormalForm() {
            var cnfLeft = left.conjunctiveNormalForm();
            var cnfRight = right.conjunctiveNormalForm();
            if (cnfLeft instanceof Proposition.True) {
                return cnfRight;
            }
            if (cnfRight instanceof Proposition.True) {
                return cnfLeft;
            }
            if (cnfLeft instanceof Proposition.False || cnfRight instanceof Proposition.False) {
                return new Proposition.False();
            }
            return new Proposition.And(cnfLeft, cnfRight);
        }

        public Stream<Proposition> splitConjonctiveNormalForm() {
            return Stream.concat(
                    left.splitConjonctiveNormalForm(),
                    right.splitConjonctiveNormalForm()
            );
        }

        public List<Proposition> terms() {
            return Stream.concat(left.terms().stream(), right.terms().stream()).toList();
        }

        public String toString() {
            return left.toString() + " and " + right.toString();
        }
    }

    record Forall(List<Value.Variable> args, Value.NamedSet set, Proposition predicate) implements Proposition {
        public Proposition conjunctiveNormalForm() {
            throw new IllegalStateException("Cannot simplify forall proposition");
        }

        public String toString() {
            return "forall " + args.stream().map(Value.Variable::name).collect(Collectors.joining(", ")) + " in " + set.name() + ": " + predicate.toString();
        }
    }

    record Exists(List<Value.Variable> args, Value.NamedSet set, Proposition predicate) implements Proposition {
        public Proposition conjunctiveNormalForm() {
            throw new IllegalStateException("Cannot simplify forall proposition");
        }

        public String toString() {
            return "exists " + args.stream().map(Value.Variable::name).collect(Collectors.joining(", ")) + " in " + set.name() + ": " + predicate.toString();
        }
    }

    record Not(Proposition other) implements Proposition {
        public String toString() {
            return "not " + other.toString();
        }
    }

    record Primary(BinaryOp truth) implements Proposition {
        public String toString() {
            return truth.toString();
        }
    }

    record True() implements Proposition {
        public String toString() {
            return "true";
        }
    }

    record False() implements Proposition {
        public String toString() {
            return "false";
        }
    }
}
