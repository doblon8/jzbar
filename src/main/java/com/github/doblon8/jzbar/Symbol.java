package com.github.doblon8.jzbar;

import java.lang.foreign.MemorySegment;

import static com.github.doblon8.jzbar.bindings.zbar.zbar_symbol_get_data;
import static com.github.doblon8.jzbar.bindings.zbar.zbar_symbol_next;

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

    public Symbol next() {
        MemorySegment nextSegment = zbar_symbol_next(segment);
        if (nextSegment.address() == 0) {
            return null;
        }
        return new Symbol(nextSegment);
    }
}
