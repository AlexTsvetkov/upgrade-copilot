package com.sapcommercetools.upgrade;

/**
 * Factory for a small, illustrative default knowledge base of upgrade rules.
 *
 * <p>The rules here are deliberately <b>generic and illustrative</b> — they
 * demonstrate the kinds of checks an SAP Commerce upgrade would run without
 * reproducing any vendor-specific or confidential detection logic. Real
 * deployments would extend or replace this set via
 * {@link DeprecationScanner#addRule(DeprecationRule)}.
 */
public final class DeprecationRules {

    private DeprecationRules() {
    }

    /**
     * Builds a scanner pre-loaded with {@link #defaultSapCommerceRules() the
     * default ruleset}.
     *
     * @return a ready-to-use scanner
     */
    public static DeprecationScanner defaultScanner() {
        DeprecationScanner scanner = new DeprecationScanner();
        for (DeprecationRule rule : defaultSapCommerceRules()) {
            scanner.addRule(rule);
        }
        return scanner;
    }

    /**
     * Returns a fresh list of generic example rules covering common upgrade
     * concerns: deprecated markers, an outdated service call signature, a
     * JUnit 3/4 -> 5 migration hint, and a field-injection style suggestion.
     *
     * @return a new, mutable list of rules
     */
    public static java.util.List<DeprecationRule> defaultSapCommerceRules() {
        java.util.List<DeprecationRule> rules = new java.util.ArrayList<>();

        // Illustrative: a "@Deprecated" marker coming from a platform package.
        rules.add(DeprecationRule.of(
                "platform-deprecated-marker",
                "@Deprecated",
                Severity.WARNING,
                "Usage annotated as @Deprecated; the referenced API may be removed in a future platform release.",
                "Locate the replacement API in the target-version migration notes and update the call site."));

        // Illustrative old signature: SessionService.getAttribute(String) taking a single arg.
        rules.add(DeprecationRule.of(
                "sessionservice-getattribute-legacy",
                "getAttribute\\s*\\(\\s*\"[^\"]*\"\\s*\\)",
                Severity.BLOCKER,
                "Single-argument getAttribute(String) is a legacy accessor signature and no longer compiles against the target API.",
                "Migrate to the typed getAttribute(String, T defaultValue) overload or a dedicated typed accessor."));

        // JUnit 3/4 -> JUnit 5 migration.
        rules.add(DeprecationRule.of(
                "junit-3-4-import",
                "import\\s+junit\\.framework\\.",
                Severity.BLOCKER,
                "Imports the JUnit 3/4 'junit.framework' package, which is unavailable under JUnit 5 (Jupiter).",
                "Replace with 'org.junit.jupiter.api.*' imports and migrate assertions to org.junit.jupiter.api.Assertions."));

        // Field injection suggestion.
        rules.add(DeprecationRule.of(
                "autowired-field-injection",
                "@Autowired",
                Severity.INFO,
                "Field injection via @Autowired hinders testability and immutability.",
                "Prefer constructor injection: inject dependencies as final constructor parameters."));

        return rules;
    }
}
