// Generated by jextract

package com.unascribed.jxljxl.panama;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.lang.foreign.*;
import static java.lang.foreign.ValueLayout.*;
/**
 * {@snippet :
 * int (*jpegxl_cms_run_func)(void* user_data,unsigned long thread,float* input_buffer,float* output_buffer,unsigned long num_pixels);
 * }
 */
public interface jpegxl_cms_run_func {

    int apply(java.lang.foreign.MemorySegment user_data, long thread, java.lang.foreign.MemorySegment input_buffer, java.lang.foreign.MemorySegment output_buffer, long num_pixels);
    static MemorySegment allocate(jpegxl_cms_run_func fi, SegmentScope scope) {
        return RuntimeHelper.upcallStub(constants$1.jpegxl_cms_run_func_UP$MH, fi, constants$1.jpegxl_cms_run_func$FUNC, scope);
    }
    static jpegxl_cms_run_func ofAddress(MemorySegment addr, SegmentScope scope) {
        MemorySegment symbol = MemorySegment.ofAddress(addr.address(), 0, scope);
        return (java.lang.foreign.MemorySegment _user_data, long _thread, java.lang.foreign.MemorySegment _input_buffer, java.lang.foreign.MemorySegment _output_buffer, long _num_pixels) -> {
            try {
                return (int)constants$1.jpegxl_cms_run_func_DOWN$MH.invokeExact(symbol, _user_data, _thread, _input_buffer, _output_buffer, _num_pixels);
            } catch (Throwable ex$) {
                throw new AssertionError("should not reach here", ex$);
            }
        };
    }
}

