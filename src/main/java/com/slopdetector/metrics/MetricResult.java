package com.slopdetector.metrics;

import java.util.List;

/**
 * Outcome of one {@link Analyzer} on one file.
 *
 * @param name     metric name
 * @param score    slop score in [0, 1]; higher means more AI-like
 * @param findings human-readable reasons (e.g. "line 6: generic name 'temp'")
 */
public record MetricResult(String name, double score, List<String> findings) {

    public MetricResult {
        if (Double.isNaN(score)) {
            score = 0.0;
        }
        score = Math.max(0.0, Math.min(1.0, score));
        findings = List.copyOf(findings);
    }
}
