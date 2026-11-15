package com.slopdetector;

import com.github.javaparser.ast.CompilationUnit;
import com.slopdetector.parser.AstBuilder;

import java.nio.file.Path;

public final class TestSources {

    public static final Path SAMPLES = Path.of("src/test/resources/samples");

    private static final AstBuilder PARSER = new AstBuilder();

    private TestSources() {
    }

    public static CompilationUnit parse(String source) {
        return PARSER.buildFromSource(source, "<test>").orElseThrow(() -> new AssertionError("does not parse"));
    }

    public static CompilationUnit sample(String fileName) {
        return PARSER.buildFromFile(SAMPLES.resolve(fileName)).orElseThrow(() -> new AssertionError("does not parse"));
    }
}
