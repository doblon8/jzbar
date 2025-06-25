package io.github.doblon8.jzbar;

import java.lang.foreign.MemorySegment;

import static io.github.doblon8.jzbar.bindings.zbar.*;

public class ImageScanner implements AutoCloseable {
    private final MemorySegment segment;

    public ImageScanner() {
        this.segment = zbar_image_scanner_create();
    }

    public int scanImage(Image image) throws ZBarException {
        int scanResult = zbar_scan_image(segment, image.segment());
        if (scanResult == -1) {
            throw new ZBarException("Error scanning image");
        }
        return scanResult;
    }

    public void setConfig(int symbology, int config, int value) throws ZBarException {
        int status = zbar_image_scanner_set_config(segment, symbology, config, value);
        if (status != 0) {
            throw new ZBarException("Failed to set config");
        }
    }

    @Override
    public void close() {
        if (segment != MemorySegment.NULL) {
            zbar_image_scanner_destroy(segment);
        }
    }
}
