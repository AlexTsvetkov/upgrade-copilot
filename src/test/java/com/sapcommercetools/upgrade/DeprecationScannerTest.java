package com.sapcommercetools.upgrade;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class DeprecationScannerTest {

    @Test
    void customRuleMatchesWithCorrectLineAndColumn() {
        DeprecationScanner scanner = new DeprecationScanner();
        scanner.addRule(new DeprecationRule(
                "todo-marker", Pattern.compile("TODO"), Severity.INFO,
                "Leftover TODO marker.", "Resolve or ticket the TODO."));

        String content = "line one\n  x = TODO;\nlast";
        List<Finding> findings = scanner.scanFile("Sample.java", content);

        assertEquals(1, findings.size(), "one TODO expected");
        Finding f = findings.get(0);
        assertEquals("todo-marker", f.ruleId());
        assertEquals("Sample.java", f.file());
        assertEquals(2, f.line(), "TODO is on the 2nd line");
        assertEquals(7, f.column(), "TODO starts at column 7 (1-based)");
        assertEquals("TODO", f.snippet());
        assertEquals(Severity.INFO, f.severity());
    }

    @Test
    void multipleRulesAcrossMultipleFilesViaScanAll() {
        DeprecationScanner scanner = new DeprecationScanner();
        scanner.addRule(DeprecationRule.of("has-foo", "foo", Severity.WARNING, "foo found", "remove foo"));
        scanner.addRule(DeprecationRule.of("has-bar", "bar", Severity.BLOCKER, "bar found", "remove bar"));

        Map<String, String> files = new LinkedHashMap<>();
        files.put("B.java", "bar here\nnothing");
        files.put("A.java", "foo here\nfoo again");

        List<Finding> findings = scanner.scanAll(files);
        assertEquals(3, findings.size(), "two foo + one bar");

        long fooCount = findings.stream().filter(x -> x.ruleId().equals("has-foo")).count();
        long barCount = findings.stream().filter(x -> x.ruleId().equals("has-bar")).count();
        assertEquals(2, fooCount);
        assertEquals(1, barCount);

        // scanAll processes files in ascending path order: A.java before B.java.
        assertEquals("A.java", findings.get(0).file());
    }

    @Test
    void prioritizeOrdersBlockerFirstThenByFileAndLine() {
        DeprecationScanner scanner = new DeprecationScanner();
        List<Finding> raw = List.of(
                new Finding("r-info", "Z.java", 5, 1, "i", Severity.INFO, "m", "fix"),
                new Finding("r-block", "Z.java", 9, 1, "b", Severity.BLOCKER, "m", "fix"),
                new Finding("r-warn", "A.java", 2, 1, "w", Severity.WARNING, "m", "fix"),
                new Finding("r-block2", "A.java", 1, 1, "b", Severity.BLOCKER, "m", "fix"));

        List<Finding> ordered = scanner.prioritize(raw);

        assertEquals(Severity.BLOCKER, ordered.get(0).severity());
        assertEquals(Severity.BLOCKER, ordered.get(1).severity());
        // Within BLOCKER, A.java (line 1) sorts before Z.java (line 9).
        assertEquals("A.java", ordered.get(0).file());
        assertEquals("Z.java", ordered.get(1).file());
        assertEquals(Severity.WARNING, ordered.get(2).severity());
        assertEquals(Severity.INFO, ordered.get(3).severity());
    }

    @Test
    void defaultRulesetFindsJunitImportAndAutowired() {
        DeprecationScanner scanner = DeprecationRules.defaultScanner();
        String src = String.join("\n",
                "package demo;",
                "import junit.framework.TestCase;",
                "public class Legacy {",
                "  @Autowired",
                "  private Service service;",
                "}");

        List<Finding> findings = scanner.scanFile("Legacy.java", src);

        boolean junit = findings.stream().anyMatch(f -> f.ruleId().equals("junit-3-4-import"));
        boolean autowired = findings.stream().anyMatch(f -> f.ruleId().equals("autowired-field-injection"));
        assertTrue(junit, "should flag junit.framework import");
        assertTrue(autowired, "should flag @Autowired field injection");

        Finding junitFinding = findings.stream()
                .filter(f -> f.ruleId().equals("junit-3-4-import")).findFirst().orElseThrow();
        assertEquals(Severity.BLOCKER, junitFinding.severity());
        assertEquals(2, junitFinding.line());
    }

    @Test
    void reportContainsCountsAndGroupsBlockerFirst() {
        DeprecationScanner scanner = DeprecationRules.defaultScanner();
        String src = String.join("\n",
                "import junit.framework.TestCase;",
                "@Autowired",
                "field;");

        List<Finding> findings = scanner.scanFile("R.java", src);
        String report = scanner.report(findings);

        assertTrue(report.contains("BLOCKER="), "report has a BLOCKER count");
        assertTrue(report.contains("INFO="), "report has an INFO count");
        assertTrue(report.contains("junit-3-4-import"), "report names the junit rule");

        // BLOCKER group header must appear before the INFO group header.
        int blockerIdx = report.indexOf("BLOCKER (");
        int infoIdx = report.indexOf("INFO (");
        assertTrue(blockerIdx >= 0 && infoIdx >= 0, "both groups present");
        assertTrue(blockerIdx < infoIdx, "BLOCKER group precedes INFO group");
    }
}
