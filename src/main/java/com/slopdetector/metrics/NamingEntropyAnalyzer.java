package com.slopdetector.metrics;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Flags variable and parameter names that carry no domain meaning ({@code data}, {@code temp},
 * {@code resultList}, {@code myObj2}). A name is generic when every word in it is generic,
 * so {@code userData} passes but {@code tempData} does not.
 *
 * <p>Single-letter names ({@code i}, {@code e}, {@code x}) are ignored: they are idiomatic for
 * loop counters, lambdas and exceptions and say nothing about authorship.
 */
public class NamingEntropyAnalyzer implements Analyzer {

    static final Set<String> GENERIC_WORDS = Set.of(
            "data", "result", "results", "temp", "tmp", "list", "item", "items", "obj", "object",
            "value", "values", "val", "helper", "res", "arr", "array", "info", "stuff", "thing",
            "things", "my", "foo", "bar", "baz", "ret", "retval", "str", "num", "flag", "var",
            "output", "elem", "element", "new", "some");

    /** Below this many names the ratio is too noisy, so the score is scaled down proportionally. */
    private static final int MIN_SAMPLE = 4;

    @Override
    public String name() {
        return "Generic naming";
    }

    @Override
    public double weight() {
        return 0.35;
    }

    @Override
    public MetricResult analyze(CompilationUnit cu) {
        List<Node> named = Stream.concat(
                cu.findAll(VariableDeclarator.class).stream(),
                cu.findAll(Parameter.class).stream()).map(n -> (Node) n).toList();

        int counted = 0;
        List<String> findings = new ArrayList<>();
        for (Node node : named) {
            String name = ((NodeWithSimpleName<?>) node).getNameAsString();
            if (name.length() <= 1) {
                continue;
            }
            counted++;
            if (isGeneric(name)) {
                findings.add(line(node) + "generic name '" + name + "'");
            }
        }

        if (counted == 0) {
            return new MetricResult(name(), 0.0, List.of());
        }
        double ratio = (double) findings.size() / counted;
        double confidence = Math.min(1.0, (double) counted / MIN_SAMPLE);
        return new MetricResult(name(), ratio * confidence, findings);
    }

    static boolean isGeneric(String identifier) {
        List<String> words = Identifiers.split(identifier);
        return !words.isEmpty() && GENERIC_WORDS.containsAll(words);
    }

    private static String line(Node node) {
        return node.getBegin().map(p -> "line " + p.line + ": ").orElse("");
    }
}
