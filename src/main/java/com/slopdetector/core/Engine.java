package com.slopdetector.core;

import com.slopdetector.metrics.MetricResult;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Command-line entry point.
 *
 * <pre>
 * java -jar slop-detector.jar [--threshold 0.35] [--verbose] &lt;file-or-directory&gt;...
 * </pre>
 *
 * Exit codes: 0 = all files pass, 1 = at least one file flagged, 2 = usage or parse error.
 */
public class Engine {

    static final int EXIT_OK = 0;
    static final int EXIT_FLAGGED = 1;
    static final int EXIT_ERROR = 2;

    private static final String USAGE = """
            Usage: slop-detector [options] <file-or-directory>...

            Scores Java source files for traits typical of AI-generated code.

            Options:
              -t, --threshold <0..1>  score at which a file is flagged (default %.2f)
              -v, --verbose           list every finding, not just the scores
              -h, --help              show this help
            """.formatted(SlopDetector.DEFAULT_THRESHOLD);

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    static int run(String[] args, PrintStream out, PrintStream err) {
        double threshold = SlopDetector.DEFAULT_THRESHOLD;
        boolean verbose = false;
        List<Path> targets = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h", "--help" -> {
                    out.print(USAGE);
                    return EXIT_OK;
                }
                case "-v", "--verbose" -> verbose = true;
                case "-t", "--threshold" -> {
                    if (i + 1 >= args.length) {
                        err.println("[ERROR] --threshold needs a value");
                        return EXIT_ERROR;
                    }
                    try {
                        threshold = Double.parseDouble(args[++i]);
                    } catch (NumberFormatException e) {
                        threshold = Double.NaN;
                    }
                    if (!(threshold >= 0 && threshold <= 1)) {
                        err.println("[ERROR] --threshold must be a number between 0 and 1");
                        return EXIT_ERROR;
                    }
                }
                default -> {
                    if (args[i].startsWith("-")) {
                        err.println("[ERROR] Unknown option: " + args[i]);
                        err.print(USAGE);
                        return EXIT_ERROR;
                    }
                    targets.add(Path.of(args[i]));
                }
            }
        }

        if (targets.isEmpty()) {
            err.print(USAGE);
            return EXIT_ERROR;
        }

        List<Path> files;
        try {
            files = SlopDetector.collectJavaFiles(targets);
        } catch (IOException e) {
            err.println("[ERROR] Cannot scan " + e.getMessage());
            return EXIT_ERROR;
        }
        if (files.isEmpty()) {
            err.println("[ERROR] No .java files found");
            return EXIT_ERROR;
        }

        SlopDetector detector = new SlopDetector(threshold);
        int flagged = 0;
        int failed = 0;
        for (Path file : files) {
            Optional<FileReport> report = detector.analyzeFile(file);
            if (report.isEmpty()) {
                failed++;
                continue;
            }
            print(report.get(), verbose, out);
            if (report.get().flagged()) {
                flagged++;
            }
        }

        int analysed = files.size() - failed;
        out.printf(Locale.ROOT, "%nSummary: %d analysed, %d flagged, %d failed (threshold %.0f%%)%n",
                analysed, flagged, failed, threshold * 100);

        if (failed > 0 && analysed == 0) {
            return EXIT_ERROR;
        }
        return flagged > 0 ? EXIT_FLAGGED : EXIT_OK;
    }

    private static void print(FileReport report, boolean verbose, PrintStream out) {
        out.printf(Locale.ROOT, "%n%s %s  slop score %.0f%%%n",
                report.flagged() ? "[FLAGGED]" : "[OK]     ", report.file(), report.score() * 100);
        for (MetricResult metric : report.metrics()) {
            out.printf(Locale.ROOT, "    %-20s %3.0f%%%n", metric.name(), metric.score() * 100);
            if (verbose) {
                metric.findings().forEach(f -> out.println("        - " + f));
            }
        }
    }
}
