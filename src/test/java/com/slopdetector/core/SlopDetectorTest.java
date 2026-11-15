package com.slopdetector.core;

import com.slopdetector.TestSources;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlopDetectorTest {

    private final SlopDetector detector = new SlopDetector(SlopDetector.DEFAULT_THRESHOLD);

    @Test
    void flagsAiStyleSample() {
        FileReport report = detector.analyzeFile(TestSources.SAMPLES.resolve("AiStyleSample.java")).orElseThrow();
        assertTrue(report.flagged(), "score was " + report.score());
    }

    @Test
    void passesHumanStyleSample() {
        FileReport report = detector.analyzeFile(TestSources.SAMPLES.resolve("HumanStyleSample.java")).orElseThrow();
        assertFalse(report.flagged(), "score was " + report.score());
        assertTrue(report.score() < 0.15, "score was " + report.score());
    }

    @Test
    void collectsJavaFilesRecursively() throws IOException {
        var files = SlopDetector.collectJavaFiles(List.of(TestSources.SAMPLES));
        assertEquals(3, files.size());
    }

    @Test
    void cliExitCodes() {
        assertEquals(Engine.EXIT_FLAGGED, runCli(TestSources.SAMPLES.toString()));
        assertEquals(Engine.EXIT_OK, runCli(TestSources.SAMPLES.resolve("HumanStyleSample.java").toString()));
        assertEquals(Engine.EXIT_ERROR, runCli());
        assertEquals(Engine.EXIT_ERROR, runCli("--threshold", "2", "x.java"));
        assertEquals(Engine.EXIT_ERROR, runCli("missing.java"));
    }

    private static int runCli(String... args) {
        var sink = new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);
        return Engine.run(args, sink, sink);
    }
}
