// Generated by jextract

package com.unascribed.jxljxl.panama;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.lang.foreign.*;
import static java.lang.foreign.ValueLayout.*;
/**
 * {@snippet :
 * void (*jpegxl_free_func)(void* opaque,void* address);
 * }
 */
public interface jpegxl_free_func {

    void apply(java.lang.foreign.MemorySegment opaque, java.lang.foreign.MemorySegment address);
    static MemorySegment allocate(jpegxl_free_func fi, SegmentScope scope) {
        return RuntimeHelper.upcallStub(constants$2.jpegxl_free_func_UP$MH, fi, constants$2.jpegxl_free_func$FUNC, scope);
    }
    static jpegxl_free_func ofAddress(MemorySegment addr, SegmentScope scope) {
        MemorySegment symbol = MemorySegment.ofAddress(addr.address(), 0, scope);
        return (java.lang.foreign.MemorySegment _opaque, java.lang.foreign.MemorySegment _address) -> {
            try {
                constants$2.jpegxl_free_func_DOWN$MH.invokeExact(symbol, _opaque, _address);
            } catch (Throwable ex$) {
                throw new AssertionError("should not reach here", ex$);
            }
        };
    }
}


