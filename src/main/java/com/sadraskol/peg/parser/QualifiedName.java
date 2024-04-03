package com.sadraskol.peg.parser;

import java.util.List;

public record QualifiedName(List<String> labels, String name) {
}
