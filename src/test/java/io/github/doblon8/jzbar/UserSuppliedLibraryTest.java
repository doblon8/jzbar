package io.github.doblon8.jzbar;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** A library on java.library.path must be used instead of the bundled copy. */
class UserSuppliedLibraryTest {

    @Test
    void userSuppliedLibraryWinsOverBundled() throws IOException {
        String libName = System.mapLibraryName("zbar");
        Optional<Path> userLib = Arrays.stream(System.getProperty("java.library.path").split(File.pathSeparator))
                .filter(dir -> !dir.isEmpty())
                .map(dir -> Path.of(dir, libName))
                .filter(Files::isRegularFile)
                .findFirst();

        if (Boolean.getBoolean("jzbar.test.requireUserLib")) {
            assertTrue(userLib.isPresent(), "no " + libName + " on java.library.path");
        }
        assumeTrue(userLib.isPresent(), "no user-supplied " + libName + " on java.library.path");

        ZBar.version(); // triggers library loading

        // Linux: only the user's library may be mapped into the process.
        Path maps = Path.of("/proc/self/maps");
        if (Files.exists(maps)) {
            Set<String> mapped = Files.readAllLines(maps).stream()
                    .filter(l -> l.contains("/" + libName))
                    .map(l -> l.substring(l.indexOf('/')))
                    .collect(Collectors.toSet());
            assertEquals(Set.of(userLib.get().toRealPath().toString()), mapped,
                    "only the user-supplied " + libName + " must be loaded");
        }

        // All platforms: the bundled copy is extracted to a zbar-native-* dir in java.io.tmpdir.
        // The margin absorbs the imprecision of the process start time.
        Instant since = ProcessHandle.current().info().startInstant().orElseThrow().minusSeconds(2);
        try (Stream<Path> entries = Files.list(Path.of(System.getProperty("java.io.tmpdir")))) {
            List<Path> extracted = entries
                    .filter(p -> p.getFileName().toString().startsWith("zbar-native-"))
                    .filter(p -> modifiedAfter(p, since))
                    .toList();
            assertTrue(extracted.isEmpty(), "bundled copy was extracted: " + extracted);
        }
    }

    private static boolean modifiedAfter(Path path, Instant since) {
        try {
            return Files.getLastModifiedTime(path).toInstant().isAfter(since);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
