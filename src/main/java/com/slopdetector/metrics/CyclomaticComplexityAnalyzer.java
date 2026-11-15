package com.slopdetector.metrics;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

import java.util.ArrayList;
import java.util.List;

/**
 * Measures branching structure per method/constructor. LLM output tends towards deeply nested
 * if/else pyramids instead of guard clauses, so both McCabe complexity and nesting depth are penalised.
 *
 * <p>The file score blends the average and the worst method, so a single pathological method
 * is not diluted by many trivial getters.
 */
public class CyclomaticComplexityAnalyzer implements Analyzer {

    static final int COMPLEXITY_THRESHOLD = 5;
    static final int NESTING_THRESHOLD = 2;
    private static final double PENALTY_PER_BRANCH = 0.15;
    private static final double PENALTY_PER_LEVEL = 0.25;

    @Override
    public String name() {
        return "Nested branching";
    }

    @Override
    public double weight() {
        return 0.30;
    }

    @Override
    public MetricResult analyze(CompilationUnit cu) {
        @SuppressWarnings("rawtypes")
        List<CallableDeclaration> callables = cu.findAll(CallableDeclaration.class);
        if (callables.isEmpty()) {
            return new MetricResult(name(), 0.0, List.of());
        }

        List<String> findings = new ArrayList<>();
        double sum = 0.0;
        double max = 0.0;
        for (CallableDeclaration<?> callable : callables) {
            int complexity = cyclomaticComplexity(callable);
            int depth = maxNesting(callable, 0);
            double penalty = Math.min(1.0,
                    Math.max(0, complexity - COMPLEXITY_THRESHOLD) * PENALTY_PER_BRANCH
                            + Math.max(0, depth - NESTING_THRESHOLD) * PENALTY_PER_LEVEL);
            if (penalty > 0) {
                findings.add(callable.getBegin().map(p -> "line " + p.line + ": ").orElse("")
                        + callable.getNameAsString() + "() complexity " + complexity + ", nesting depth " + depth);
            }
            sum += penalty;
            max = Math.max(max, penalty);
        }
        double average = sum / callables.size();
        return new MetricResult(name(), 0.5 * average + 0.5 * max, findings);
    }

    /** McCabe complexity: 1 + number of decision points. */
    static int cyclomaticComplexity(Node callable) {
        int complexity = 1;
        complexity += callable.findAll(IfStmt.class).size();
        complexity += callable.findAll(ForStmt.class).size();
        complexity += callable.findAll(ForEachStmt.class).size();
        complexity += callable.findAll(WhileStmt.class).size();
        complexity += callable.findAll(DoStmt.class).size();
        complexity += callable.findAll(CatchClause.class).size();
        complexity += callable.findAll(ConditionalExpr.class).size();
        complexity += (int) callable.findAll(SwitchEntry.class).stream()
                .filter(e -> !e.getLabels().isEmpty())   // `default` is not a new path
                .count();
        complexity += (int) callable.findAll(BinaryExpr.class).stream()
                .filter(b -> b.getOperator() == BinaryExpr.Operator.AND || b.getOperator() == BinaryExpr.Operator.OR)
                .count();
        return complexity;
    }

    /** Deepest nesting of control-flow blocks. An {@code else if} chain stays at the same level. */
    static int maxNesting(Node node, int depth) {
        if (node instanceof IfStmt ifStmt) {
            int inner = maxNesting(ifStmt.getThenStmt(), depth + 1);
            if (ifStmt.getElseStmt().isPresent()) {
                Node elseStmt = ifStmt.getElseStmt().get();
                int elseDepth = elseStmt instanceof IfStmt ? depth : depth + 1;
                inner = Math.max(inner, maxNesting(elseStmt, elseDepth));
            }
            return Math.max(inner, depth + 1);
        }
        int next = isBlockOpener(node) ? depth + 1 : depth;
        int max = next;
        for (Node child : node.getChildNodes()) {
            max = Math.max(max, maxNesting(child, next));
        }
        return max;
    }

    private static boolean isBlockOpener(Node node) {
        return node instanceof ForStmt || node instanceof ForEachStmt || node instanceof WhileStmt
                || node instanceof DoStmt || node instanceof SwitchStmt || node instanceof TryStmt;
    }
}
