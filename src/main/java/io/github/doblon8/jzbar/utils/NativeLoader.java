package io.github.doblon8.jzbar.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
            Path tempDir = Files.createTempDirectory("zbar-native");
            tempDir.toFile().deleteOnExit();

            for (String lib : libs) {
                try (InputStream in = NativeLoader.class.getResourceAsStream(basePath + lib)) {
                    if (in == null) {
                        throw new IllegalStateException("Missing native lib: " + basePath + lib);
                    }

                    // Create an exclusive temp file
                    String suffix = lib.contains(".") ? lib.substring(lib.lastIndexOf('.')) : null;
                    Path tempFile = Files.createTempFile(tempDir, "lib-", suffix);

                    // Set restrictive permissions on the temp file
                    try {
                        Set<PosixFilePermission> perms = EnumSet.of(
                                PosixFilePermission.OWNER_READ,
                                PosixFilePermission.OWNER_WRITE
                        );
                        Files.setPosixFilePermissions(tempFile, perms);
                    } catch (UnsupportedOperationException ignored) {
                        // Not a POSIX FS / platform; fall through to fallback logic
                        File f = tempFile.toFile();
                        // Note: Using bitwise & (not &&) to ensure both methods run
                        boolean ok = f.setReadable(true, true) &
                                f.setWritable(true, true);
                        if (!ok) {
                            throw new IOException("Failed to set owner-only permissions on temp file: " + tempFile);
                        }
                    }

                    // Copy the library to the temp file
                    try (OutputStream out = Files.newOutputStream(tempFile, StandardOpenOption.WRITE)) {
                        in.transferTo(out);
                        out.flush();
                    }

                    // Ensure the temp file is deleted on exit
                    tempFile.toFile().deleteOnExit();

                    // Load the library
                    System.load(tempFile.toAbsolutePath().toString());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load native libraries", e);
        }
    }

    /**
     * Create a temporary directory with owner-only permissions.
     * <p>
     * This method attempts to create a temporary directory with permissions
     * that restrict access to the owner only. It first tries to use POSIX
     * file permissions, then falls back to ACLs on Windows, and finally
     * uses the java.io.File API as a last resort.
     *
     * @param prefix the prefix string to be used in generating the directory's name; may be null
     * @return the path to the newly created temporary directory
     * @throws IOException if an I/O error occurs or if setting permissions fails
     */
    public static Path createOwnerOnlyTempDir(String prefix) throws IOException {
        Path tmpRoot = Paths.get(System.getProperty("java.io.tmpdir"));

        // POSIX: try atomic creation with owner-only perms (rwx------)
        try {
            Set<PosixFilePermission> posixPerms = EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE
            );
            return Files.createTempDirectory(tmpRoot, prefix, PosixFilePermissions.asFileAttribute(posixPerms));
        } catch (UnsupportedOperationException e) {
            // Not a POSIX FS / platform; fall through to fallback logic
        }

        // Fallback: create the dir, then try ACLs (Windows) or File API
        Path dir = Files.createTempDirectory(tmpRoot, prefix);

        // Try ACL view (best-effort)
        try {
            AclFileAttributeView aclView = Files.getFileAttributeView(dir, AclFileAttributeView.class);
            if (aclView != null) {
                UserPrincipalLookupService lookup = dir.getFileSystem().getUserPrincipalLookupService();
                String userName = System.getProperty("user.name");
                UserPrincipal user = lookup.lookupPrincipalByName(userName);

                // Allow full control to current user only
                Set<AclEntryPermission> allPerms = EnumSet.allOf(AclEntryPermission.class);
                AclEntry allow = AclEntry.newBuilder()
                        .setType(AclEntryType.ALLOW)
                        .setPrincipal(user)
                        .setPermissions(allPerms)
                        .build();

                aclView.setAcl(Collections.singletonList(allow));
                return dir;
            }
        } catch (Exception ignored) {
            // ACLs not available or failed — fall back to File permission API
        }

        // Final fallback: best-effort owner-only via java.io.File flags
        File f = dir.toFile();
        // Note: Using bitwise & (not &&) to ensure all three methods run
        boolean ok =
                f.setWritable(true, true) &
                        f.setReadable(true, true) &
                        f.setExecutable(true, true);

        if (!ok) {
            throw new IOException("Failed to set owner-only permissions on temp directory: " + dir);
        }
        return dir;
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
