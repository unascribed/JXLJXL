// Generated by jextract

package com.unascribed.jxljxl.panama;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.lang.foreign.*;
import static java.lang.foreign.ValueLayout.*;
/**
 * {@snippet :
 * struct {
 *     uint64_t extensions;
 * };
 * }
 */
public class JxlHeaderExtensions {

    static final StructLayout $struct$LAYOUT = MemoryLayout.structLayout(
        Constants$root.C_LONG_LONG$LAYOUT.withName("extensions")
    );
    public static MemoryLayout $LAYOUT() {
        return JxlHeaderExtensions.$struct$LAYOUT;
    }
    static final VarHandle extensions$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("extensions"));
    public static VarHandle extensions$VH() {
        return JxlHeaderExtensions.extensions$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * uint64_t extensions;
     * }
     */
    public static long extensions$get(MemorySegment seg) {
        return (long)JxlHeaderExtensions.extensions$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * uint64_t extensions;
     * }
     */
    public static void extensions$set(MemorySegment seg, long x) {
        JxlHeaderExtensions.extensions$VH.set(seg, x);
    }
    public static long extensions$get(MemorySegment seg, long index) {
        return (long)JxlHeaderExtensions.extensions$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void extensions$set(MemorySegment seg, long index, long x) {
        JxlHeaderExtensions.extensions$VH.set(seg.asSlice(index*sizeof()), x);
    }
    public static long sizeof() { return $LAYOUT().byteSize(); }
    public static MemorySegment allocate(SegmentAllocator allocator) { return allocator.allocate($LAYOUT()); }
    public static MemorySegment allocateArray(long len, SegmentAllocator allocator) {
        return allocator.allocate(MemoryLayout.sequenceLayout(len, $LAYOUT()));
    }
    public static MemorySegment ofAddress(MemorySegment addr, SegmentScope scope) { return RuntimeHelper.asArray(addr, $LAYOUT(), 1, scope); }
}

