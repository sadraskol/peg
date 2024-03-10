package com.sadraskol.peg.parser;

import java.util.List;

public sealed interface Statement {
    record Import(List<String> domain) implements Statement {
    }

    record Record(String name, List<RecordMember> members, List<RecordRelation> relations) implements Statement {
    }
}
