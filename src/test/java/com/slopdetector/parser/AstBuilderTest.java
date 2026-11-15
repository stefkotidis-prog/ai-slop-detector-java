package com.slopdetector.parser;

import com.slopdetector.TestSources;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AstBuilderTest {

    private final AstBuilder parser = new AstBuilder();

    @Test
    void parsesModernJavaSyntax() {
        assertTrue(parser.buildFromFile(TestSources.SAMPLES.resolve("ModernJavaSample.java")).isPresent());
    }

    @Test
    void returnsEmptyForMissingFile() {
        assertFalse(parser.buildFromFile(Path.of("does/not/exist.java")).isPresent());
    }

    @Test
    void returnsEmptyForInvalidSource() {
        assertFalse(parser.buildFromSource("class {", "<broken>").isPresent());
    }
}
