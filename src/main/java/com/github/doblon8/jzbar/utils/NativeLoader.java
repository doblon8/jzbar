package com.github.doblon8.jzbar.utils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class NativeLoader {
    public static void loadZBar() {
        String osName = System.getProperty("os.name").toLowerCase();
        String libraryPath;

        if (osName.contains("nux")) {
            libraryPath = "/zbar/linux/libzbar.so";
        } else if (osName.contains("mac")) {
            libraryPath = "/zbar/macos/libzbar.dylib";
        } else if (osName.contains("win")) {
            libraryPath = "/zbar/windows/zbar.dll";
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + osName);
        }

        try (InputStream inputStream = NativeLoader.class.getResourceAsStream(libraryPath)) {
            if (inputStream == null) {
                throw new IllegalStateException("Native library not found: " + libraryPath);
            }
            Path tempFile = Files.createTempFile("zbar", libraryPath.substring(libraryPath.lastIndexOf('.')));
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            System.load(tempFile.toAbsolutePath().toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load native library: " + libraryPath, e);
        }
    }
}
