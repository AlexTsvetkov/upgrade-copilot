package com.sapcommercetools.upgrade;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DeprecationScannerTest {

    private final DeprecationScanner subject = new DeprecationScanner();

    @Test
    void describes_itself() {
        assertTrue(subject.describe().startsWith("upgrade-copilot"));
    }

    @Test
    void accepts_non_blank_input() {
        assertTrue(subject.accepts("cart-123"));
        assertFalse(subject.accepts(" "));
        assertFalse(subject.accepts(null));
    }
}
