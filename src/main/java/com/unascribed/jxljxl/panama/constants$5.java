// Generated by jextract

package com.unascribed.jxljxl.panama;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.lang.foreign.*;
import static java.lang.foreign.ValueLayout.*;
final class constants$5 {

    // Suppresses default constructor, ensuring non-instantiability.
    private constants$5() {}
    static final FunctionDescriptor JxlEncoderGetError$FUNC = FunctionDescriptor.of(Constants$root.C_INT$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle JxlEncoderGetError$MH = RuntimeHelper.downcallHandle(
        "JxlEncoderGetError",
        constants$5.JxlEncoderGetError$FUNC
    );
    static final FunctionDescriptor JxlEncoderProcessOutput$FUNC = FunctionDescriptor.of(Constants$root.C_INT$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle JxlEncoderProcessOutput$MH = RuntimeHelper.downcallHandle(
        "JxlEncoderProcessOutput",
        constants$5.JxlEncoderProcessOutput$FUNC
    );
    static final FunctionDescriptor JxlEncoderSetFrameHeader$FUNC = FunctionDescriptor.of(Constants$root.C_INT$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle JxlEncoderSetFrameHeader$MH = RuntimeHelper.downcallHandle(
        "JxlEncoderSetFrameHeader",
        constants$5.JxlEncoderSetFrameHeader$FUNC
    );
    static final FunctionDescriptor JxlEncoderSetExtraChannelBlendInfo$FUNC = FunctionDescriptor.of(Constants$root.C_INT$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_LONG_LONG$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle JxlEncoderSetExtraChannelBlendInfo$MH = RuntimeHelper.downcallHandle(
        "JxlEncoderSetExtraChannelBlendInfo",
        constants$5.JxlEncoderSetExtraChannelBlendInfo$FUNC
    );
    static final FunctionDescriptor JxlEncoderSetFrameName$FUNC = FunctionDescriptor.of(Constants$root.C_INT$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle JxlEncoderSetFrameName$MH = RuntimeHelper.downcallHandle(
        "JxlEncoderSetFrameName",
        constants$5.JxlEncoderSetFrameName$FUNC
    );
    static final FunctionDescriptor JxlEncoderSetFrameBitDepth$FUNC = FunctionDescriptor.of(Constants$root.C_INT$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle JxlEncoderSetFrameBitDepth$MH = RuntimeHelper.downcallHandle(
        "JxlEncoderSetFrameBitDepth",
        constants$5.JxlEncoderSetFrameBitDepth$FUNC
    );
}


