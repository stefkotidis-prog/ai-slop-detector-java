package com.slopdetector.core;

import com.slopdetector.metrics.MetricResult;

import java.nio.file.Path;
import java.util.List;

/**
 * Analysis outcome for one source file.
 *
 * @param file    analysed file
 * @param metrics per-analyzer results, in analyzer order
 * @param score   weighted slop score in [0, 1]
 * @param flagged whether {@code score} reached the detector's threshold
 */
public record FileReport(Path file, List<MetricResult> metrics, double score, boolean flagged) {

    public FileReport {
        metrics = List.copyOf(metrics);
    }
}
