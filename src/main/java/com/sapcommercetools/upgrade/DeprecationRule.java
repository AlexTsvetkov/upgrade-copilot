package com.sapcommercetools.upgrade;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A single entry in the deprecation/impact knowledge base.
 *
 * <p>A rule pairs a compiled regular expression with the metadata the scanner
 * needs to turn a match into an actionable {@link Finding}: a stable
 * {@code id}, an impact {@link Severity}, a human-readable {@code message}
 * explaining why the pattern is problematic, and a {@code suggestedFix}
 * describing the recommended remediation.
 *
 * <p>Instances are immutable. The {@link Pattern} is applied per source line by
 * {@link DeprecationScanner}, so patterns should not rely on multi-line
 * constructs.
 */
public final class DeprecationRule {

    private final String id;
    private final Pattern pattern;
    private final Severity severity;
    private final String message;
    private final String suggestedFix;

    /**
     * Creates a rule.
     *
     * @param id           stable, unique identifier (e.g. {@code "junit-3-4-import"})
     * @param pattern      compiled pattern applied to each source line
     * @param severity     impact classification of a match
     * @param message      explanation of why the match is a problem
     * @param suggestedFix recommended remediation
     */
    public DeprecationRule(String id, Pattern pattern, Severity severity, String message, String suggestedFix) {
        this.id = Objects.requireNonNull(id, "id");
        this.pattern = Objects.requireNonNull(pattern, "pattern");
        this.severity = Objects.requireNonNull(severity, "severity");
        this.message = Objects.requireNonNull(message, "message");
        this.suggestedFix = Objects.requireNonNull(suggestedFix, "suggestedFix");
    }

    /**
     * Convenience factory that compiles the given regex with default flags.
     *
     * @param id           stable, unique identifier
     * @param regex        regular expression source
     * @param severity     impact classification of a match
     * @param message      explanation of why the match is a problem
     * @param suggestedFix recommended remediation
     * @return a new rule
     */
    public static DeprecationRule of(String id, String regex, Severity severity, String message, String suggestedFix) {
        return new DeprecationRule(id, Pattern.compile(regex), severity, message, suggestedFix);
    }

    public String id() {
        return id;
    }

    public Pattern pattern() {
        return pattern;
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

    @Override
    public String toString() {
        return "DeprecationRule[" + id + ", " + severity + "]";
    }
}
