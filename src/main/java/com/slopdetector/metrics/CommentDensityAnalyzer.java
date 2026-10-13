package com.slopdetector.metrics;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import java.util.List;

public class CommentDensityAnalyzer {

    public double computeCommentScore(CompilationUnit cu) {
        int totalLines = cu.getRange().map(r -> r.getLineCount()).orElse(1);
        
        // Fix: Χρησιμοποιούμε το getAllComments() αντί για διάσχιση δέντρου
        List<Comment> comments = cu.getAllComments();
        
        if (comments.isEmpty()) {
            return 0.0;
        }

        int commentLines = comments.stream()
            .mapToInt(c -> c.getRange().map(r -> r.getLineCount()).orElse(0))
            .sum();

        return (double) commentLines / totalLines;
    }
}