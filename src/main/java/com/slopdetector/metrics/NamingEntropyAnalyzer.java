package com.slopdetector.metrics;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import java.util.List;
import java.util.Set;

public class NamingEntropyAnalyzer {

    private static final Set<String> SLOP_INDICATORS = Set.of(
        "data", "result", "temp", "list", "item", "obj", "value", "helper", "res", "arr"
    );

    public double computeSlopScore(CompilationUnit cu) {
        List<VariableDeclarator> variables = cu.findAll(VariableDeclarator.class);

        if (variables.isEmpty()) {
            return 0.0;
        }

        long genericNameCount = variables.stream()
            .map(v -> v.getNameAsString().toLowerCase())
            .filter(SLOP_INDICATORS::contains)
            .count();

        return (double) genericNameCount / variables.size();
    }
}