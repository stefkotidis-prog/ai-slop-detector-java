package com.slopdetector.metrics;

import com.github.javaparser.ast.comments.Comment;
import com.slopdetector.TestSources;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommentDensityAnalyzerTest {

    private final CommentDensityAnalyzer analyzer = new CommentDensityAnalyzer();

    private static Comment onlyComment(String source) {
        var comments = TestSources.parse(source).getAllComments();
        assertEquals(1, comments.size());
        return comments.get(0);
    }

    @Test
    void commentThatRestatesStatementIsRedundant() {
        assertTrue(CommentDensityAnalyzer.isRedundant(onlyComment(
                "class A { void f() {\n // Initialize temp variable to zero\n int temp = 0; } }")));
    }

    @Test
    void commentThatRestatesMethodNameIsRedundant() {
        assertTrue(CommentDensityAnalyzer.isRedundant(onlyComment(
                "class A {\n // This method processes the data\n void processData() { } }")));
    }

    @Test
    void pureNarrationIsRedundant() {
        assertTrue(CommentDensityAnalyzer.isRedundant(onlyComment(
                "class A { int f() { int total = 1;\n // Return the variable\n return total; } }")));
    }

    @Test
    void commentExplainingWhyIsNotRedundant() {
        assertFalse(CommentDensityAnalyzer.isRedundant(onlyComment(
                "class A { void f() {\n // Banks settle late on weekends, so wait before retrying.\n int retryDelayDays = 3; } }")));
    }

    @Test
    void separatorLineIsNotRedundant() {
        assertFalse(CommentDensityAnalyzer.isRedundant(onlyComment(
                "class A {\n // ---------------\n int count; }")));
    }

    @Test
    void javadocRestatingGetterIsRedundant() {
        assertTrue(CommentDensityAnalyzer.isRedundant(onlyComment(
                "class A {\n /** Gets the user name. */\n String getUserName() { return null; } }")));
    }

    @Test
    void licenseHeaderIsIgnored() {
        var cu = TestSources.parse("/* Copyright the authors. Licensed under MIT. */\npackage p;\nclass A { }");
        assertEquals(0.0, analyzer.analyze(cu).score());
    }

    @Test
    void noCommentsScoresZero() {
        assertEquals(0.0, analyzer.analyze(TestSources.parse("class A { int x; }")).score());
    }
}
