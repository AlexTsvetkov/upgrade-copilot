package com.sapcommercetools.upgrade;

import java.util.Objects;

/**
 * An evidence-backed hit produced when a {@link DeprecationRule} matches a line
 * of source.
 *
 * <p>A finding is a value object that carries everything a reviewer needs to
 * locate and act on the issue: the originating {@code ruleId}, the {@code file}
 * and 1-based {@code line}/{@code column} of the match, the matched
 * {@code snippet}, and the rule's {@link Severity}, {@code message} and
 * {@code suggestedFix} copied for convenience so a finding is self-contained.
 */
public final class Finding {

    private final String ruleId;
    private final String file;
    private final int line;
    private final int column;
    private final String snippet;
    private final Severity severity;
    private final String message;
    private final String suggestedFix;

    /**
     * Creates a finding.
     *
     * @param ruleId       id of the rule that matched
     * @param file         path of the scanned file
     * @param line         1-based line number of the match
     * @param column       1-based column number of the match start
     * @param snippet      the exact matched text
     * @param severity     impact classification
     * @param message      explanation of the issue
     * @param suggestedFix recommended remediation
     */
    public Finding(String ruleId, String file, int line, int column, String snippet,
                   Severity severity, String message, String suggestedFix) {
        this.ruleId = Objects.requireNonNull(ruleId, "ruleId");
        this.file = Objects.requireNonNull(file, "file");
        this.line = line;
        this.column = column;
        this.snippet = Objects.requireNonNull(snippet, "snippet");
        this.severity = Objects.requireNonNull(severity, "severity");
        this.message = Objects.requireNonNull(message, "message");
        this.suggestedFix = Objects.requireNonNull(suggestedFix, "suggestedFix");
    }

    public String ruleId() {
        return ruleId;
    }

    public String file() {
        return file;
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }

    public String snippet() {
        return snippet;
    }

    public Severity severity() {
        return severity;
    }

    public String message() {
        return message;
    }

    public String suggestedFix() {
        return suggestedFix;
    }

    /**
     * Renders a single-line, human-readable representation such as
     * {@code [BLOCKER] Foo.java:12:5 junit-3-4-import - <message> -> <fix>}.
     *
     * @return a display string for reports
     */
    public String display() {
        return "[" + severity + "] " + file + ":" + line + ":" + column + " " + ruleId
                + " - " + message + " -> " + suggestedFix;
    }

    @Override
    public String toString() {
        return display();
    }
}
