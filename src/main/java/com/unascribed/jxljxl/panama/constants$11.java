// Generated by jextract

package com.unascribed.jxljxl.panama;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.lang.foreign.*;
import static java.lang.foreign.ValueLayout.*;
final class constants$11 {

    // Suppresses default constructor, ensuring non-instantiability.
    private constants$11() {}
    static final FunctionDescriptor JxlEncoderSetExtraChannelDistance$FUNC = FunctionDescriptor.of(Constants$root.C_INT$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_LONG_LONG$LAYOUT,
        Constants$root.C_FLOAT$LAYOUT
    );
    static final MethodHandle JxlEncoderSetExtraChannelDistance$MH = RuntimeHelper.downcallHandle(
        "JxlEncoderSetExtraChannelDistance",
        constants$11.JxlEncoderSetExtraChannelDistance$FUNC
    );
    static final FunctionDescriptor JxlEncoderFrameSettingsCreate$FUNC = FunctionDescriptor.of(Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle JxlEncoderFrameSettingsCreate$MH = RuntimeHelper.downcallHandle(
        "JxlEncoderFrameSettingsCreate",
        constants$11.JxlEncoderFrameSettingsCreate$FUNC
    );
    static final FunctionDescriptor JxlColorEncodingSetToSRGB$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_INT$LAYOUT
    );
    static final MethodHandle JxlColorEncodingSetToSRGB$MH = RuntimeHelper.downcallHandle(
        "JxlColorEncodingSetToSRGB",
        constants$11.JxlColorEncodingSetToSRGB$FUNC
    );
    static final FunctionDescriptor JxlColorEncodingSetToLinearSRGB$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_INT$LAYOUT
    );
    static final MethodHandle JxlColorEncodingSetToLinearSRGB$MH = RuntimeHelper.downcallHandle(
        "JxlColorEncodingSetToLinearSRGB",
        constants$11.JxlColorEncodingSetToLinearSRGB$FUNC
    );
    static final FunctionDescriptor JxlEncoderAllowExpertOptions$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle JxlEncoderAllowExpertOptions$MH = RuntimeHelper.downcallHandle(
        "JxlEncoderAllowExpertOptions",
        constants$11.JxlEncoderAllowExpertOptions$FUNC
    );
    static final FunctionDescriptor JxlDebugImageCallback$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_LONG_LONG$LAYOUT,
        Constants$root.C_LONG_LONG$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final FunctionDescriptor JxlDebugImageCallback_UP$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_LONG_LONG$LAYOUT,
        Constants$root.C_LONG_LONG$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle JxlDebugImageCallback_UP$MH = RuntimeHelper.upcallHandle(JxlDebugImageCallback.class, "apply", constants$11.JxlDebugImageCallback_UP$FUNC);
}


