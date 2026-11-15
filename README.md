# ai-slop-detector-java

Static analyzer that scores Java source files for traits typical of AI-generated code.
It parses each file into an AST with [JavaParser](https://javaparser.org/) (Java 21 syntax supported)
and combines three heuristics into a single **slop score** between 0% and 100%.

## Build and run

Requires JDK 21+ and Maven.

```bash
mvn package
java -jar target/slop-detector.jar [options] <file-or-directory>...
```

During development you can skip the jar:

```bash
mvn compile exec:java -Dexec.args="-v src/test/resources/samples"
```

| Option | Meaning |
|---|---|
| `-t, --threshold <0..1>` | Score at which a file is flagged (default `0.35`) |
| `-v, --verbose` | Print every finding with its line number |
| `-h, --help` | Show usage |

Directories are scanned recursively for `.java` files.
Exit codes: `0` all files pass, `1` at least one file flagged, `2` usage or parse error, so the tool can gate a CI job.

Example:

```
[FLAGGED] src/test/resources/samples/AiStyleSample.java  slop score 42%
    Generic naming        40%
        - line 6: generic name 'temp'
    Narrating comments    53%
        - line 5: comment restates code: "Initialize temp variable to zero"
    Nested branching      30%
        - line 15: getUserStatus() complexity 6, nesting depth 3
```

## Heuristics

| Metric | Weight | What it looks for |
|---|---|---|
| **Generic naming** (`NamingEntropyAnalyzer`) | 0.35 | Variables and parameters whose every word is generic (`data`, `temp`, `tempList`, `resultObj`). `userData` passes. Single-letter names are ignored. Files with fewer than 4 names are scored down because the ratio is noisy. |
| **Narrating comments** (`CommentDensityAnalyzer`) | 0.35 | Comments that restate the code they sit on (`// Initialize temp variable to zero` above `int temp = 0;`), measured by word overlap with the commented node. License headers are ignored. A smaller part of the score comes from comment density above 15% of lines. |
| **Nested branching** (`CyclomaticComplexityAnalyzer`) | 0.30 | McCabe complexity above 5 and nesting depth above 2 per method or constructor. `else if` chains do not add depth. The file score blends the average and the worst method so one bad method is not diluted. |

The final score is the weighted average. These are heuristics, not proof of authorship: treat a flag as a prompt for review, not a verdict.

## Project layout

```
src/main/java/com/slopdetector/
  core/     Engine (CLI), SlopDetector (runs analyzers, combines scores), FileReport
  parser/   AstBuilder (JavaParser configured for Java 21)
  metrics/  Analyzer interface, MetricResult and the three analyzers
src/test/
  java/                 JUnit 5 tests
  resources/samples/    AI-style, human-style and modern-Java fixtures
```

## Adding a heuristic

1. Implement `com.slopdetector.metrics.Analyzer` (`name()`, `weight()`, `analyze(CompilationUnit)`), returning a score in [0, 1] and line-numbered findings.
2. Register it in `SlopDetector.defaultAnalyzers()`.
3. Add unit tests, and check that `SlopDetectorTest` still flags the AI sample and passes the human sample.
