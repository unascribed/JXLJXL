// Generated by jextract

package com.unascribed.jxljxl.panama;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.lang.foreign.*;
import static java.lang.foreign.ValueLayout.*;
/**
 * {@snippet :
 * int (*JxlParallelRunInit)(void* jpegxl_opaque,unsigned long num_threads);
 * }
 */
public interface JxlParallelRunInit {

    int apply(java.lang.foreign.MemorySegment jpegxl_opaque, long num_threads);
    static MemorySegment allocate(JxlParallelRunInit fi, SegmentScope scope) {
        return RuntimeHelper.upcallStub(constants$2.JxlParallelRunInit_UP$MH, fi, constants$2.JxlParallelRunInit$FUNC, scope);
    }
    static JxlParallelRunInit ofAddress(MemorySegment addr, SegmentScope scope) {
        MemorySegment symbol = MemorySegment.ofAddress(addr.address(), 0, scope);
        return (java.lang.foreign.MemorySegment _jpegxl_opaque, long _num_threads) -> {
            try {
                return (int)constants$2.JxlParallelRunInit_DOWN$MH.invokeExact(symbol, _jpegxl_opaque, _num_threads);
            } catch (Throwable ex$) {
                throw new AssertionError("should not reach here", ex$);
            }
        };
    }
}

