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
 *     int have_container;
 *     uint32_t xsize;
 *     uint32_t ysize;
 *     uint32_t bits_per_sample;
 *     uint32_t exponent_bits_per_sample;
 *     float intensity_target;
 *     float min_nits;
 *     int relative_to_max_display;
 *     float linear_below;
 *     int uses_original_profile;
 *     int have_preview;
 *     int have_animation;
 *     JxlOrientation orientation;
 *     uint32_t num_color_channels;
 *     uint32_t num_extra_channels;
 *     uint32_t alpha_bits;
 *     uint32_t alpha_exponent_bits;
 *     int alpha_premultiplied;
 *     JxlPreviewHeader preview;
 *     JxlAnimationHeader animation;
 *     uint32_t intrinsic_xsize;
 *     uint32_t intrinsic_ysize;
 *     uint8_t padding[100];
 * };
 * }
 */
public class JxlBasicInfo {

    static final StructLayout $struct$LAYOUT = MemoryLayout.structLayout(
        Constants$root.C_INT$LAYOUT.withName("have_container"),
        Constants$root.C_INT$LAYOUT.withName("xsize"),
        Constants$root.C_INT$LAYOUT.withName("ysize"),
        Constants$root.C_INT$LAYOUT.withName("bits_per_sample"),
        Constants$root.C_INT$LAYOUT.withName("exponent_bits_per_sample"),
        Constants$root.C_FLOAT$LAYOUT.withName("intensity_target"),
        Constants$root.C_FLOAT$LAYOUT.withName("min_nits"),
        Constants$root.C_INT$LAYOUT.withName("relative_to_max_display"),
        Constants$root.C_FLOAT$LAYOUT.withName("linear_below"),
        Constants$root.C_INT$LAYOUT.withName("uses_original_profile"),
        Constants$root.C_INT$LAYOUT.withName("have_preview"),
        Constants$root.C_INT$LAYOUT.withName("have_animation"),
        Constants$root.C_INT$LAYOUT.withName("orientation"),
        Constants$root.C_INT$LAYOUT.withName("num_color_channels"),
        Constants$root.C_INT$LAYOUT.withName("num_extra_channels"),
        Constants$root.C_INT$LAYOUT.withName("alpha_bits"),
        Constants$root.C_INT$LAYOUT.withName("alpha_exponent_bits"),
        Constants$root.C_INT$LAYOUT.withName("alpha_premultiplied"),
        MemoryLayout.structLayout(
            Constants$root.C_INT$LAYOUT.withName("xsize"),
            Constants$root.C_INT$LAYOUT.withName("ysize")
        ).withName("preview"),
        MemoryLayout.structLayout(
            Constants$root.C_INT$LAYOUT.withName("tps_numerator"),
            Constants$root.C_INT$LAYOUT.withName("tps_denominator"),
            Constants$root.C_INT$LAYOUT.withName("num_loops"),
            Constants$root.C_INT$LAYOUT.withName("have_timecodes")
        ).withName("animation"),
        Constants$root.C_INT$LAYOUT.withName("intrinsic_xsize"),
        Constants$root.C_INT$LAYOUT.withName("intrinsic_ysize"),
        MemoryLayout.sequenceLayout(100, Constants$root.C_CHAR$LAYOUT).withName("padding")
    );
    public static MemoryLayout $LAYOUT() {
        return JxlBasicInfo.$struct$LAYOUT;
    }
    static final VarHandle have_container$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("have_container"));
    public static VarHandle have_container$VH() {
        return JxlBasicInfo.have_container$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * int have_container;
     * }
     */
    public static int have_container$get(MemorySegment seg) {
        return (int)JxlBasicInfo.have_container$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * int have_container;
     * }
     */
    public static void have_container$set(MemorySegment seg, int x) {
        JxlBasicInfo.have_container$VH.set(seg, x);
    }
    public static int have_container$get(MemorySegment seg, long index) {
        return (int)JxlBasicInfo.have_container$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void have_container$set(MemorySegment seg, long index, int x) {
        JxlBasicInfo.have_container$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle xsize$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("xsize"));
    public static VarHandle xsize$VH() {
        return JxlBasicInfo.xsize$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * uint32_t xsize;
     * }
     */
    public static int xsize$get(MemorySegment seg) {
        return (int)JxlBasicInfo.xsize$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * uint32_t xsize;
     * }
     */
    public static void xsize$set(MemorySegment seg, int x) {
        JxlBasicInfo.xsize$VH.set(seg, x);
    }
    public static int xsize$get(MemorySegment seg, long index) {
        return (int)JxlBasicInfo.xsize$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void xsize$set(MemorySegment seg, long index, int x) {
        JxlBasicInfo.xsize$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle ysize$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("ysize"));
    public static VarHandle ysize$VH() {
        return JxlBasicInfo.ysize$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * uint32_t ysize;
     * }
     */
    public static int ysize$get(MemorySegment seg) {
        return (int)JxlBasicInfo.ysize$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * uint32_t ysize;
     * }
     */
    public static void ysize$set(MemorySegment seg, int x) {
        JxlBasicInfo.ysize$VH.set(seg, x);
    }
    public static int ysize$get(MemorySegment seg, long index) {
        return (int)JxlBasicInfo.ysize$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void ysize$set(MemorySegment seg, long index, int x) {
        JxlBasicInfo.ysize$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle bits_per_sample$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("bits_per_sample"));
    public static VarHandle bits_per_sample$VH() {
        return JxlBasicInfo.bits_per_sample$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * uint32_t bits_per_sample;
     * }
     */
    public static int bits_per_sample$get(MemorySegment seg) {
        return (int)JxlBasicInfo.bits_per_sample$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * uint32_t bits_per_sample;
     * }
     */
    public static void bits_per_sample$set(MemorySegment seg, int x) {
        JxlBasicInfo.bits_per_sample$VH.set(seg, x);
    }
    public static int bits_per_sample$get(MemorySegment seg, long index) {
        return (int)JxlBasicInfo.bits_per_sample$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void bits_per_sample$set(MemorySegment seg, long index, int x) {
        JxlBasicInfo.bits_per_sample$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle exponent_bits_per_sample$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("exponent_bits_per_sample"));
    public static VarHandle exponent_bits_per_sample$VH() {
        return JxlBasicInfo.exponent_bits_per_sample$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * uint32_t exponent_bits_per_sample;
     * }
     */
    public static int exponent_bits_per_sample$get(MemorySegment seg) {
        return (int)JxlBasicInfo.exponent_bits_per_sample$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * uint32_t exponent_bits_per_sample;
     * }
     */
    public static void exponent_bits_per_sample$set(MemorySegment seg, int x) {
        JxlBasicInfo.exponent_bits_per_sample$VH.set(seg, x);
    }
    public static int exponent_bits_per_sample$get(MemorySegment seg, long index) {
        return (int)JxlBasicInfo.exponent_bits_per_sample$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void exponent_bits_per_sample$set(MemorySegment seg, long index, int x) {
        JxlBasicInfo.exponent_bits_per_sample$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle intensity_target$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("intensity_target"));
    public static VarHandle intensity_target$VH() {
        return JxlBasicInfo.intensity_target$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * float intensity_target;
     * }
     */
    public static float intensity_target$get(MemorySegment seg) {
        return (float)JxlBasicInfo.intensity_target$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * float intensity_target;
     * }
     */
    public static void intensity_target$set(MemorySegment seg, float x) {
        JxlBasicInfo.intensity_target$VH.set(seg, x);
    }
    public static float intensity_target$get(MemorySegment seg, long index) {
        return (float)JxlBasicInfo.intensity_target$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void intensity_target$set(MemorySegment seg, long index, float x) {
        JxlBasicInfo.intensity_target$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle min_nits$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("min_nits"));
    public static VarHandle min_nits$VH() {
        return JxlBasicInfo.min_nits$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * float min_nits;
     * }
     */
    public static float min_nits$get(MemorySegment seg) {
        return (float)JxlBasicInfo.min_nits$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * float min_nits;
     * }
     */
    public static void min_nits$set(MemorySegment seg, float x) {
        JxlBasicInfo.min_nits$VH.set(seg, x);
    }
    public static float min_nits$get(MemorySegment seg, long index) {
        return (float)JxlBasicInfo.min_nits$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void min_nits$set(MemorySegment seg, long index, float x) {
        JxlBasicInfo.min_nits$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle relative_to_max_display$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("relative_to_max_display"));
    public static VarHandle relative_to_max_display$VH() {
        return JxlBasicInfo.relative_to_max_display$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * int relative_to_max_display;
     * }
     */
    public static int relative_to_max_display$get(MemorySegment seg) {
        return (int)JxlBasicInfo.relative_to_max_display$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * int relative_to_max_display;
     * }
     */
    public static void relative_to_max_display$set(MemorySegment seg, int x) {
        JxlBasicInfo.relative_to_max_display$VH.set(seg, x);
    }
    public static int relative_to_max_display$get(MemorySegment seg, long index) {
        return (int)JxlBasicInfo.relative_to_max_display$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void relative_to_max_display$set(MemorySegment seg, long index, int x) {
        JxlBasicInfo.relative_to_max_display$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle linear_below$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("linear_below"));
    public static VarHandle linear_below$VH() {
        return JxlBasicInfo.linear_below$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * float linear_below;
     * }
     */
    public static float linear_below$get(MemorySegment seg) {
        return (float)JxlBasicInfo.linear_below$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * float linear_below;
     * }
     */
    public static void linear_below$set(MemorySegment seg, float x) {
        JxlBasicInfo.linear_below$VH.set(seg, x);
    }
    public static float linear_below$get(MemorySegment seg, long index) {
        return (float)JxlBasicInfo.linear_below$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void linear_below$set(MemorySegment seg, long index, float x) {
        JxlBasicInfo.linear_below$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle uses_original_profile$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("uses_original_profile"));
    public static VarHandle uses_original_profile$VH() {
        return JxlBasicInfo.uses_original_profile$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * int uses_original_profile;
     * }
     */
    public static int uses_original_profile$get(MemorySegment seg) {
        return (int)JxlBasicInfo.uses_original_profile$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * int uses_original_profile;
     * }
     */
    public static void uses_original_profile$set(MemorySegment seg, int x) {
        JxlBasicInfo.uses_original_profile$VH.set(seg, x);
    }
    public static int uses_original_profile$get(MemorySegment seg, long index) {
        return (int)JxlBasicInfo.uses_original_profile$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void uses_original_profile$set(MemorySegment seg, long index, int x) {
        JxlBasicInfo.uses_original_profile$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle have_preview$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("have_preview"));
    public static VarHandle have_preview$VH() {
        return JxlBasicInfo.have_preview$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * int have_preview;
     * }
     */
    public static int have_preview$get(MemorySegment seg) {
        return (int)JxlBasicInfo.have_preview$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * int have_preview;
     * }
     */
    public static void have_preview$set(MemorySegment seg, int x) {
        JxlBasicInfo.have_preview$VH.set(seg, x);
    }
    public static int have_preview$get(MemorySegment seg, long index) {
        return (int)JxlBasicInfo.have_preview$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void have_preview$set(MemorySegment seg, long index, int x) {
        JxlBasicInfo.have_preview$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle have_animation$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("have_animation"));
    public static VarHandle have_animation$VH() {
        return JxlBasicInfo.have_animation$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * int have_animation;
     * }
     */
    public static int have_animation$get(MemorySegment seg) {
        return (int)JxlBasicInfo.have_animation$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * int have_animation;
     * }
     */
    public static void have_animation$set(MemorySegment seg, int x) {
        JxlBasicInfo.have_animation$VH.set(seg, x);
    }
    public static int have_animation$get(MemorySegment seg, long index) {
        return (int)JxlBasicInfo.have_animation$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void have_animation$set(MemorySegment seg, long index, int x) {
        JxlBasicInfo.have_animation$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle orientation$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("orientation"));
    public static VarHandle orientation$VH() {
        return JxlBasicInfo.orientation$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * JxlOrientation orientation;
     * }
     */
    public static int orientation$get(MemorySegment seg) {
        return (int)JxlBasicInfo.orientation$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * JxlOrientation orientation;
     * }
     */
    public static void orientation$set(MemorySegment seg, int x) {
        JxlBasicInfo.orientation$VH.set(seg, x);
    }
    public static int orientation$get(MemorySegment seg, long index) {
        return (int)JxlBasicInfo.orientation$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void orientation$set(MemorySegment seg, long index, int x) {
        JxlBasicInfo.orientation$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle num_color_channels$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("num_color_channels"));
    public static VarHandle num_color_channels$VH() {
        return JxlBasicInfo.num_color_channels$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * uint32_t num_color_channels;
     * }
     */
    public static int num_color_channels$get(MemorySegment seg) {
        return (int)JxlBasicInfo.num_color_channels$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * uint32_t num_color_channels;
     * }
     */
    public static void num_color_channels$set(MemorySegment seg, int x) {
        JxlBasicInfo.num_color_channels$VH.set(seg, x);
    }
    public static int num_color_channels$get(MemorySegment seg, long index) {
        return (int)JxlBasicInfo.num_color_channels$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void num_color_channels$set(MemorySegment seg, long index, int x) {
        JxlBasicInfo.num_color_channels$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle num_extra_channels$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("num_extra_channels"));
    public static VarHandle num_extra_channels$VH() {
        return JxlBasicInfo.num_extra_channels$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * uint32_t num_extra_channels;
     * }
     */
    public static int num_extra_channels$get(MemorySegment seg) {
        return (int)JxlBasicInfo.num_extra_channels$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * uint32_t num_extra_channels;
     * }
     */
    public static void num_extra_channels$set(MemorySegment seg, int x) {
        JxlBasicInfo.num_extra_channels$VH.set(seg, x);
    }
    public static int num_extra_channels$get(MemorySegment seg, long index) {
        return (int)JxlBasicInfo.num_extra_channels$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void num_extra_channels$set(MemorySegment seg, long index, int x) {
        JxlBasicInfo.num_extra_channels$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle alpha_bits$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("alpha_bits"));
    public static VarHandle alpha_bits$VH() {
        return JxlBasicInfo.alpha_bits$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * uint32_t alpha_bits;
     * }
     */
    public static int alpha_bits$get(MemorySegment seg) {
        return (int)JxlBasicInfo.alpha_bits$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * uint32_t alpha_bits;
     * }
     */
    public static void alpha_bits$set(MemorySegment seg, int x) {
        JxlBasicInfo.alpha_bits$VH.set(seg, x);
    }
    public static int alpha_bits$get(MemorySegment seg, long index) {
        return (int)JxlBasicInfo.alpha_bits$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void alpha_bits$set(MemorySegment seg, long index, int x) {
        JxlBasicInfo.alpha_bits$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle alpha_exponent_bits$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("alpha_exponent_bits"));
    public static VarHandle alpha_exponent_bits$VH() {
        return JxlBasicInfo.alpha_exponent_bits$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * uint32_t alpha_exponent_bits;
     * }
     */
    public static int alpha_exponent_bits$get(MemorySegment seg) {
        return (int)JxlBasicInfo.alpha_exponent_bits$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * uint32_t alpha_exponent_bits;
     * }
     */
    public static void alpha_exponent_bits$set(MemorySegment seg, int x) {
        JxlBasicInfo.alpha_exponent_bits$VH.set(seg, x);
    }
    public static int alpha_exponent_bits$get(MemorySegment seg, long index) {
        return (int)JxlBasicInfo.alpha_exponent_bits$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void alpha_exponent_bits$set(MemorySegment seg, long index, int x) {
        JxlBasicInfo.alpha_exponent_bits$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle alpha_premultiplied$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("alpha_premultiplied"));
    public static VarHandle alpha_premultiplied$VH() {
        return JxlBasicInfo.alpha_premultiplied$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * int alpha_premultiplied;
     * }
     */
    public static int alpha_premultiplied$get(MemorySegment seg) {
        return (int)JxlBasicInfo.alpha_premultiplied$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * int alpha_premultiplied;
     * }
     */
    public static void alpha_premultiplied$set(MemorySegment seg, int x) {
        JxlBasicInfo.alpha_premultiplied$VH.set(seg, x);
    }
    public static int alpha_premultiplied$get(MemorySegment seg, long index) {
        return (int)JxlBasicInfo.alpha_premultiplied$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void alpha_premultiplied$set(MemorySegment seg, long index, int x) {
        JxlBasicInfo.alpha_premultiplied$VH.set(seg.asSlice(index*sizeof()), x);
    }
    public static MemorySegment preview$slice(MemorySegment seg) {
        return seg.asSlice(72, 8);
    }
    public static MemorySegment animation$slice(MemorySegment seg) {
        return seg.asSlice(80, 16);
    }
    static final VarHandle intrinsic_xsize$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("intrinsic_xsize"));
    public static VarHandle intrinsic_xsize$VH() {
        return JxlBasicInfo.intrinsic_xsize$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * uint32_t intrinsic_xsize;
     * }
     */
    public static int intrinsic_xsize$get(MemorySegment seg) {
        return (int)JxlBasicInfo.intrinsic_xsize$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * uint32_t intrinsic_xsize;
     * }
     */
    public static void intrinsic_xsize$set(MemorySegment seg, int x) {
        JxlBasicInfo.intrinsic_xsize$VH.set(seg, x);
    }
    public static int intrinsic_xsize$get(MemorySegment seg, long index) {
        return (int)JxlBasicInfo.intrinsic_xsize$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void intrinsic_xsize$set(MemorySegment seg, long index, int x) {
        JxlBasicInfo.intrinsic_xsize$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle intrinsic_ysize$VH = $struct$LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("intrinsic_ysize"));
    public static VarHandle intrinsic_ysize$VH() {
        return JxlBasicInfo.intrinsic_ysize$VH;
    }
    /**
     * Getter for field:
     * {@snippet :
     * uint32_t intrinsic_ysize;
     * }
     */
    public static int intrinsic_ysize$get(MemorySegment seg) {
        return (int)JxlBasicInfo.intrinsic_ysize$VH.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * uint32_t intrinsic_ysize;
     * }
     */
    public static void intrinsic_ysize$set(MemorySegment seg, int x) {
        JxlBasicInfo.intrinsic_ysize$VH.set(seg, x);
    }
    public static int intrinsic_ysize$get(MemorySegment seg, long index) {
        return (int)JxlBasicInfo.intrinsic_ysize$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void intrinsic_ysize$set(MemorySegment seg, long index, int x) {
        JxlBasicInfo.intrinsic_ysize$VH.set(seg.asSlice(index*sizeof()), x);
    }
    public static MemorySegment padding$slice(MemorySegment seg) {
        return seg.asSlice(104, 100);
    }
    public static long sizeof() { return $LAYOUT().byteSize(); }
    public static MemorySegment allocate(SegmentAllocator allocator) { return allocator.allocate($LAYOUT()); }
    public static MemorySegment allocateArray(long len, SegmentAllocator allocator) {
        return allocator.allocate(MemoryLayout.sequenceLayout(len, $LAYOUT()));
    }
    public static MemorySegment ofAddress(MemorySegment addr, SegmentScope scope) { return RuntimeHelper.asArray(addr, $LAYOUT(), 1, scope); }
}

