package com.slopdetector.core;

import com.slopdetector.parser.AstBuilder;
import com.github.javaparser.ast.CompilationUnit;
import java.util.Optional;

public class Engine {
    public static void main(String[] args) {
        System.out.println("SlopDetector Engine started.");

        AstBuilder builder = new AstBuilder();


        String fileToAnalyze = "TestCode.java"; 

        Optional<CompilationUnit> ast = builder.buildFromFile(fileToAnalyze);

        if (ast.isPresent()) {
            System.out.println("Επιτυχία! Το δέντρο δημιουργήθηκε.");
            // Test just if it works
            System.out.println(ast.get().toString());
        } else {
            System.out.println("Αποτυχία δημιουργίας δέντρου.");
        }
    }
}