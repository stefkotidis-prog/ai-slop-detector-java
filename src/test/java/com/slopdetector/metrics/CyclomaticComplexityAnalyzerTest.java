package com.slopdetector.metrics;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.slopdetector.TestSources;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CyclomaticComplexityAnalyzerTest {

    private final CyclomaticComplexityAnalyzer analyzer = new CyclomaticComplexityAnalyzer();

    private static MethodDeclaration method(String body) {
        return TestSources.parse("class A { void f(int a, int b) { " + body + " } }")
                .findFirst(MethodDeclaration.class).orElseThrow();
    }

    @Test
    void countsAllDecisionPoints() {
        String body = """
                if (a > 0 && b > 0) { }
                for (int i = 0; i < a; i++) { }
                for (var x : new int[0]) { }
                while (a < 0) { }
                do { } while (b < 0);
                int c = a > b ? a : b;
                switch (a) { case 1 -> { } case 2 -> { } default -> { } }
                try { } catch (RuntimeException e) { }
                """;
        // 1 + if + && + for + foreach + while + do + ?: + 2 cases + catch
        assertEquals(11, CyclomaticComplexityAnalyzer.cyclomaticComplexity(method(body)));
    }

    @Test
    void elseIfChainDoesNotDeepenNesting() {
        String body = "if (a == 1) { } else if (a == 2) { } else if (a == 3) { } else { }";
        assertEquals(1, CyclomaticComplexityAnalyzer.maxNesting(method(body), 0));
    }

    @Test
    void nestedIfsDeepenNesting() {
        String body = "if (a > 0) { if (b > 0) { for (;;) { } } }";
        assertEquals(3, CyclomaticComplexityAnalyzer.maxNesting(method(body), 0));
    }

    @Test
    void flatCodeScoresZero() {
        assertEquals(0.0, analyzer.analyze(TestSources.parse("class A { int f(int a) { return a + 1; } }")).score());
    }

    @Test
    void oneBadMethodIsNotDilutedByManyGetters() {
        StringBuilder src = new StringBuilder("class A {");
        for (int i = 0; i < 20; i++) {
            src.append("int g").append(i).append("() { return ").append(i).append("; }");
        }
        src.append("void bad(int a, int b, int c) { if (a > 0) { if (b > 0) { if (c > 0) { if (a > b) { } } } } }");
        src.append("}");
        assertTrue(analyzer.analyze(TestSources.parse(src.toString())).score() >= 0.25);
    }
}
