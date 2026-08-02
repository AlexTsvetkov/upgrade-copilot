package com.sapcommercetools.upgrade;

/**
 * Analyzes a codebase against a target-version knowledge base and emits a prioritized, evidence-backed impact report.
 *
 * <p>This is the core abstraction of <b>upgrade-copilot</b>. The starter implementation
 * below is intentionally minimal — a foundation that documents the intended
 * contract and gives tests something real to exercise.
 */
public final class DeprecationScanner {

    /**
     * Returns a human-readable description of what this component does.
     * Replace with the real behaviour as the project grows.
     */
    public String describe() {
        return "upgrade-copilot: AI-assisted version-upgrade and deprecation engine for SAP Commerce — turn a multi-month upgrade project into a tool-driven workflow.";
    }

    /**
     * Placeholder for the primary operation. Kept trivial and total so the
     * scaffold builds and tests pass on a clean checkout.
     *
     * @param input a caller-supplied token
     * @return {@code true} when the input is non-blank
     */
    public boolean accepts(String input) {
        return input != null && !input.isBlank();
    }
}
