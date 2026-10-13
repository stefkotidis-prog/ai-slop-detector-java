package com.slopdetector.core;

import com.slopdetector.parser.AstBuilder;
import com.github.javaparser.ast.CompilationUnit;
import java.util.Optional;

public class Engine {
    public static void main(String[] args) {
        System.out.println("[INFO] SlopDetector Engine initialized.");
        
        // 1. Instantiate the AST Builder
        AstBuilder builder = new AstBuilder();
        
        // 2. Define the target file to analyze
        String targetFile = "TestCode.java"; 
        
        // 3. Attempt to parse the file into an AST
        Optional<CompilationUnit> ast = builder.buildFromFile(targetFile);
        
        // 4. Safely check if the parsing was successful
        if (ast.isPresent()) {
            System.out.println("[SUCCESS] Abstract Syntax Tree generated successfully.");
            System.out.println("--- AST OUTPUT ---");
            System.out.println(ast.get().toString());
            System.out.println("------------------");
        } else {
            System.err.println("[FAILED] Could not generate AST for the target file.");
        }
    }
}