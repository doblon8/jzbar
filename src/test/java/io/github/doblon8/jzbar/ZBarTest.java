package io.github.doblon8.jzbar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ZBarTest {

    @Test
    void version() {
        assertEquals("0.23.93", ZBar.version(), "Bundled ZBar version");
    }
}