package com.sapcommercetools.upgrade;

/**
 * Impact classification for a {@link Finding}.
 *
 * <p>The declaration order is the priority order used throughout the scanner:
 * {@link #BLOCKER} is the most severe and must be addressed before an upgrade
 * can proceed, {@link #WARNING} should be reviewed, and {@link #INFO} is
 * advisory. Because Java assigns {@link Enum#ordinal()} in declaration order,
 * a simple {@code Comparator.comparingInt(Severity::ordinal)} sorts most-severe
 * first.
 */
public enum Severity {
    /** Compilation- or runtime-breaking usage that blocks the upgrade. */
    BLOCKER,
    /** Behavioural risk or deprecated-but-working usage that should be reviewed. */
    WARNING,
    /** Advisory / stylistic hint with no upgrade risk. */
    INFO
}
