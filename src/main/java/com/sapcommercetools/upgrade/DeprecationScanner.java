package com.sapcommercetools.upgrade;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * Scans source text against a knowledge base of {@link DeprecationRule}s and
 * emits a prioritized, evidence-backed impact report.
 *
 * <p>This is the core abstraction of <b>upgrade-copilot</b>. The scanner is a
 * pure, line-oriented text analyzer: it does not parse Java, it applies each
 * registered rule's regular expression to every line of a file and records a
 * {@link Finding} for each match with a 1-based line and column. That keeps the
 * engine language-agnostic and dependency-free while still surfacing the
 * concrete evidence a reviewer needs.
 *
 * <p>Usage:
 * <pre>{@code
 * DeprecationScanner scanner = DeprecationRules.defaultScanner();
 * List<Finding> findings = scanner.scanFile("Foo.java", source);
 * System.out.println(scanner.report(findings));
 * }</pre>
 */
public final class DeprecationScanner {

    private final List<DeprecationRule> rules = new ArrayList<>();

    /**
     * Registers a rule. Rules are applied in registration order; ordering of
     * the final report is governed by {@link #prioritize(List)} rather than by
     * insertion order.
     *
     * @param rule the rule to add (non-null)
     * @return this scanner, for chaining
     */
    public DeprecationScanner addRule(DeprecationRule rule) {
        rules.add(Objects.requireNonNull(rule, "rule"));
        return this;
    }

    /**
     * Returns an unmodifiable view of the currently registered rules.
     *
     * @return the rules
     */
    public List<DeprecationRule> rules() {
        return List.copyOf(rules);
    }

    /**
     * Scans a single file's content, applying every registered rule to each
     * line. Multiple matches of the same rule on one line each produce a
     * separate finding.
     *
     * <p>Line splitting recognises {@code \n}, {@code \r\n} and {@code \r}
     * terminators. Line and column numbers are 1-based; the column is the
     * 1-based index of the first character of the matched snippet.
     *
     * @param path    the file path recorded on each finding
     * @param content the full text of the file (may be empty)
     * @return findings in the order rule-by-rule, line-by-line (unprioritized)
     */
    public List<Finding> scanFile(String path, String content) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(content, "content");

        List<Finding> findings = new ArrayList<>();
        String[] lines = content.split("\r\n|\r|\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;
            for (DeprecationRule rule : rules) {
                Matcher matcher = rule.pattern().matcher(line);
                while (matcher.find()) {
                    int column = matcher.start() + 1; // 1-based
                    String snippet = matcher.group();
                    findings.add(new Finding(
                            rule.id(), path, lineNumber, column, snippet,
                            rule.severity(), rule.message(), rule.suggestedFix()));
                    // Guard against zero-width matches to avoid an infinite loop.
                    if (matcher.end() == matcher.start()) {
                        if (matcher.end() >= line.length()) {
                            break;
                        }
                        matcher.region(matcher.end() + 1, line.length());
                    }
                }
            }
        }
        return findings;
    }

    /**
     * Scans a set of files. Files are processed in ascending path order so the
     * raw output is deterministic regardless of map iteration order.
     *
     * @param files map of file path to file content
     * @return the aggregated, unprioritized findings across all files
     */
    public List<Finding> scanAll(Map<String, String> files) {
        Objects.requireNonNull(files, "files");
        List<Finding> all = new ArrayList<>();
        files.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> all.addAll(scanFile(e.getKey(), e.getValue())));
        return all;
    }

    /**
     * Comparator ordering findings BLOCKER &gt; WARNING &gt; INFO, then by file
     * path, then by line, then by column. Because {@link Severity} is declared
     * most-severe first, comparing by {@link Enum#ordinal()} yields the desired
     * order.
     */
    private static final Comparator<Finding> PRIORITY =
            Comparator.<Finding>comparingInt(f -> f.severity().ordinal())
                    .thenComparing(Finding::file)
                    .thenComparingInt(Finding::line)
                    .thenComparingInt(Finding::column)
                    .thenComparing(Finding::ruleId);

    /**
     * Returns a new list of the given findings sorted by priority: BLOCKER
     * first, then WARNING, then INFO, breaking ties by file, line and column.
     *
     * @param findings the findings to sort (not modified)
     * @return a new, sorted list
     */
    public List<Finding> prioritize(List<Finding> findings) {
        List<Finding> sorted = new ArrayList<>(findings);
        sorted.sort(PRIORITY);
        return sorted;
    }

    /**
     * Renders a human-readable report grouped by severity (BLOCKER, WARNING,
     * INFO), with a per-severity count header and each finding rendered via
     * {@link Finding#display()}. Empty severity groups are omitted, and a
     * summary total line is always present.
     *
     * @param findings the findings to report on
     * @return the formatted multi-line report
     */
    public String report(List<Finding> findings) {
        Objects.requireNonNull(findings, "findings");
        List<Finding> ordered = prioritize(findings);

        Map<Severity, List<Finding>> grouped = new LinkedHashMap<>();
        for (Severity s : Severity.values()) {
            grouped.put(s, new ArrayList<>());
        }
        for (Finding f : ordered) {
            grouped.get(f.severity()).add(f);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Upgrade impact report: ")
                .append(findings.size())
                .append(" finding(s)\n");
        sb.append("Totals: ")
                .append("BLOCKER=").append(grouped.get(Severity.BLOCKER).size())
                .append(", WARNING=").append(grouped.get(Severity.WARNING).size())
                .append(", INFO=").append(grouped.get(Severity.INFO).size())
                .append('\n');

        for (Severity s : Severity.values()) {
            List<Finding> group = grouped.get(s);
            if (group.isEmpty()) {
                continue;
            }
            sb.append('\n').append(s).append(" (").append(group.size()).append(")\n");
            for (Finding f : group) {
                sb.append("  ").append(f.display()).append('\n');
            }
        }
        return sb.toString();
    }
}
