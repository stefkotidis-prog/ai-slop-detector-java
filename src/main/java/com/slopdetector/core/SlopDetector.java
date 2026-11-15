package com.slopdetector.core;

import com.github.javaparser.ast.CompilationUnit;
import com.slopdetector.metrics.Analyzer;
import com.slopdetector.metrics.CommentDensityAnalyzer;
import com.slopdetector.metrics.CyclomaticComplexityAnalyzer;
import com.slopdetector.metrics.MetricResult;
import com.slopdetector.metrics.NamingEntropyAnalyzer;
import com.slopdetector.parser.AstBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/** Runs every {@link Analyzer} over Java files and combines their scores. */
public class SlopDetector {

    public static final double DEFAULT_THRESHOLD = 0.35;

    private final AstBuilder parser;
    private final List<Analyzer> analyzers;
    private final double threshold;

    public SlopDetector(double threshold) {
        this(new AstBuilder(), defaultAnalyzers(), threshold);
    }

    public SlopDetector(AstBuilder parser, List<Analyzer> analyzers, double threshold) {
        if (analyzers.isEmpty()) {
            throw new IllegalArgumentException("At least one analyzer is required");
        }
        this.parser = parser;
        this.analyzers = List.copyOf(analyzers);
        this.threshold = threshold;
    }

    public static List<Analyzer> defaultAnalyzers() {
        return List.of(new NamingEntropyAnalyzer(), new CommentDensityAnalyzer(), new CyclomaticComplexityAnalyzer());
    }

    /** Analyses one file; empty if it could not be read or parsed. */
    public Optional<FileReport> analyzeFile(Path file) {
        return parser.buildFromFile(file).map(cu -> analyze(file, cu));
    }

    public FileReport analyze(Path file, CompilationUnit cu) {
        List<MetricResult> results = new ArrayList<>();
        double weighted = 0.0;
        double totalWeight = 0.0;
        for (Analyzer analyzer : analyzers) {
            MetricResult result = analyzer.analyze(cu);
            results.add(result);
            weighted += result.score() * analyzer.weight();
            totalWeight += analyzer.weight();
        }
        double score = weighted / totalWeight;
        return new FileReport(file, results, score, score >= threshold);
    }

    /** Expands files and directories (recursively) into a sorted list of {@code .java} files. */
    public static List<Path> collectJavaFiles(List<Path> roots) throws IOException {
        List<Path> files = new ArrayList<>();
        for (Path root : roots) {
            if (Files.isDirectory(root)) {
                try (Stream<Path> walk = Files.walk(root)) {
                    walk.filter(Files::isRegularFile)
                            .filter(p -> p.toString().endsWith(".java"))
                            .sorted()
                            .forEach(files::add);
                } catch (UncheckedIOException e) {
                    throw e.getCause();
                }
            } else {
                files.add(root);
            }
        }
        return files;
    }
}
