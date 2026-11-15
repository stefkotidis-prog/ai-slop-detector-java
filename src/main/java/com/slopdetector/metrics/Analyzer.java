package com.slopdetector.metrics;

import com.github.javaparser.ast.CompilationUnit;

/**
 * A single heuristic that estimates how "AI-generated" a compilation unit looks.
 * Implementations must be stateless so one instance can analyse many files.
 */
public interface Analyzer {

    /** Human-readable metric name used in reports. */
    String name();

    /** Relative weight of this metric in the final score. Weights are normalised by the detector. */
    double weight();

    /** Analyses the AST and returns a score in [0, 1] plus the findings that produced it. */
    MetricResult analyze(CompilationUnit cu);
}
