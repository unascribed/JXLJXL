// Generated by jextract

package com.unascribed.jxljxl.panama;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.lang.foreign.*;
import static java.lang.foreign.ValueLayout.*;
/**
 * {@snippet :
 * void (*JxlImageOutDestroyCallback)(void* run_opaque);
 * }
 */
public interface JxlImageOutDestroyCallback {

    void apply(java.lang.foreign.MemorySegment run_opaque);
    static MemorySegment allocate(JxlImageOutDestroyCallback fi, SegmentScope scope) {
        return RuntimeHelper.upcallStub(constants$19.JxlImageOutDestroyCallback_UP$MH, fi, constants$19.JxlImageOutDestroyCallback$FUNC, scope);
    }
    static JxlImageOutDestroyCallback ofAddress(MemorySegment addr, SegmentScope scope) {
        MemorySegment symbol = MemorySegment.ofAddress(addr.address(), 0, scope);
        return (java.lang.foreign.MemorySegment _run_opaque) -> {
            try {
                constants$19.JxlImageOutDestroyCallback_DOWN$MH.invokeExact(symbol, _run_opaque);
            } catch (Throwable ex$) {
                throw new AssertionError("should not reach here", ex$);
            }
        };
    }
}

