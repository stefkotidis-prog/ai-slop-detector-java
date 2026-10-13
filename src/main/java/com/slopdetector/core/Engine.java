package com.slopdetector.core;

import com.slopdetector.parser.AstBuilder;
import com.slopdetector.metrics.NamingEntropyAnalyzer;
import com.slopdetector.metrics.CommentDensityAnalyzer;
import com.slopdetector.metrics.CyclomaticComplexityAnalyzer;
import com.github.javaparser.ast.CompilationUnit;
import java.util.Optional;

public class Engine {
    public static void main(String[] args) {
        System.out.println("[INFO] Booting SlopDetector Engine...");
        
        AstBuilder parser = new AstBuilder();
        NamingEntropyAnalyzer namingAnalyzer = new NamingEntropyAnalyzer();
        CommentDensityAnalyzer commentAnalyzer = new CommentDensityAnalyzer();
        CyclomaticComplexityAnalyzer complexityAnalyzer = new CyclomaticComplexityAnalyzer();
        
        String targetFile = "TestCode.java"; 
        Optional<CompilationUnit> astOptional = parser.buildFromFile(targetFile);
        
        astOptional.ifPresentOrElse(ast -> {
            System.out.println("--- Analysis Report for: " + targetFile + " ---");
            
            double namingScore = namingAnalyzer.computeSlopScore(ast);
            System.out.printf("Naming Entropy Slop: %.2f%%\n", namingScore * 100);
            
            double commentScore = commentAnalyzer.computeCommentScore(ast);
            System.out.printf("Comment Density Slop: %.2f%%\n", commentScore * 100);
            
            double complexityScore = complexityAnalyzer.computeComplexityScore(ast);
            System.out.printf("Cyclomatic Complexity Slop: %.2f%%\n", complexityScore * 100);
            
            // Weighted Average: 40% Naming, 30% Structure, 30% Comments
            double finalScore = (namingScore * 0.4) + (complexityScore * 0.3) + (commentScore * 0.3);
            System.out.printf("Final AI Slop Probability: %.2f%%\n", finalScore * 100);
            
            if (finalScore > 0.4) {
                System.out.println("[WARNING] Code flagged as heavily AI-generated.");
            } else {
                System.out.println("[OK] Code passes static hygiene checks.");
            }
            System.out.println("-------------------------------------");
            
        }, () -> {
            System.err.println("[ERROR] Pipeline aborted. AST generation failed.");
        });
    }
}