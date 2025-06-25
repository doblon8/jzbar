package io.github.doblon8.jzbar;

import java.lang.foreign.MemorySegment;

import static io.github.doblon8.jzbar.bindings.zbar.*;

public class Symbol {
    private final MemorySegment segment;

    public Symbol(MemorySegment segment) {
        this.segment = segment;
    }

    public String getData() {
        MemorySegment dataSegment = zbar_symbol_get_data(segment);
        if (dataSegment.address() == 0) {
            return null;
        }
        return dataSegment.getString(0);
    }

    public String getOrientation() {
        int orientation = zbar_symbol_get_orientation(segment);
        return zbar_get_orientation_name(orientation).getString(0);
    }

    public Symbol next() {
        MemorySegment nextSegment = zbar_symbol_next(segment);
        if (nextSegment.address() == 0) {
            return null;
        }
        return new Symbol(nextSegment);
    }
}
