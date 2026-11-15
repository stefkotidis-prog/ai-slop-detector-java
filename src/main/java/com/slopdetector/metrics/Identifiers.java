package com.slopdetector.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Text helpers shared by the analyzers. */
final class Identifiers {

    private Identifiers() {
    }

    /**
     * Splits an identifier into lowercase word tokens.
     * {@code "tempList2"} becomes {@code [temp, list]}, {@code "MAX_RETRY_COUNT"} becomes {@code [max, retry, count]}.
     */
    static List<String> split(String identifier) {
        List<String> tokens = new ArrayList<>();
        String spaced = identifier
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2")
                .replaceAll("[^A-Za-z]+", " ");
        for (String part : spaced.trim().split("\\s+")) {
            if (!part.isEmpty()) {
                tokens.add(part.toLowerCase(Locale.ROOT));
            }
        }
        return tokens;
    }

    /** Very small suffix-stripping stemmer, good enough to match "processes" with "process". */
    static String stem(String word) {
        if (word.length() > 5 && word.endsWith("ing")) {
            return word.substring(0, word.length() - 3);
        }
        if (word.length() > 4 && word.endsWith("es")) {
            return word.substring(0, word.length() - 2);
        }
        if (word.length() > 4 && word.endsWith("ed")) {
            return word.substring(0, word.length() - 2);
        }
        if (word.length() > 3 && word.endsWith("s") && !word.endsWith("ss")) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }
}
