package io.github.doblon8.jzbar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@EnabledOnOs(OS.LINUX)
class UserSuppliedLibraryTest {

    @Test
    void userSuppliedLibraryWinsOverBundled() throws IOException {
        Optional<Path> userLib = Arrays.stream(System.getProperty("java.library.path").split(File.pathSeparator))
                .filter(dir -> !dir.isEmpty())
                .map(dir -> Path.of(dir, "libzbar.so"))
                .filter(Files::isRegularFile)
                .findFirst();

        if (Boolean.getBoolean("jzbar.test.requireUserLib")) {
            assertTrue(userLib.isPresent(), "no libzbar.so on java.library.path");
        }
        assumeTrue(userLib.isPresent(), "no user-supplied libzbar.so on java.library.path");

        ZBar.version(); // triggers library loading

        List<String> maps = Files.readAllLines(Path.of("/proc/self/maps"));
        Set<String> mapped = maps.stream()
                .filter(l -> l.contains("/libzbar.so"))
                .map(l -> l.substring(l.indexOf('/')))
                .collect(Collectors.toSet());
        assertEquals(Set.of(userLib.get().toRealPath().toString()), mapped,
                "only the user-supplied libzbar.so must be loaded");
    }
}
