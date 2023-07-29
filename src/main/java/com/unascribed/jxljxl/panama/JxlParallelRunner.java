// Generated by jextract

package com.unascribed.jxljxl.panama;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.lang.foreign.*;
import static java.lang.foreign.ValueLayout.*;
/**
 * {@snippet :
 * int (*JxlParallelRunner)(void* runner_opaque,void* jpegxl_opaque,int (*init)(void*,unsigned long),void (*func)(void*,unsigned int,unsigned long),unsigned int start_range,unsigned int end_range);
 * }
 */
public interface JxlParallelRunner {

    int apply(java.lang.foreign.MemorySegment runner_opaque, java.lang.foreign.MemorySegment jpegxl_opaque, java.lang.foreign.MemorySegment init, java.lang.foreign.MemorySegment func, int start_range, int end_range);
    static MemorySegment allocate(JxlParallelRunner fi, SegmentScope scope) {
        return RuntimeHelper.upcallStub(constants$3.JxlParallelRunner_UP$MH, fi, constants$3.JxlParallelRunner$FUNC, scope);
    }
    static JxlParallelRunner ofAddress(MemorySegment addr, SegmentScope scope) {
        MemorySegment symbol = MemorySegment.ofAddress(addr.address(), 0, scope);
        return (java.lang.foreign.MemorySegment _runner_opaque, java.lang.foreign.MemorySegment _jpegxl_opaque, java.lang.foreign.MemorySegment _init, java.lang.foreign.MemorySegment _func, int _start_range, int _end_range) -> {
            try {
                return (int)constants$3.JxlParallelRunner_DOWN$MH.invokeExact(symbol, _runner_opaque, _jpegxl_opaque, _init, _func, _start_range, _end_range);
            } catch (Throwable ex$) {
                throw new AssertionError("should not reach here", ex$);
            }
        };
    }
}

