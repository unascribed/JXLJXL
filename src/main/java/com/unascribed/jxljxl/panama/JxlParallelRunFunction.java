// Generated by jextract

package com.unascribed.jxljxl.panama;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.lang.foreign.*;
import static java.lang.foreign.ValueLayout.*;
/**
 * {@snippet :
 * void (*JxlParallelRunFunction)(void* jpegxl_opaque,unsigned int value,unsigned long thread_id);
 * }
 */
public interface JxlParallelRunFunction {

    void apply(java.lang.foreign.MemorySegment jpegxl_opaque, int value, long thread_id);
    static MemorySegment allocate(JxlParallelRunFunction fi, SegmentScope scope) {
        return RuntimeHelper.upcallStub(constants$2.JxlParallelRunFunction_UP$MH, fi, constants$2.JxlParallelRunFunction$FUNC, scope);
    }
    static JxlParallelRunFunction ofAddress(MemorySegment addr, SegmentScope scope) {
        MemorySegment symbol = MemorySegment.ofAddress(addr.address(), 0, scope);
        return (java.lang.foreign.MemorySegment _jpegxl_opaque, int _value, long _thread_id) -> {
            try {
                constants$2.JxlParallelRunFunction_DOWN$MH.invokeExact(symbol, _jpegxl_opaque, _value, _thread_id);
            } catch (Throwable ex$) {
                throw new AssertionError("should not reach here", ex$);
            }
        };
    }
}


