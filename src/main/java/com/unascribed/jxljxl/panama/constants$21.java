// Generated by jextract

package com.unascribed.jxljxl.panama;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.lang.foreign.*;
import static java.lang.foreign.ValueLayout.*;
final class constants$21 {

    // Suppresses default constructor, ensuring non-instantiability.
    private constants$21() {}
    static final FunctionDescriptor JxlDecoderReleaseBoxBuffer$FUNC = FunctionDescriptor.of(Constants$root.C_LONG_LONG$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle JxlDecoderReleaseBoxBuffer$MH = RuntimeHelper.downcallHandle(
        "JxlDecoderReleaseBoxBuffer",
        constants$21.JxlDecoderReleaseBoxBuffer$FUNC
    );
    static final FunctionDescriptor JxlDecoderSetDecompressBoxes$FUNC = FunctionDescriptor.of(Constants$root.C_INT$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_INT$LAYOUT
    );
    static final MethodHandle JxlDecoderSetDecompressBoxes$MH = RuntimeHelper.downcallHandle(
        "JxlDecoderSetDecompressBoxes",
        constants$21.JxlDecoderSetDecompressBoxes$FUNC
    );
    static final FunctionDescriptor JxlDecoderGetBoxType$FUNC = FunctionDescriptor.of(Constants$root.C_INT$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_INT$LAYOUT
    );
    static final MethodHandle JxlDecoderGetBoxType$MH = RuntimeHelper.downcallHandle(
        "JxlDecoderGetBoxType",
        constants$21.JxlDecoderGetBoxType$FUNC
    );
    static final FunctionDescriptor JxlDecoderGetBoxSizeRaw$FUNC = FunctionDescriptor.of(Constants$root.C_INT$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle JxlDecoderGetBoxSizeRaw$MH = RuntimeHelper.downcallHandle(
        "JxlDecoderGetBoxSizeRaw",
        constants$21.JxlDecoderGetBoxSizeRaw$FUNC
    );
    static final FunctionDescriptor JxlDecoderSetProgressiveDetail$FUNC = FunctionDescriptor.of(Constants$root.C_INT$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_INT$LAYOUT
    );
    static final MethodHandle JxlDecoderSetProgressiveDetail$MH = RuntimeHelper.downcallHandle(
        "JxlDecoderSetProgressiveDetail",
        constants$21.JxlDecoderSetProgressiveDetail$FUNC
    );
    static final FunctionDescriptor JxlDecoderGetIntendedDownsamplingRatio$FUNC = FunctionDescriptor.of(Constants$root.C_LONG_LONG$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle JxlDecoderGetIntendedDownsamplingRatio$MH = RuntimeHelper.downcallHandle(
        "JxlDecoderGetIntendedDownsamplingRatio",
        constants$21.JxlDecoderGetIntendedDownsamplingRatio$FUNC
    );
}

