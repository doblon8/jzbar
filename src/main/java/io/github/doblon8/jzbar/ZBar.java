package io.github.doblon8.jzbar;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static io.github.doblon8.jzbar.bindings.zbar.C_INT;
import static io.github.doblon8.jzbar.bindings.zbar.zbar_version;

public class ZBar {

    /**
     * Retrieve runtime library version information.
     *
     * @return the version string in the format "major.minor.patch", e.g., "0.23.93"
     */
    public static String version() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment majorSegment = arena.allocate(C_INT);
            MemorySegment minorSegment = arena.allocate(C_INT);
            MemorySegment patchSegment = arena.allocate(C_INT);

            zbar_version(majorSegment, minorSegment, patchSegment); // always returns 0

            int major = majorSegment.get(C_INT, 0);
            int minor = minorSegment.get(C_INT, 0);
            int patch = patchSegment.get(C_INT, 0);

            return major + "." + minor + "." + patch;
        }
    }
}
