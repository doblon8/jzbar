package com.github.doblon8.jzbar;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static com.github.doblon8.jzbar.bindings.zbar.*;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;

public class Image implements AutoCloseable {
    private final MemorySegment segment;
    private final Arena arena;

    public Image() {
        this.segment = zbar_image_create();
        this.arena = Arena.ofConfined();
    }

    MemorySegment segment() {
        return segment;
    }

    public void setFormat(long format) {
        zbar_image_set_format(segment, format);
    }

    public void setSize(int width, int height) {
        zbar_image_set_size(segment, width, height);
    }

    public void setData(byte[] data) {
        MemorySegment dataSegment = arena.allocateFrom(JAVA_BYTE, data);
        zbar_image_set_data(segment, dataSegment, data.length, MemorySegment.NULL);
    }

    @Override
    public void close() {
        if (segment != MemorySegment.NULL) {
            zbar_image_destroy(segment);
        }
        arena.close();
    }
}
