package io.github.doblon8.jzbar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ZBarTest {

    @Test
    void version() {
        assertNotNull(ZBar.version(), "Version string should not be null");
    }
}