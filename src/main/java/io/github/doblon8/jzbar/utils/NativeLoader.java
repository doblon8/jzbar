package io.github.doblon8.jzbar.utils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
        String[] windowsLibs = {"iconv-2.dll", "zbar.dll"};
        String[] linuxLibs = {"libzbar.so"};
        String[] macLibs = {"libzbar.dylib"};

        String[] libs = switch (os) {
            case "windows" -> windowsLibs;
            case "linux" -> linuxLibs;
            case "osx" -> macLibs;
            default -> throw new UnsupportedOperationException("Unsupported OS: " + os);
        };

        try {
            Path tempDir = Files.createTempDirectory("zbar-native");
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
