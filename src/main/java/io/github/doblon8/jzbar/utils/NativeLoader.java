package io.github.doblon8.jzbar.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.*;
import java.security.MessageDigest;
import java.util.*;


public class NativeLoader {

    // Map of library filenames to their expected SHA-256 hashes (in hex)
    private static final Map<String, String> LIB_HASHES = Map.ofEntries(
            // Linux
            Map.entry("linux-aarch64-libzbar.so", "ed61c047c092d26ccad94193b83d44acca7d4e5583c0c38302f5b7130135e786"),
            Map.entry("linux-x64-libzbar.so", "4f62893ae033aba5cbeaa48f41559716fda1522daad7eb98692ad3397b00d85f"),

            // macOS
            Map.entry("osx-aarch64-libzbar.dylib", "c667d65ceac184fa9a651dfe7cab398343ad3009082e618531a27f3fdb2cfb18"),
            Map.entry("osx-x64-libzbar.dylib", "338b57fddcd72dacf68900d5b0507fee8226e8201606eb33e190cc3708a1458c"),

            // Windows
            Map.entry("windows-x64-iconv-2.dll", "c33c49d95e5800f47d34e0da7e32a7ae583f9e3e39b3030c5dc320c950642ff6"),
            Map.entry("windows-x64-zbar.dll", "c68e4dcb1bd6cd7ba3e0972f2b32192fa02c09315ee0eb93799cb0e444fe9956")
    );

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
            Path tempDir = createOwnerOnlyTempDir("zbar-native");
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

                    // Copy library bytes to temp file and compute SHA-256
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    try (OutputStream outputStream = Files.newOutputStream(tempFile, StandardOpenOption.WRITE)) {
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, read);
                            md.update(buffer, 0, read);
                        }
                        outputStream.flush();
                    }

                    // Verify SHA-256 digest
                    String key = os + "-" + arch + "-" + lib;
                    String expectedHex = LIB_HASHES.get(key);
                    if (expectedHex == null) throw new IllegalStateException("No SHA-256 entry for " + key);

                    String actualHex = bytesToHex(md.digest());
                    if (!actualHex.equalsIgnoreCase(expectedHex)) {
                        throw new SecurityException("SHA-256 mismatch for " + lib);
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
     * Convert a byte array to a hexadecimal string.
     *
     * @param digest the byte array to convert
     * @return the hexadecimal representation of the byte array
     */
    private static String bytesToHex(byte[] digest) {
        StringBuilder sb = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
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
