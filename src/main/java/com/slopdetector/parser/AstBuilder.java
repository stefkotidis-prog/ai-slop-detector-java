package com.slopdetector.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.ast.CompilationUnit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Parses Java source code into an Abstract Syntax Tree (AST).
 *
 * <p>Uses a dedicated {@link JavaParser} instance configured for Java 21, so modern
 * syntax (records, switch pattern matching, text blocks, sealed types) is supported.
 */
public class AstBuilder {

    private final JavaParser parser;

    public AstBuilder() {
        this(LanguageLevel.JAVA_21);
    }

    public AstBuilder(LanguageLevel languageLevel) {
        ParserConfiguration config = new ParserConfiguration()
                .setLanguageLevel(languageLevel)
                .setAttributeComments(true);
        this.parser = new JavaParser(config);
    }

    /** Parses a source file. Returns empty (and logs the reason) when the file is missing or invalid. */
    public Optional<CompilationUnit> buildFromFile(Path file) {
        try {
            return buildFromSource(Files.readString(file), file.toString());
        } catch (NoSuchFileException e) {
            System.err.println("[ERROR] File not found: " + file);
        } catch (IOException e) {
            System.err.println("[ERROR] Cannot read " + file + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<CompilationUnit> buildFromFile(String filePath) {
        return buildFromFile(Path.of(filePath));
    }

    /** Parses source text. {@code origin} is only used in error messages. */
    public Optional<CompilationUnit> buildFromSource(String source, String origin) {
        ParseResult<CompilationUnit> result = parser.parse(source);
        if (result.isSuccessful() && result.getResult().isPresent()) {
            return result.getResult();
        }
        String reason = result.getProblems().isEmpty()
                ? "unknown parse error"
                : result.getProblems().get(0).getVerboseMessage().lines().findFirst().orElse("");
        System.err.println("[ERROR] Failed to parse " + origin + ": " + reason);
        return Optional.empty();
    }
}
