package com.slopdetector.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;

/**
 * Responsible for parsing Java source code into an Abstract Syntax Tree (AST).
 */
public class AstBuilder {

    /**
     * Reads a source file and returns the root of its AST.
     * Uses Optional to safely handle missing files or parsing errors.
     *
     * @param filePath The absolute or relative path to the Java file.
     * @return An Optional containing the CompilationUnit (AST root), or empty if an error occurs.
     */
    public Optional<CompilationUnit> buildFromFile(String filePath) {
        try {
            // StaticJavaParser.parse is the core engine reading the raw text
            CompilationUnit cu = StaticJavaParser.parse(new File(filePath));
            return Optional.of(cu);
        } catch (FileNotFoundException e) {
            System.err.println("[ERROR] File not found at path: " + filePath);
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to parse file: " + filePath + ". Reason: " + e.getMessage());
            return Optional.empty();
        }
    }
}