package com.slopdetector.metrics;

import com.slopdetector.TestSources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NamingEntropyAnalyzerTest {

    private final NamingEntropyAnalyzer analyzer = new NamingEntropyAnalyzer();

    @ParameterizedTest
    @ValueSource(strings = {"data", "temp", "tempList", "resultData", "my_obj", "data2", "RESULT"})
    void genericNames(String name) {
        assertTrue(NamingEntropyAnalyzer.isGeneric(name), name);
    }

    @ParameterizedTest
    @ValueSource(strings = {"userData", "invoiceTotal", "databaseUrl", "retryCount", "customerList"})
    void meaningfulNames(String name) {
        assertFalse(NamingEntropyAnalyzer.isGeneric(name), name);
    }

    @Test
    void ignoresSingleLetterNames() {
        var cu = TestSources.parse("class A { void f() { for (int i = 0; i < 3; i++) { } try { } catch (Exception e) { } } }");
        assertEquals(0.0, analyzer.analyze(cu).score());
    }

    @Test
    void countsParameters() {
        var cu = TestSources.parse("class A { void f(String data, Object obj, int value, int item) { } }");
        assertEquals(1.0, analyzer.analyze(cu).score(), 1e-9);
    }

    @Test
    void smallSamplesAreDampened() {
        var cu = TestSources.parse("class A { int data; }");
        assertEquals(0.25, analyzer.analyze(cu).score(), 1e-9);
    }
}
