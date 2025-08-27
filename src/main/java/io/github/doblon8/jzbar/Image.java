package io.github.doblon8.jzbar;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;

import static io.github.doblon8.jzbar.bindings.zbar.*;
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

    /**
     * Specify the fourcc image format code for image sample data.
     * <br>
     * Refer to the documentation for supported formats.
     * <p>
     * Note: this does not convert the data!
     *
     * @param format four‑character code (e.g. {@code "Y800"})
     */
    public void setFormat(String format) {
        long fmt = parseFourcc(format);
        zbar_image_set_format(segment, fmt);
    }

    /**
     * Specify the pixel size of the image.
     * <p>
     * Note: this also resets the crop rectangle to the full image
     * ({@code 0, 0, width, height}).
     * <br>
     * Note: this does not affect the image data.
     *
     * @param width  image width in pixels
     * @param height image height in pixels
     */
    public void setSize(int width, int height) {
        zbar_image_set_size(segment, width, height);
    }

    /**
     * Specify the image sample data.
     * <p>
     * Note: application image data will not be modified by the library.
     *
     * @param data raw image data buffer
     */
    public void setData(byte[] data) {
        MemorySegment dataSegment = arena.allocateFrom(JAVA_BYTE, data);
        zbar_image_set_data(segment, dataSegment, data.length, MemorySegment.NULL);
    }

    /**
     * Returns the first symbol in the image.
     *
     * @return the first symbol, or {@code null} if no symbols are found
     */
    public Symbol getFirstSymbol() {
        MemorySegment symbolSegment = zbar_image_first_symbol(segment);
        if (symbolSegment.address() == 0) {
            return null;
        }
        return new Symbol(symbolSegment);
    }

    /**
     * Returns a list of all symbols found in the image.
     *
     * @return a list of symbols, which may be empty if no symbols are found
     */
    public List<Symbol> getSymbols() {
        List<Symbol> symbols = new ArrayList<>();
        Symbol symbol = getFirstSymbol();
        while (symbol != null) {
            symbols.add(symbol);
            symbol = symbol.next();
        }
        return symbols;
    }

    /**
     * Returns the fourcc format code of the image.
     *
     * @param format four‑character code (e.g. {@code "Y800"})
     * @return the fourcc format code as a long
     * @throws IllegalArgumentException if the format is not 4 characters
     */
    private long parseFourcc(String format) {
        int length = format.length();
        if (length != 4) {
            throw new IllegalArgumentException("Format must be 4 characters");
        }
        long fourcc = 0;
        for (int i = 0; i < length; i++) {
            fourcc |= ((long) format.charAt(i) & 0xFF) << (i * 8);
        }
        return fourcc;
    }

    @Override
    public void close() {
        if (segment != MemorySegment.NULL) {
            zbar_image_destroy(segment);
        }
        arena.close();
    }
}
