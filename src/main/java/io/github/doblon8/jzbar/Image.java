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

    public void setFormat(String format) {
        long fmt = parseFourcc(format);
        zbar_image_set_format(segment, fmt);
    }

    public void setSize(int width, int height) {
        zbar_image_set_size(segment, width, height);
    }

    public void setData(byte[] data) {
        MemorySegment dataSegment = arena.allocateFrom(JAVA_BYTE, data);
        zbar_image_set_data(segment, dataSegment, data.length, MemorySegment.NULL);
    }

    public Symbol getFirstSymbol() {
        MemorySegment symbolSegment = zbar_image_first_symbol(segment);
        if (symbolSegment.address() == 0) {
            return null;
        }
        return new Symbol(symbolSegment);
    }

    public List<Symbol> getSymbols() {
        List<Symbol> symbols = new ArrayList<>();
        Symbol symbol = getFirstSymbol();
        while (symbol != null) {
            symbols.add(symbol);
            symbol = symbol.next();
        }
        return symbols;
    }

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
