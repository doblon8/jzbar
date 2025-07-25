// Generated by jextract

package io.github.doblon8.jzbar.bindings;

import java.lang.foreign.*;
import java.util.function.*;

import static java.lang.foreign.ValueLayout.*;
import static java.lang.foreign.MemoryLayout.PathElement.*;

/**
 * {@snippet lang=c :
 * struct video_controls_s {
 *     char *name;
 *     char *group;
 *     video_control_type_t type;
 *     int64_t min;
 *     int64_t max;
 *     int64_t def;
 *     uint64_t step;
 *     unsigned int menu_size;
 *     video_control_menu_t *menu;
 *     void *next;
 * }
 * }
 */
public class video_controls_s {

    video_controls_s() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        zbar.C_POINTER.withName("name"),
        zbar.C_POINTER.withName("group"),
        zbar.C_INT.withName("type"),
        MemoryLayout.paddingLayout(4),
        zbar.C_LONG.withName("min"),
        zbar.C_LONG.withName("max"),
        zbar.C_LONG.withName("def"),
        zbar.C_LONG.withName("step"),
        zbar.C_INT.withName("menu_size"),
        MemoryLayout.paddingLayout(4),
        zbar.C_POINTER.withName("menu"),
        zbar.C_POINTER.withName("next")
    ).withName("video_controls_s");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final AddressLayout name$LAYOUT = (AddressLayout)$LAYOUT.select(groupElement("name"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * char *name
     * }
     */
    public static final AddressLayout name$layout() {
        return name$LAYOUT;
    }

    private static final long name$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * char *name
     * }
     */
    public static final long name$offset() {
        return name$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * char *name
     * }
     */
    public static MemorySegment name(MemorySegment struct) {
        return struct.get(name$LAYOUT, name$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * char *name
     * }
     */
    public static void name(MemorySegment struct, MemorySegment fieldValue) {
        struct.set(name$LAYOUT, name$OFFSET, fieldValue);
    }

    private static final AddressLayout group$LAYOUT = (AddressLayout)$LAYOUT.select(groupElement("group"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * char *group
     * }
     */
    public static final AddressLayout group$layout() {
        return group$LAYOUT;
    }

    private static final long group$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * char *group
     * }
     */
    public static final long group$offset() {
        return group$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * char *group
     * }
     */
    public static MemorySegment group(MemorySegment struct) {
        return struct.get(group$LAYOUT, group$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * char *group
     * }
     */
    public static void group(MemorySegment struct, MemorySegment fieldValue) {
        struct.set(group$LAYOUT, group$OFFSET, fieldValue);
    }

    private static final OfInt type$LAYOUT = (OfInt)$LAYOUT.select(groupElement("type"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * video_control_type_t type
     * }
     */
    public static final OfInt type$layout() {
        return type$LAYOUT;
    }

    private static final long type$OFFSET = 16;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * video_control_type_t type
     * }
     */
    public static final long type$offset() {
        return type$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * video_control_type_t type
     * }
     */
    public static int type(MemorySegment struct) {
        return struct.get(type$LAYOUT, type$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * video_control_type_t type
     * }
     */
    public static void type(MemorySegment struct, int fieldValue) {
        struct.set(type$LAYOUT, type$OFFSET, fieldValue);
    }

    private static final OfLong min$LAYOUT = (OfLong)$LAYOUT.select(groupElement("min"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * int64_t min
     * }
     */
    public static final OfLong min$layout() {
        return min$LAYOUT;
    }

    private static final long min$OFFSET = 24;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * int64_t min
     * }
     */
    public static final long min$offset() {
        return min$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * int64_t min
     * }
     */
    public static long min(MemorySegment struct) {
        return struct.get(min$LAYOUT, min$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * int64_t min
     * }
     */
    public static void min(MemorySegment struct, long fieldValue) {
        struct.set(min$LAYOUT, min$OFFSET, fieldValue);
    }

    private static final OfLong max$LAYOUT = (OfLong)$LAYOUT.select(groupElement("max"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * int64_t max
     * }
     */
    public static final OfLong max$layout() {
        return max$LAYOUT;
    }

    private static final long max$OFFSET = 32;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * int64_t max
     * }
     */
    public static final long max$offset() {
        return max$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * int64_t max
     * }
     */
    public static long max(MemorySegment struct) {
        return struct.get(max$LAYOUT, max$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * int64_t max
     * }
     */
    public static void max(MemorySegment struct, long fieldValue) {
        struct.set(max$LAYOUT, max$OFFSET, fieldValue);
    }

    private static final OfLong def$LAYOUT = (OfLong)$LAYOUT.select(groupElement("def"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * int64_t def
     * }
     */
    public static final OfLong def$layout() {
        return def$LAYOUT;
    }

    private static final long def$OFFSET = 40;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * int64_t def
     * }
     */
    public static final long def$offset() {
        return def$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * int64_t def
     * }
     */
    public static long def(MemorySegment struct) {
        return struct.get(def$LAYOUT, def$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * int64_t def
     * }
     */
    public static void def(MemorySegment struct, long fieldValue) {
        struct.set(def$LAYOUT, def$OFFSET, fieldValue);
    }

    private static final OfLong step$LAYOUT = (OfLong)$LAYOUT.select(groupElement("step"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t step
     * }
     */
    public static final OfLong step$layout() {
        return step$LAYOUT;
    }

    private static final long step$OFFSET = 48;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t step
     * }
     */
    public static final long step$offset() {
        return step$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t step
     * }
     */
    public static long step(MemorySegment struct) {
        return struct.get(step$LAYOUT, step$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t step
     * }
     */
    public static void step(MemorySegment struct, long fieldValue) {
        struct.set(step$LAYOUT, step$OFFSET, fieldValue);
    }

    private static final OfInt menu_size$LAYOUT = (OfInt)$LAYOUT.select(groupElement("menu_size"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int menu_size
     * }
     */
    public static final OfInt menu_size$layout() {
        return menu_size$LAYOUT;
    }

    private static final long menu_size$OFFSET = 56;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int menu_size
     * }
     */
    public static final long menu_size$offset() {
        return menu_size$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int menu_size
     * }
     */
    public static int menu_size(MemorySegment struct) {
        return struct.get(menu_size$LAYOUT, menu_size$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int menu_size
     * }
     */
    public static void menu_size(MemorySegment struct, int fieldValue) {
        struct.set(menu_size$LAYOUT, menu_size$OFFSET, fieldValue);
    }

    private static final AddressLayout menu$LAYOUT = (AddressLayout)$LAYOUT.select(groupElement("menu"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * video_control_menu_t *menu
     * }
     */
    public static final AddressLayout menu$layout() {
        return menu$LAYOUT;
    }

    private static final long menu$OFFSET = 64;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * video_control_menu_t *menu
     * }
     */
    public static final long menu$offset() {
        return menu$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * video_control_menu_t *menu
     * }
     */
    public static MemorySegment menu(MemorySegment struct) {
        return struct.get(menu$LAYOUT, menu$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * video_control_menu_t *menu
     * }
     */
    public static void menu(MemorySegment struct, MemorySegment fieldValue) {
        struct.set(menu$LAYOUT, menu$OFFSET, fieldValue);
    }

    private static final AddressLayout next$LAYOUT = (AddressLayout)$LAYOUT.select(groupElement("next"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * void *next
     * }
     */
    public static final AddressLayout next$layout() {
        return next$LAYOUT;
    }

    private static final long next$OFFSET = 72;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * void *next
     * }
     */
    public static final long next$offset() {
        return next$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * void *next
     * }
     */
    public static MemorySegment next(MemorySegment struct) {
        return struct.get(next$LAYOUT, next$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * void *next
     * }
     */
    public static void next(MemorySegment struct, MemorySegment fieldValue) {
        struct.set(next$LAYOUT, next$OFFSET, fieldValue);
    }

    /**
     * Obtains a slice of {@code arrayParam} which selects the array element at {@code index}.
     * The returned segment has address {@code arrayParam.address() + index * layout().byteSize()}
     */
    public static MemorySegment asSlice(MemorySegment array, long index) {
        return array.asSlice(layout().byteSize() * index);
    }

    /**
     * The size (in bytes) of this struct
     */
    public static long sizeof() { return layout().byteSize(); }

    /**
     * Allocate a segment of size {@code layout().byteSize()} using {@code allocator}
     */
    public static MemorySegment allocate(SegmentAllocator allocator) {
        return allocator.allocate(layout());
    }

    /**
     * Allocate an array of size {@code elementCount} using {@code allocator}.
     * The returned segment has size {@code elementCount * layout().byteSize()}.
     */
    public static MemorySegment allocateArray(long elementCount, SegmentAllocator allocator) {
        return allocator.allocate(MemoryLayout.sequenceLayout(elementCount, layout()));
    }

    /**
     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).
     * The returned segment has size {@code layout().byteSize()}
     */
    public static MemorySegment reinterpret(MemorySegment addr, Arena arena, Consumer<MemorySegment> cleanup) {
        return reinterpret(addr, 1, arena, cleanup);
    }

    /**
     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).
     * The returned segment has size {@code elementCount * layout().byteSize()}
     */
    public static MemorySegment reinterpret(MemorySegment addr, long elementCount, Arena arena, Consumer<MemorySegment> cleanup) {
        return addr.reinterpret(layout().byteSize() * elementCount, arena, cleanup);
    }
}

