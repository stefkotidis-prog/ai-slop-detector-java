package com.slopdetector.core;

import com.slopdetector.parser.AstBuilder;
import com.slopdetector.metrics.NamingEntropyAnalyzer;
import com.github.javaparser.ast.CompilationUnit;
import java.util.Optional;

public class Engine {
    public static void main(String[] args) {
        System.out.println("[INFO] Booting SlopDetector Engine...");
        
        AstBuilder parser = new AstBuilder();
        NamingEntropyAnalyzer namingAnalyzer = new NamingEntropyAnalyzer();
        
        String targetFile = "TestCode.java"; 
        Optional<CompilationUnit> astOptional = parser.buildFromFile(targetFile);
        
        astOptional.ifPresentOrElse(ast -> {
            double namingScore = namingAnalyzer.computeSlopScore(ast);
            
            System.out.println("--- Analysis Report for: " + targetFile + " ---");
            System.out.printf("Naming Entropy Slop Score: %.2f%%\n", namingScore * 100);
            
            if (namingScore > 0.3) {
                System.out.println("[WARNING] High probability of AI-generated code detected.");
            }
        }, () -> {
            System.err.println("[ERROR] Pipeline aborted. AST generation failed.");
        });
    }
}