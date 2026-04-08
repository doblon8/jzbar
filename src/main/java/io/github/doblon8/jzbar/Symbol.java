package io.github.doblon8.jzbar;

import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;

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
     * Retrieve a symbol confidence metric.
     *
     * @return an unscaled, relative quantity: larger values are better than smaller values,
     * where "large" and "small" are application dependent.
     */
    public int getQuality() {
        return zbar_symbol_get_quality(segment);
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
     * Retrieve type of decoded symbol.
     *
     * @return the symbol type as a String, e.g., "QR-Code", "EAN-13", "CODE-128",
     * or "UNKNOWN" if the encoding is not recognized
     */
    public String getType() {
        int type = zbar_symbol_get_type(segment);
        return zbar_get_symbol_name(type).getString(0);
    }

    /** Retrieve the location polygon.
     * <p>
     * The location polygon defines the image area that the symbol was extracted from.
     *
     * @return a list of points representing the vertices of the location polygon.
     * The number of points can vary depending on the symbol type and the quality of the scan.
     * For example, a QR code may have 4 points corresponding to its corners,
     * while a linear barcode may have 2 points corresponding to its endpoints.
     */
    public List<Point> getLocationPolygon() {
        int numberOfPoints = zbar_symbol_get_loc_size(segment);
        List<Point> points = new ArrayList<>(numberOfPoints);
        for (int i = 0; i < numberOfPoints; i++) {
            int x = zbar_symbol_get_loc_x(segment, i);
            int y = zbar_symbol_get_loc_y(segment, i);
            points.add(new Point(x, y));
        }
        return points;
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
