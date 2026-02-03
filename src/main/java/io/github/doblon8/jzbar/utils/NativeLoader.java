package io.github.doblon8.jzbar.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class NativeLoader {
    /**
     * Load the native ZBar library based on the current operating system and architecture.
     * <p>
     * The library is loaded from the classpath and extracted to a temporary directory.
     *
     * @throws UnsupportedOperationException if the current OS or architecture is not supported
     * @throws RuntimeException              if an error occurs while loading the native library
     */
    public static void loadZBar() {
        String os = getOsName();
        String arch = getArchName();

        String basePath = "/native/" + os + "/" + arch + "/";
        String[] libs = switch (os) {
            case "windows" -> new String[]{"iconv-2.dll", "zbar.dll"};
            case "linux" -> new String[]{"libzbar.so"};
            case "osx" -> new String[]{"libzbar.dylib"};
            default -> throw new UnsupportedOperationException("Unsupported OS: " + os);
        };

        try {
            Path tempDir = createTempDir("zbar-native-");
            tempDir.toFile().deleteOnExit();

            for (String lib : libs) {
                try (InputStream in = NativeLoader.class.getResourceAsStream(basePath + lib)) {
                    if (in == null) {
                        throw new IllegalStateException("Missing native lib: " + basePath + lib);
                    }
                    Path out = tempDir.resolve(lib);
                    Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
                    out.toFile().deleteOnExit();
                    System.load(out.toAbsolutePath().toString());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load native libraries", e);
        }
    }

    /**
     * Create a temporary directory.
     *
     * @param prefix the prefix string to be used in generating the directory's name
     * @return the path to the created temporary directory
     * @throws IOException if an I/O error occurs or the temporary-directory could not be created
     */
    private static Path createTempDir(String prefix) throws IOException {
        Path tmpRoot = Paths.get(System.getProperty("java.io.tmpdir"));
        try {
            if (isPosixCompliant()) {
                Set<PosixFilePermission> ownerOnlyPerms = EnumSet.of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE,
                        PosixFilePermission.OWNER_EXECUTE
                );
                return Files.createTempDirectory(tmpRoot, prefix, PosixFilePermissions.asFileAttribute(ownerOnlyPerms));
            } else {
                return Files.createTempDirectory(tmpRoot, prefix);
            }
        } catch (IOException e) {
            throw new IOException("Failed to create temporary directory", e);
        }
    }

    /**
     * Check if the current file system is POSIX compliant.
     *
     * @return true if POSIX compliant, false otherwise
     */
    private static boolean isPosixCompliant() {
        try {
            return FileSystems.getDefault()
                    .supportedFileAttributeViews()
                    .contains("posix");
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Get the name of the current operating system.
     *
     * @return the OS name as a String, either "linux", "osx", or "windows"
     */
    private static String getOsName() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("nux")) {
            return "linux";
        } else if (os.contains("mac")) {
            return "osx";
        } else if (os.contains("win")) {
            return "windows";
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + os);
        }
    }

    /**
     * Get the name of the current architecture.
     *
     * @return the architecture name as a String, either "aarch64" or "x64"
     */
    private static String getArchName() {
        String arch = System.getProperty("os.arch").toLowerCase();
        if (arch.contains("aarch64") || arch.contains("arm64")) {
            return "aarch64";
        } else if (arch.contains("amd64") || arch.contains("x86_64")) {
            return "x64";
        } else {
            throw new UnsupportedOperationException("Unsupported architecture: " + arch);
        }
    }
}