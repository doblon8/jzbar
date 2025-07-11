package io.github.doblon8.jzbar;

import java.lang.foreign.MemorySegment;

import static io.github.doblon8.jzbar.bindings.zbar.*;

public class Symbol {
    private final MemorySegment segment;

    public Symbol(MemorySegment segment) {
        this.segment = segment;
    }

    /**
     * Retrieve data decoded from symbol.
     *
     * @return the decoded data as a String, or null if no data is available
     */
    public String getData() {
        MemorySegment dataSegment = zbar_symbol_get_data(segment);
        if (dataSegment.address() == 0) {
            return null;
        }
        return dataSegment.getString(0);
    }

    /**
     * Retrieve general orientation of decoded symbol.
     *
     * @return the orientation as a String, e.g., "UP", "RIGHT", "DOWN", "LEFT"
     */
    public String getOrientation() {
        int orientation = zbar_symbol_get_orientation(segment);
        return zbar_get_orientation_name(orientation).getString(0);
    }

    /**
     * Iterate the set to which this symbol belongs (there can be only one).
     *
     * @return the next symbol in the set, or null if there are no more symbols
     */
    public Symbol next() {
        MemorySegment nextSegment = zbar_symbol_next(segment);
        if (nextSegment.address() == 0) {
            return null;
        }
        return new Symbol(nextSegment);
    }
}
