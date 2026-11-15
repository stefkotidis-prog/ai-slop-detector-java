package com.slopdetector.metrics;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.SimpleName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Detects comments that merely restate the code they sit on, e.g.
 * {@code // Initialize temp variable to zero} above {@code int temp = 0;}.
 * Narrating comments like this are one of the most recognisable traits of LLM output.
 *
 * <p>The score is mostly the share of redundant comments, with a smaller component for
 * unusually high comment density. License headers and package/import comments are ignored.
 */
public class CommentDensityAnalyzer implements Analyzer {

    /** Function words that carry no information. */
    private static final Set<String> STOPWORDS = Set.of(
            "the", "a", "an", "to", "of", "for", "and", "or", "this", "that", "is", "are", "in",
            "on", "with", "it", "its", "be", "we", "will", "by", "as", "at", "from", "if", "then",
            "else", "our", "here", "now", "into", "which", "based", "given", "provided", "all");

    /** Words that describe code mechanics rather than intent ("initialize the variable"). */
    private static final Set<String> CODE_NARRATION = Set.of(
            "variable", "method", "function", "field", "class", "loop", "initialize", "initialise",
            "declare", "define", "set", "get", "return", "call", "create", "new", "instance",
            "param", "parameter", "argument", "check", "store", "assign", "update", "increment",
            "iterate", "through", "each", "over", "string", "integer", "int", "boolean", "list",
            "object", "constructor", "helper", "logic", "handle");

    private static final double REDUNDANCY_THRESHOLD = 0.6;
    private static final double DENSITY_START = 0.15;
    private static final double DENSITY_FULL = 0.40;

    @Override
    public String name() {
        return "Narrating comments";
    }

    @Override
    public double weight() {
        return 0.35;
    }

    @Override
    public MetricResult analyze(CompilationUnit cu) {
        List<Comment> comments = cu.getAllComments().stream().filter(c -> !isHeader(c)).toList();
        if (comments.isEmpty()) {
            return new MetricResult(name(), 0.0, List.of());
        }

        List<String> findings = new ArrayList<>();
        int commentLines = 0;
        for (Comment comment : comments) {
            commentLines += comment.getRange().map(r -> r.getLineCount()).orElse(1);
            if (isRedundant(comment)) {
                findings.add(comment.getBegin().map(p -> "line " + p.line + ": ").orElse("")
                        + "comment restates code: \"" + firstLine(comment) + "\"");
            }
        }

        double redundancy = (double) findings.size() / comments.size();
        int totalLines = cu.getRange().map(r -> r.getLineCount()).orElse(1);
        double density = (double) commentLines / Math.max(1, totalLines);
        double densityScore = clamp((density - DENSITY_START) / (DENSITY_FULL - DENSITY_START));
        if (densityScore > 0) {
            findings.add(String.format(Locale.ROOT, "comment density %.0f%% of lines", density * 100));
        }

        return new MetricResult(name(), 0.8 * redundancy + 0.2 * densityScore, findings);
    }

    static boolean isRedundant(Comment comment) {
        String text = commentText(comment);
        Set<String> words = meaningfulWords(text);
        if (words.isEmpty()) {
            // Pure narration such as "// Initialize variable" is redundant;
            // a separator line such as "// ------" has no words at all and is not.
            return !tokens(text).isEmpty();
        }
        Node target = comment.getCommentedNode().orElse(null);
        if (target == null) {
            return false;
        }
        Set<String> codeWords = codeWords(target);
        long overlap = words.stream().filter(codeWords::contains).count();
        return (double) overlap / words.size() >= REDUNDANCY_THRESHOLD;
    }

    private static Set<String> meaningfulWords(String text) {
        Set<String> result = new HashSet<>();
        for (String token : tokens(text)) {
            String stem = Identifiers.stem(token);
            if (token.length() > 1 && !STOPWORDS.contains(token)
                    && !CODE_NARRATION.contains(token) && !CODE_NARRATION.contains(stem)) {
                result.add(stem);
            }
        }
        return result;
    }

    private static List<String> tokens(String text) {
        List<String> tokens = new ArrayList<>();
        for (String raw : text.split("[^A-Za-z0-9_]+")) {
            if (raw.isEmpty()) {
                continue;
            }
            tokens.addAll(Identifiers.split(raw));
        }
        return tokens;
    }

    /** Identifier words (and a few literal spellings) visible in the commented node. */
    private static Set<String> codeWords(Node target) {
        List<Node> sources = new ArrayList<>();
        if (target instanceof CallableDeclaration<?> callable) {
            // Only the signature: the body would match almost any comment.
            sources.add(callable.getName());
            for (Parameter p : callable.getParameters()) {
                sources.add(p);
            }
        } else if (target instanceof TypeDeclaration<?> type) {
            sources.add(type.getName());
        } else {
            sources.add(target);
        }

        Set<String> words = new HashSet<>();
        for (Node source : sources) {
            List<SimpleName> names = new ArrayList<>(source.findAll(SimpleName.class));
            if (source instanceof SimpleName sn) {
                names.add(sn);
            }
            for (SimpleName sn : names) {
                for (String w : Identifiers.split(sn.getIdentifier())) {
                    words.add(Identifiers.stem(w));
                }
            }
            source.findAll(IntegerLiteralExpr.class).forEach(lit -> {
                switch (lit.getValue()) {
                    case "0" -> words.add("zero");
                    case "1" -> words.add("one");
                    default -> { }
                }
            });
            source.findAll(BooleanLiteralExpr.class).forEach(b -> words.add(String.valueOf(b.getValue())));
            source.findAll(NullLiteralExpr.class).forEach(n -> words.add("null"));
        }
        return words;
    }

    private static String commentText(Comment comment) {
        if (comment instanceof JavadocComment javadoc) {
            // Only the description; block tags like @param/@return are allowed to repeat names.
            return javadoc.parse().getDescription().toText();
        }
        return comment.getContent();
    }

    private static boolean isHeader(Comment comment) {
        Node target = comment.getCommentedNode().orElse(null);
        if (target instanceof PackageDeclaration || target instanceof ImportDeclaration) {
            return true;
        }
        return comment.isBlockComment() && comment.getBegin().map(p -> p.line == 1).orElse(false);
    }

    private static String firstLine(Comment comment) {
        String text = commentText(comment).strip().replaceAll("\\s*\\n\\s*\\*?\\s*", " ");
        return text.length() > 60 ? text.substring(0, 57) + "..." : text;
    }

    private static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
