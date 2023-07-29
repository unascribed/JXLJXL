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
 *     JxlBlendMode blendmode;
 *     uint32_t source;
 *     uint32_t alpha;
 *     int clamp;
 * };
 * }
 */
public class JxlBlendInfo {

    static final StructLayout $struct$LAYOUT = MemoryLayout.structLayout(
        Constants$root.C_INT$LAYOUT.withName("blendmode"),
        Constants$root.C_INT$LAYOUT.withName("source"),
        Constants$root.C_INT$LAYOUT.withName("alpha"),
        Constants$root.C_INT$LAYOUT.withName("clamp")
    );
    public static MemoryLayout $LAYOUT() {
        return JxlBlendInfo.$struct$LAYOUT;
    }
    static final VarHandle blendmode$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("blendmode"));
    public static VarHandle blendmode$VH() {
        return JxlBlendInfo.blendmode$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * JxlBlendMode blendmode;
     * }
     */
    public static int blendmode$get(MemorySegment seg) {
        return (int)JxlBlendInfo.blendmode$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * JxlBlendMode blendmode;
     * }
     */
    public static void blendmode$set(MemorySegment seg, int x) {
        JxlBlendInfo.blendmode$VH.set(seg, x);
    }
    public static int blendmode$get(MemorySegment seg, long index) {
        return (int)JxlBlendInfo.blendmode$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void blendmode$set(MemorySegment seg, long index, int x) {
        JxlBlendInfo.blendmode$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle source$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("source"));
    public static VarHandle source$VH() {
        return JxlBlendInfo.source$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * uint32_t source;
     * }
     */
    public static int source$get(MemorySegment seg) {
        return (int)JxlBlendInfo.source$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * uint32_t source;
     * }
     */
    public static void source$set(MemorySegment seg, int x) {
        JxlBlendInfo.source$VH.set(seg, x);
    }
    public static int source$get(MemorySegment seg, long index) {
        return (int)JxlBlendInfo.source$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void source$set(MemorySegment seg, long index, int x) {
        JxlBlendInfo.source$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle alpha$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("alpha"));
    public static VarHandle alpha$VH() {
        return JxlBlendInfo.alpha$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * uint32_t alpha;
     * }
     */
    public static int alpha$get(MemorySegment seg) {
        return (int)JxlBlendInfo.alpha$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * uint32_t alpha;
     * }
     */
    public static void alpha$set(MemorySegment seg, int x) {
        JxlBlendInfo.alpha$VH.set(seg, x);
    }
    public static int alpha$get(MemorySegment seg, long index) {
        return (int)JxlBlendInfo.alpha$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void alpha$set(MemorySegment seg, long index, int x) {
        JxlBlendInfo.alpha$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle clamp$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("clamp"));
    public static VarHandle clamp$VH() {
        return JxlBlendInfo.clamp$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * int clamp;
     * }
     */
    public static int clamp$get(MemorySegment seg) {
        return (int)JxlBlendInfo.clamp$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * int clamp;
     * }
     */
    public static void clamp$set(MemorySegment seg, int x) {
        JxlBlendInfo.clamp$VH.set(seg, x);
    }
    public static int clamp$get(MemorySegment seg, long index) {
        return (int)JxlBlendInfo.clamp$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void clamp$set(MemorySegment seg, long index, int x) {
        JxlBlendInfo.clamp$VH.set(seg.asSlice(index*sizeof()), x);
    }
    public static long sizeof() { return $LAYOUT().byteSize(); }
    public static MemorySegment allocate(SegmentAllocator allocator) { return allocator.allocate($LAYOUT()); }
    public static MemorySegment allocateArray(long len, SegmentAllocator allocator) {
        return allocator.allocate(MemoryLayout.sequenceLayout(len, $LAYOUT()));
    }
    public static MemorySegment ofAddress(MemorySegment addr, SegmentScope scope) { return RuntimeHelper.asArray(addr, $LAYOUT(), 1, scope); }
}

