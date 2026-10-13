package com.slopdetector.metrics;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.CatchClause;

public class CyclomaticComplexityAnalyzer {

    // Threshold: Methods with complexity higher than this are penalized
    private static final int COMPLEXITY_THRESHOLD = 5;

    public double computeComplexityScore(CompilationUnit cu) {
        var methods = cu.findAll(MethodDeclaration.class);
        
        if (methods.isEmpty()) {
            return 0.0;
        }

        double totalPenalty = 0.0;

        for (MethodDeclaration method : methods) {
            // Base path is 1
            int complexity = 1;
            
            // Count branching nodes
            complexity += method.findAll(IfStmt.class).size();
            complexity += method.findAll(ForStmt.class).size();
            complexity += method.findAll(WhileStmt.class).size();
            complexity += method.findAll(SwitchEntry.class).size();
            complexity += method.findAll(CatchClause.class).size();

            // Calculate penalty if the method is too complex
            if (complexity > COMPLEXITY_THRESHOLD) {
                // Add 15% slop probability for every point above the threshold, capped at 1.0 (100%)
                double penalty = Math.min((complexity - COMPLEXITY_THRESHOLD) * 0.15, 1.0);
                totalPenalty += penalty;
            }
        }

        // Return the average penalty across all methods in the file
        return totalPenalty / methods.size();
    }
}