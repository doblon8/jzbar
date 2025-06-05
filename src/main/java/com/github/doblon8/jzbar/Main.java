package com.github.doblon8.jzbar;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static com.github.doblon8.jzbar.bindings.zbar.zbar_version;

public class Main {
    public static void main(String[] args) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment major = arena.allocate(ValueLayout.JAVA_INT);
            MemorySegment minor = arena.allocate(ValueLayout.JAVA_INT);
            MemorySegment patch = arena.allocate(ValueLayout.JAVA_INT);
            int result = zbar_version(major, minor, patch);
            if (result != 0) {
                System.err.println("Failed to retrieve ZBar version.");
                return;
            }
            int majorVersion = major.get(ValueLayout.JAVA_INT, 0);
            int minorVersion = minor.get(ValueLayout.JAVA_INT, 0);
            int patchVersion = patch.get(ValueLayout.JAVA_INT, 0);
            System.out.println("ZBar version: " + majorVersion + "." + minorVersion + "." + patchVersion);
        }
    }
}