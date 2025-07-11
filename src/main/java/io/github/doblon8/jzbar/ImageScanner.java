package io.github.doblon8.jzbar;

import java.lang.foreign.MemorySegment;

import static io.github.doblon8.jzbar.bindings.zbar.*;

public class ImageScanner implements AutoCloseable {
    private final MemorySegment segment;

    public ImageScanner() {
        this.segment = zbar_image_scanner_create();
    }

    /**
     * Scan for symbols in provided image.
     * The image format must be {@code "GRAY"}.
     *
     * @param image the image to scan
     * @return the number of symbols found in the image
     * @throws ZBarException if an error occurs during scanning
     */
    public int scanImage(Image image) throws ZBarException {
        int scanResult = zbar_scan_image(segment, image.segment());
        if (scanResult == -1) {
            throw new ZBarException("Error scanning image");
        }
        return scanResult;
    }

    /**
     * Set config for indicated symbology to specified value.
     *
     * @param symbology the symbology to configure (see {@link SymbolType}), or {@link SymbolType#NONE} for all
     * @param config the configuration option to set (see {@link Config})
     * @param value the value to set for the configuration option (1 for enabled, 0 for disabled)
     * @throws ZBarException if an error occurs while setting the configuration
     */
    public void setConfig(int symbology, int config, int value) throws ZBarException {
        int status = zbar_image_scanner_set_config(segment, symbology, config, value);
        if (status != 0) {
            throw new ZBarException("Failed to set config");
        }
    }

    /**
     * Set the configuration for a specific symbology.
     *
     * @param symbology the symbology to configure (see {@link SymbolType}), or {@link SymbolType#NONE} for all
     * @param enabled true to enable, false to disable
     * @throws ZBarException if an error occurs while setting the configuration
     */
    public void setConfig(int symbology, boolean enabled) throws ZBarException {
        int value = enabled ? 1 : 0;
        setConfig(symbology, Config.ENABLE, value);
    }

    @Override
    public void close() {
        if (segment != MemorySegment.NULL) {
            zbar_image_scanner_destroy(segment);
        }
    }
}
