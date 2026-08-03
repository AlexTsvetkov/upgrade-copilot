package com.sapcommercetools.upgrade.examples;

import com.sapcommercetools.upgrade.DeprecationRules;
import com.sapcommercetools.upgrade.DeprecationScanner;
import com.sapcommercetools.upgrade.Finding;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A runnable, self-contained mini-tutorial for <b>upgrade-copilot</b>.
 *
 * <p>It builds a scanner pre-loaded with the default SAP Commerce upgrade rules
 * and runs it over a few in-memory "source files" (plain strings) that contain
 * the kinds of code an upgrade would flag: a JUnit 3/4 {@code junit.framework}
 * import, {@code @Autowired} field injection, a legacy single-arg
 * {@code getAttribute("...")} call, and a {@code @Deprecated} marker. It then
 * prints the prioritized findings and the grouped report.
 *
 * <p>Run it with:
 * <pre>{@code
 * find src/main/java -name '*.java' | xargs javac -d /tmp/ex-upgrade
 * java -cp /tmp/ex-upgrade com.sapcommercetools.upgrade.examples.Example
 * }</pre>
 */
public final class Example {

    private Example() {
    }

    public static void main(String[] args) {
        section("1. Build a scanner with the default SAP Commerce rules");
        // DeprecationRules.defaultScanner() returns a DeprecationScanner already
        // loaded with the illustrative default ruleset. You could also start
        // from `new DeprecationScanner()` and call addRule(...) yourself.
        DeprecationScanner scanner = DeprecationRules.defaultScanner();
        System.out.println("Loaded rules:");
        scanner.rules().forEach(r -> System.out.println("  - " + r.id()
                + " [" + r.severity() + "]"));

        section("2. Provide a few sample source files (as strings)");
        // Each entry is (path -> file content). The scanner is line-oriented and
        // language-agnostic: it applies every rule's regex to every line.
        Map<String, String> files = new LinkedHashMap<>();

        // A legacy JUnit 3/4 test: the junit.framework import is a BLOCKER under
        // JUnit 5, and TestCase is 1970s-style testing.
        files.put("LegacyTest.java", String.join("\n",
                "package com.acme.commerce;",
                "",
                "import junit.framework.TestCase;", // -> junit-3-4-import (BLOCKER)
                "",
                "public class LegacyTest extends TestCase {",
                "    public void testNothing() { }",
                "}"));

        // A Spring service using field injection and a legacy session accessor.
        files.put("CartService.java", String.join("\n",
                "package com.acme.commerce;",
                "",
                "public class CartService {",
                "",
                "    @Autowired",                                  // -> autowired-field-injection (INFO)
                "    private SessionService sessionService;",
                "",
                "    public Object current() {",
                "        return sessionService.getAttribute(\"cart\");", // -> legacy getAttribute (BLOCKER)
                "    }",
                "}"));

        // A model class exposing a @Deprecated accessor.
        files.put("ProductModel.java", String.join("\n",
                "package com.acme.commerce;",
                "",
                "public class ProductModel {",
                "    @Deprecated",                                 // -> platform-deprecated-marker (WARNING)
                "    public String getOldCode() { return null; }",
                "}"));

        files.forEach((path, content) -> System.out.println("  provided " + path
                + " (" + content.split("\n").length + " lines)"));

        section("3. Scan all files and inspect the prioritized findings");
        // scanAll(...) processes files in ascending path order and returns raw,
        // unprioritized findings. prioritize(...) sorts them BLOCKER > WARNING >
        // INFO, then by file/line/column — the order a reviewer should tackle.
        List<Finding> raw = scanner.scanAll(files);
        List<Finding> prioritized = scanner.prioritize(raw);
        System.out.println("Total findings: " + prioritized.size()
                + " (shown highest-priority first)");
        int n = 1;
        for (Finding f : prioritized) {
            System.out.println("  " + (n++) + ". " + f.display());
        }

        section("4. Print the grouped, human-readable report()");
        // report(...) is what you'd surface to a developer: a summary line, per
        // severity totals, and findings grouped under BLOCKER / WARNING / INFO.
        System.out.println(scanner.report(raw));

        System.out.println("Done.");
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }
}
