/*
 * MIT License
 *
 * Copyright (c) 2023 Una Thompson (unascribed)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.unascribed.jxljxl;

import static com.unascribed.jxljxl.panama.LibJxl.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SegmentScope;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.ValueLayout.OfByte;
import java.lang.foreign.ValueLayout.OfDouble;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import com.unascribed.jxljxl.JXLBoxBuilder.JXLWellKnownBoxType;
import com.unascribed.jxljxl.JXLCustomColorEncoding.JXLGammaTransferFunction;
import com.unascribed.jxljxl.JXLCustomColorEncoding.JXLPredefinedTransferFunction;
import com.unascribed.jxljxl.JXLCustomColorEncoding.JXLPrimaries;
import com.unascribed.jxljxl.JXLCustomColorEncoding.JXLTransferFunction;
import com.unascribed.jxljxl.JXLCustomColorEncoding.JXLWhitePoint;
import com.unascribed.jxljxl.JXLFrameBuilder.JXLBuffering;
import com.unascribed.jxljxl.JXLFrameBuilder.JXLFrameData;
import com.unascribed.jxljxl.panama.FunctionAddresses;
import com.unascribed.jxljxl.panama.JxlAnimationHeader;
import com.unascribed.jxljxl.panama.JxlBasicInfo;
import com.unascribed.jxljxl.panama.JxlBitDepth;
import com.unascribed.jxljxl.panama.JxlBlendInfo;
import com.unascribed.jxljxl.panama.JxlColorEncoding;
import com.unascribed.jxljxl.panama.JxlExtraChannelInfo;
import com.unascribed.jxljxl.panama.JxlFrameHeader;
import com.unascribed.jxljxl.panama.JxlLayerInfo;
import com.unascribed.jxljxl.panama.JxlPixelFormat;
import com.unascribed.jxljxl.panama.JxlPreviewHeader;
import com.unascribed.jxljxl.panama.LibJxl;

public class JXLEncoder {
	
	// JxlEncoder {
		private Boolean useContainer = null;
		private int codestreamLevel = -1;
		private Boolean storeJpegMetadata = null;
		private boolean allowExpertOptions = false;
		private JXLColorEncoding colorEncoding = null;
	// }

	// JxlBasicInfo {
		int xsize = -1;
		int ysize = -1;
		private int bitsPerSample = -1;
		private int exponentBitsPerSample = 0;
		private Float intensityTarget = null;
		private Float minNits = null;
		private Boolean relativeToMaxDisplay = null;
		private Float linearBelow = null;
		private Boolean usesOriginalProfile = null;
		private Boolean havePreview = null;
		private Boolean haveAnimation = null;
		private JXLOrientation orientation = JXLOrientation.IDENTITY;
		private int numColorChannels = -1;
		int numExtraChannels = -1;
		private int alphaBits = -1;
		private int alphaExponentBits = 0;
		boolean alphaPremultiplied = false;
		// JxlPreviewHeader {
			private int previewXsize = -1;
			private int previewYsize = -1;
		// }
		// JxlAnimationHeader {
			private int tpsNumerator = -1;
			private int tpsDenominator = -1;
			private int numLoops = 0;
			private Boolean haveTimecodes = null;
		// }
		private int intrinsicXsize = -1;
		private int intrinsicYsize = -1;
	// }
	
	final List<JXLBoxBuilder> boxes = new ArrayList<>();
	final List<JXLFrameBuilder> frames = new ArrayList<>();
	
	private JXLEncoder() {}
	
	public static JXLEncoder create() {
		return new JXLEncoder();
	}
	
	/**
	 * Forces the encoder to use the box-based container format (BMFF) even when not necessary.
	 * <p>
	 * When using {@link #addBox}, JxlEncoderStoreJPEGMetadata or {@link #codestreamLevel(int)}
	 * with level 10, the encoder will automatically also use the container format, it is not
	 * necessary to use JxlEncoderUseContainer for those use cases.
	 * <p>
	 * By default this setting is disabled.
	 */
	public JXLEncoder withContainer() {
		this.useContainer = true;
		return this;
	}
	
	/**
	 * Sets the feature level of the JPEG XL codestream. Valid values are 5 and 10, or -1 (to choose
	 * automatically). Using the minimum required level, or level 5 in most cases, is recommended
	 * for compatibility with all decoders.
	 * <p>
	 * Level 5: for end-user image delivery, this level is the most widely supported level by image
	 * decoders and the recommended level to use unless a level 10 feature is absolutely necessary.
	 * Supports a maximum resolution 268435456 pixels total with a maximum width or height of 262144
	 * pixels, maximum 16-bit color channel depth, maximum 120 frames per second for animation,
	 * maximum ICC color profile size of 4 MiB, it allows all color models and extra channel types
	 * except CMYK and the JXL_CHANNEL_BLACK extra channel, and a maximum of 4 extra channels in
	 * addition to the 3 color channels. It also sets boundaries to certain internally used coding
	 * tools.
	 * <p>
	 * Level 10: this level removes or increases the bounds of most of the level 5 limitations,
	 * allows CMYK color and up to 32 bits per color channel, but may be less widely supported.
	 * <p>
	 * The default value is -1. This means the encoder will automatically choose between level 5 and
	 * level 10 based on what information is inside the JxlBasicInfo structure. Do note that some
	 * level 10 features, particularly those used by animated JPEG XL codestreams, might require
	 * level 10, even though the JxlBasicInfo only suggests level 5. In this case, the level must be
	 * explicitly set to 10, otherwise the encoder will return an error. The encoder will restrict
	 * internal encoding choices to those compatible with the level setting.
	 */
	public JXLEncoder codestreamLevel(int level) {
		this.codestreamLevel = level;
		return this;
	}
	
	/**
	 * Configure the encoder to store JPEG reconstruction metadata in the JPEG XL container.
	 * <p>
	 * If this is set to true and a single JPEG frame is added, it will be possible to losslessly
	 * reconstruct the JPEG codestream.
	 */
	public JXLEncoder storeJpegMetadata(boolean store) {
		this.storeJpegMetadata = store;
		return this;
	}
	
	/**
	 * Enables usage of expert options.
	 * <p>
	 * At the moment, the only expert option is setting an effort value of 10, which gives the best
	 * compression for pixel-lossless modes but is very slow.
	 * @see JXLFrameBuilder#effort(int)
	 */
	public JXLEncoder allowExpertOptions() {
		this.allowExpertOptions = true;
		return this;
	}
	
	/**
	 * Sets the original color encoding of the image encoded by this encoder.
	 * @see JXLPredefinedColorEncoding
	 * @see JXLCustomColorEncoding
	 * @see JXLICCColorEncoding
	 */
	public JXLEncoder colorEncoding(JXLColorEncoding encoding) {
		this.colorEncoding = encoding;
		return this;
	}
	
	/**
	 * Size of the image in pixels, before applying orientation.
	 */
	public JXLEncoder size(int xsize, int ysize) {
		this.xsize = xsize;
		this.ysize = ysize;
		return this;
	}
	
	/**
	 * Specifies an explicit intensity range for the image.
	 * 
	 * @param minNits
	 *            Lower bound on the intensity level present in the image. This may be loose, i.e.
	 *            lower than the actual darkest pixel. When tone mapping, a decoder will map
	 *            [min_nits, intensity_target] to the display range.
	 * @param intensityTarget
	 *            Upper bound on the intensity level present in the image in nits. For unsigned
	 *            integer pixel encodings, this is the brightness of the largest representable
	 *            value. The image does not necessarily contain a pixel actually this bright. An
	 *            encoder is allowed to set 255 for SDR images without computing a histogram.
	 *            Leaving this set to its default of 0 lets libjxl choose a sensible default value
	 *            based on the color encoding.
	 */
	public JXLEncoder intensityRange(float minNits, float intensityTarget) {
		this.minNits = minNits;
		this.intensityTarget = intensityTarget;
		return this;
	}
	
	/**
	 * The tone mapping will leave unchanged (linear mapping) any pixels whose brightness is
	 * strictly below this, in terms of an absolute brightness [nits].
	 */
	public JXLEncoder linearBelowAbsolute(float nits) {
		this.relativeToMaxDisplay = false;
		this.linearBelow = nits;
		return this;
	}
	
	/**
	 * The tone mapping will leave unchanged (linear mapping) any pixels whose brightness is
	 * strictly below this, in terms of a ratio [0, 1] of the maximum display brightness [nits].
	 */
	public JXLEncoder linearBelowRelative(float displayRatio) {
		this.relativeToMaxDisplay = true;
		this.linearBelow = displayRatio;
		return this;
	}
	
	public JXLEncoder bitsPerSample(int bitsPerSample) {
		this.bitsPerSample = bitsPerSample;
		return this;
	}
	
	public JXLEncoder withFloatingSamples(int exponentBits) {
		if (exponentBits == 0) throw new IllegalArgumentException("exponentBits must not be 0");
		this.exponentBitsPerSample = exponentBits;
		return this;
	}
	
	/**
	 * Whether the data in the codestream is encoded in the original color profile that is attached
	 * to the codestream metadata header, or is encoded in an internally supported absolute color
	 * space (which the decoder can always convert to linear or non-linear sRGB or to XYB). If the
	 * original profile is used, the decoder outputs pixel data in the color space matching that
	 * profile, but doesn’t convert it to any other color space. If the original profile is not
	 * used, the decoder only outputs the data as sRGB (linear if outputting to floating point,
	 * nonlinear with standard sRGB transfer function if outputting to unsigned integers) but will
	 * not convert it to to the original color profile. The decoder also does not convert to the
	 * target display color profile. Note that for lossy compression, this should be set to false
	 * for most use cases, and if needed, the image should be converted to the original color
	 * profile after decoding.
	 */
	public JXLEncoder usesOriginalProfile(boolean b) {
		this.usesOriginalProfile = b;
		return this;
	}
	
	/**
	 * Forces adding a smaller preview/thumbnail image to the beginning of the codestream of the
	 * given size.
	 */
	public JXLEncoder withPreview(int xsize, int ysize) {
		this.havePreview = true;
		this.previewXsize = xsize;
		this.previewYsize = ysize;
		return this;
	}
	
	/**
	 * Explicitly requests no preview be added.
	 */
	public JXLEncoder withoutPreview() {
		this.havePreview = false;
		return this;
	}
	
	public class JXLAnimationConfigurator {
		/**
		 * The animation should play once then freeze on its final frame.
		 */
		public JXLAnimationConfigurator playOnce() {
			numLoops = 1;
			return this;
		}
		
		/**
		 * The animation should repeat forever.
		 */
		public JXLAnimationConfigurator loopInfinitely() {
			numLoops = 0;
			return this;
		}
		
		/**
		 * The animation should play exactly {@code n} times, then freeze on its final frame.
		 */
		public JXLAnimationConfigurator loop(int n) {
			if (n <= 0) throw new IllegalArgumentException("Cannot loop "+n+" times");
			numLoops = n;
			return this;
		}
		
		/**
		 * Enable usage of timecodes for frames.
		 */
		public JXLAnimationConfigurator withTimecodes() {
			haveTimecodes = true;
			return this;
		}
		
		/**
		 * Disable usage of timecodes for frames.
		 */
		public JXLAnimationConfigurator withoutTimecodes() {
			haveTimecodes = false;
			return this;
		}

		/**
		 * Finish configuring animation data and return the parent.
		 */
		public JXLEncoder done() {
			return JXLEncoder.this;
		}
	}
	
	/**
	 * Enable animation with the given tickrate, described in terms of a rational value, i.e.
	 * {@code tpsNumerator/tpsDenominator}.
	 */
	public JXLAnimationConfigurator withAnimation(int tpsNumerator, int tpsDenominator) {
		this.haveAnimation = true;
		this.tpsNumerator = tpsNumerator;
		this.tpsDenominator = tpsDenominator;
		return new JXLAnimationConfigurator();
	}
	
	/**
	 * Enable animation with the given tickrate, described by the given amount of time between
	 * ticks.
	 */
	// TODO is this method actually useful in any way? lol
	public JXLAnimationConfigurator withAnimation(int unitsPerTick, TimeUnit unit) {
		this.haveAnimation = true;
		long n = unitsPerTick;
		long d = unit.convert(1, TimeUnit.SECONDS);
		long gcd = gcd(n, d);
		this.tpsNumerator = (int)(n/gcd);
		this.tpsDenominator = (int)(d/gcd);
		return new JXLAnimationConfigurator();
	}
	
	private static long gcd(long n, long d) {
		while (d != 0) {
			long tmp = d;
			d = n % d;
			n = tmp;
		}
		return n;
	}
	
	/**
	 * Explicitly disable animation.
	 */
	public JXLEncoder withoutAnimation() {
		this.haveAnimation = false;
		return this;
	}
	
	/**
	 * Specify a rotation for this image to be applied after decoding.
	 */
	public JXLEncoder orientiation(JXLOrientation orientation) {
		this.orientation = orientation;
		return this;
	}
	
	/**
	 * Specify the number of channels in this image.
	 * 
	 * @param color
	 *            Number of color channels encoded in the image, this is either 1 for grayscale
	 *            data, or 3 for colored data. This count does not include the alpha channel or
	 *            other extra channels. If and only if this is 1, the JXLColorSpace in the
	 *            JXLColorEncoding must be GRAY.
	 * @param extra
	 *            Number of additional image channels. This includes the main alpha channel, but can
	 *            also include additional channels such as depth, additional alpha channels, spot
	 *            colors, and so on.
	 */
	public JXLEncoder numChannels(int color, int extra) {
		this.numColorChannels = color;
		this.numExtraChannels = extra;
		return this;
	}
	
	/**
	 * Bit depth of the encoded alpha channel, or 0 if there is no alpha channel.
	 */
	public JXLEncoder bitsPerAlphaSample(int bitsPerSample) {
		this.alphaBits = bitsPerSample;
		return this;
	}
	
	/**
	 * Alpha channel floating point exponent bits, or 0 if they are unsigned.
	 */
	public JXLEncoder withFloatingAlphaSamples(int exponentBits) {
		if (exponentBits == 0) throw new IllegalArgumentException("exponentBits must not be 0");
		this.alphaExponentBits = exponentBits;
		return this;
	}
	
	/**
	 * Enables premultiplied alpha. Only used if there is a main alpha channel.
	 */
	public JXLEncoder withPremultipliedAlpha() {
		this.alphaPremultiplied = true;
		return this;
	}
	
	/**
	 * Disables premultiplied alpha. Only used if there is a main alpha channel.
	 */
	public JXLEncoder withStraightAlpha() {
		this.alphaPremultiplied = false;
		return this;
	}
	
	/**
	 * Configure an intrinsic size for the image. The intrinsic size can be different from the
	 * actual size in pixels (as given by xsize and ysize) and it denotes the recommended dimensions
	 * for displaying the image, i.e. applications are advised to resample the decoded image to the
	 * intrinsic dimensions.
	 */
	public JXLEncoder intrinsicSize(int xsize, int ysize) {
		this.intrinsicXsize = xsize;
		this.intrinsicYsize = ysize;
		return this;
	}

	
	/**
	 * Adds a metadata box to the file format.
	 * <p>
	 * Boxes allow inserting application-specific data and metadata (Exif, XML/XMP, JUMBF and user
	 * defined boxes).
	 * <p>
	 * The box format follows ISO BMFF and shares features and box types with other image and video
	 * formats, including the Exif, XML and JUMBF boxes. The box format for JPEG XL is specified in
	 * ISO/IEC 18181-2.
	 * <p>
	 * Boxes in general don’t contain other boxes inside, except a JUMBF superbox. Boxes follow each
	 * other sequentially and are byte-aligned. If the container format is used, the JXL stream
	 * consists of concatenated boxes. It is also possible to use a direct codestream without boxes,
	 * but in that case metadata cannot be added.
	 * <p>
	 * Each box generally has the following byte structure in the file:
	 * <ul>
	 * <li>4 bytes: box size including box header (Big endian. If set to 0, an 8-byte 64-bit size
	 * follows instead).</li>
	 * <li>4 bytes: type, e.g. "JXL " for the signature box, "jxlc" for a codestream box.</li>
	 * <li>N bytes: box contents.</li>
	 * </ul>
	 * Only the box contents are provided to the returned builder, the encoder encodes the size
	 * header itself. Most boxes are written automatically by the encoder as needed ("JXL ", "ftyp",
	 * "jxll", "jxlc", "jxlp", "jxli", "jbrd"), and this function only needs to be called to add
	 * optional metadata when encoding from pixels. When recompressing JPEG files, if the input JPEG
	 * contains EXIF, XMP or JUMBF metadata, the corresponding boxes are already added
	 * automatically.
	 * <p>
	 * Box types are given by 4 characters. The following boxes can be added with this function:
	 * <ul>
	 * <li>"Exif": a box with EXIF metadata, can be added by libjxl users, or is automatically added
	 * when needed for JPEG reconstruction. The contents of this box must be prepended by a 4-byte
	 * tiff header offset, which may be 4 zero bytes in case the tiff header follows immediately.
	 * The EXIF metadata must be in sync with what is encoded in the JPEG XL codestream,
	 * specifically the image orientation. While this is not recommended in practice, in case of
	 * conflicting metadata, the JPEG XL codestream takes precedence.</li>
	 * <li>"xml ": a box with XML data, in particular XMP metadata, can be added by libjxl users, or
	 * is automatically added when needed for JPEG reconstruction</li>
	 * <li>"jumb": a JUMBF superbox, which can contain boxes with different types of metadata
	 * inside. This box type can be added by the encoder transparently, and other libraries to
	 * create and handle JUMBF content exist.</li>
	 * <li>Application-specific boxes. Their typename should not begin with "jxl" or "JXL" or
	 * conflict with other existing typenames, and they should be registered with
	 * <a href="mp4ra.org">MP4RA</a>.</li>
	 * </ul>
	 * These boxes can be stored uncompressed or Brotli-compressed (using a "brob" box), depending
	 * on the {@link JXLBoxBuilder#compressed() compressed} parameter.
	 */
	public JXLBoxBuilder addBox(String typeFourcc) {
		return new JXLBoxBuilder(this).type(typeFourcc);
	}
	
	/**
	 * @see #addBox(String)
	 */
	public JXLBoxBuilder addBox(JXLWellKnownBoxType type) {
		return new JXLBoxBuilder(this).type(type);
	}
	

	/**
	 * Start defining a new image frame with no name and no duration.
	 */
	public JXLFrameBuilder newFrame() {
		return new JXLFrameBuilder(this);
	}
	/**
	 * Start defining a new image frame with the given name and no duration.
	 */
	public JXLFrameBuilder newFrame(String name) {
		return new JXLFrameBuilder(this).name(name);
	}
	/**
	 * Start defining a new image frame with no name and the given duration.
	 */
	public JXLFrameBuilder newFrame(int duration) {
		return new JXLFrameBuilder(this).duration(duration);
	}
	/**
	 * Start defining a new image frame with the given name and given duration.
	 */
	public JXLFrameBuilder newFrame(String name, int duration) {
		return new JXLFrameBuilder(this).name(name).duration(duration);
	}

	private static final MemorySegment jxl_runner = JxlThreadParallelRunnerCreate(MemorySegment.NULL, Runtime.getRuntime().availableProcessors());
	
	private long nullableBoolint(Boolean b) {
		return b == null ? -1 : b ? 1 : 0;
	}
	
	private static final String NO_MORE_INFO = " (no further detail is available, try compiling libjxl with -DJXL_DEBUG_ON_ERROR)";

	private void check(int res) throws JXLException {
		switch (res) {
			case 0x00 -> {}
			case 0x01 -> throw new JXLException("libjxl returned a generic error"+NO_MORE_INFO);
			case 0x02 -> throw new OutOfMemoryError("libjxl ran out of native memory");
			case 0x03 -> throw new JXLException("JPEG reconstruction data cannot be represented (e.g. too much tail data)");
			case 0x04 -> throw new JXLException("Input is invalid (e.g. corrupt JPEG file or ICC profile)");
			
			case 0x80 -> throw new JXLException("Selected options not supported by this version of libjxl");
			case 0x81 -> throw new IllegalArgumentException("Inappropriate API usage"+NO_MORE_INFO);
			
			default -> throw new JXLException("libjxl returned unknown error 0x"+Integer.toHexString(res)+NO_MORE_INFO);
		}
	}

	private <T, U> void applyUnlessNull(BiConsumer<T, ? super U> func, T subject, U value) {
		if (value != null) func.accept(subject, value);
	}
	
	private interface ObjectIntBiConsumer<T> {
		void accept(T t, int b);
	}
	
	private <T, U> void applyBoolintUnlessNull(ObjectIntBiConsumer<T> func, T subject, Boolean value) {
		if (value != null) func.accept(subject, value ? 1 : 0);
	}
	
	private <T, U> void applyUnlessM1(ObjectIntBiConsumer<T> func, T subject, int value) {
		if (value != -1) func.accept(subject, value);
	}

	/**
	 * Encode a JXL file to the given OutputStream.
	 * @throws IOException if an IO error occurs
	 * @throws JXLException if an error is encountered while encoding
	 * @throws IllegalArgumentException if this JXLEncoder has been misconfigured
	 */
	@SuppressWarnings("deprecation")
	public void encodeToStream(OutputStream out) throws IOException, JXLException {
		if (colorEncoding == null) throw new IllegalArgumentException("colorEncoding must be set");
		if (xsize == -1) throw new IllegalArgumentException("xsize must be set");
		if (ysize == -1) throw new IllegalArgumentException("ysize must be set");
		if (bitsPerSample == -1) throw new IllegalArgumentException("bitsPerSample must be set");
		if (numColorChannels == -1) throw new IllegalArgumentException("numColorChannels must be set");
		if (numExtraChannels == -1) throw new IllegalArgumentException("numExtraChannels must be set");
		if (havePreview == Boolean.TRUE) {
			if (previewXsize == -1) throw new IllegalArgumentException("When havePreview is true, previewXsize must be set");
			if (previewYsize == -1) throw new IllegalArgumentException("When havePreview is true, previewYsize must be set");
		}
		if (haveAnimation == Boolean.TRUE) {
			if (tpsNumerator == -1) throw new IllegalArgumentException("When haveAnimation is true, tpsNumerator must be set");
			if (tpsDenominator == -1) throw new IllegalArgumentException("When haveAnimation is true, tpsDenominator must be set");
			if (numLoops == -1) throw new IllegalArgumentException("When haveAnimation is true, numLoops must be set");
		}
		var alloc = SegmentAllocator.nativeAllocator(SegmentScope.auto());
		var enc = JxlEncoderCreate(MemorySegment.NULL);
		try {
			applyBoolintUnlessNull(LibJxl::JxlEncoderUseContainer, enc, useContainer);
			JxlEncoderSetCodestreamLevel(enc, codestreamLevel);
			applyBoolintUnlessNull(LibJxl::JxlEncoderStoreJPEGMetadata, enc, storeJpegMetadata);
			if (allowExpertOptions) JxlEncoderAllowExpertOptions(enc);
			
			if (!boxes.isEmpty()) {
				JxlEncoderUseBoxes(enc);
				for (var box : boxes) {
					JxlEncoderAddBox(enc, MemorySegment.ofArray(box.type), MemorySegment.ofArray(box.contents), box.contents.length, box.compressed ? 1 : 0);
				}
			}
			
			var info = JxlBasicInfo.allocate(alloc);
			JxlEncoderInitBasicInfo(info);
			JxlBasicInfo.xsize$set(info, xsize);
			JxlBasicInfo.ysize$set(info, ysize);
			JxlBasicInfo.bits_per_sample$set(info, bitsPerSample);
			JxlBasicInfo.exponent_bits_per_sample$set(info, exponentBitsPerSample);
			applyUnlessNull(JxlBasicInfo::intensity_target$set, info, intensityTarget);
			applyUnlessNull(JxlBasicInfo::min_nits$set, info, minNits);
			applyBoolintUnlessNull(JxlBasicInfo::relative_to_max_display$set, info, relativeToMaxDisplay);
			applyUnlessNull(JxlBasicInfo::linear_below$set, info, linearBelow);
			applyBoolintUnlessNull(JxlBasicInfo::uses_original_profile$set, info, usesOriginalProfile);
			applyBoolintUnlessNull(JxlBasicInfo::have_preview$set, info, havePreview);
			if (havePreview == Boolean.TRUE) {
				var hdr = JxlBasicInfo.preview$slice(info);
				JxlPreviewHeader.xsize$set(hdr, previewXsize);
				JxlPreviewHeader.ysize$set(hdr, previewYsize);
			}
			applyBoolintUnlessNull(JxlBasicInfo::have_animation$set, info, havePreview);
			if (haveAnimation == Boolean.TRUE) {
				var hdr = JxlBasicInfo.animation$slice(info);
				JxlAnimationHeader.tps_numerator$set(hdr, tpsNumerator);
				JxlAnimationHeader.tps_denominator$set(hdr, tpsDenominator);
				JxlAnimationHeader.num_loops$set(hdr, numLoops);
				applyBoolintUnlessNull(JxlAnimationHeader::have_timecodes$set, hdr, haveTimecodes);
			}
			JxlBasicInfo.orientation$set(info, switch (orientation) {
				case ANTI_TRANSPOSE -> JXL_ORIENT_ANTI_TRANSPOSE();
				case FLIP_HORIZONTAL -> JXL_ORIENT_FLIP_HORIZONTAL();
				case FLIP_VERTICAL -> JXL_ORIENT_FLIP_VERTICAL();
				case IDENTITY -> JXL_ORIENT_IDENTITY();
				case ROTATE_180 -> JXL_ORIENT_ROTATE_180();
				case ROTATE_90_CCW -> JXL_ORIENT_ROTATE_90_CCW();
				case ROTATE_90_CW -> JXL_ORIENT_ROTATE_90_CW();
				case TRANSPOSE -> JXL_ORIENT_TRANSPOSE();
			});
			JxlBasicInfo.num_color_channels$set(info, numColorChannels);
			JxlBasicInfo.num_extra_channels$set(info, numExtraChannels);
			applyUnlessM1(JxlBasicInfo::alpha_bits$set, info, alphaBits);
			JxlBasicInfo.alpha_exponent_bits$set(info, alphaExponentBits);
			applyBoolintUnlessNull(JxlBasicInfo::alpha_premultiplied$set, info, alphaPremultiplied);
			applyUnlessM1(JxlBasicInfo::intrinsic_xsize$set, info, intrinsicXsize);
			applyUnlessM1(JxlBasicInfo::intrinsic_ysize$set, info, intrinsicYsize);
			check(JxlEncoderSetBasicInfo(enc, info));
			
			 if (colorEncoding instanceof JXLICCColorEncoding icc) {
				var data = alloc.allocate(icc.data().length);
				data.copyFrom(MemorySegment.ofArray(icc.data()));
				JxlEncoderSetICCProfile(enc, data, data.byteSize());
			} else {
				var cenc = JxlColorEncoding.allocate(alloc);
				if (colorEncoding instanceof JXLPredefinedColorEncoding p) {
					switch (p) {
						case LINEAR_SRGB -> JxlColorEncodingSetToLinearSRGB(cenc, 0);
						case LINEAR_SRGB_GRAY -> JxlColorEncodingSetToLinearSRGB(cenc, 1);
						case SRGB -> JxlColorEncodingSetToSRGB(cenc, 0);
						case SRGB_GRAY -> JxlColorEncodingSetToSRGB(cenc, 1);
					}
				} else if (colorEncoding instanceof JXLCustomColorEncoding c) {
					JxlColorEncoding.color_space$set(cenc, switch (c.colorSpace()) {
						case GRAY -> JXL_COLOR_SPACE_GRAY();
						case RGB -> JXL_COLOR_SPACE_RGB();
						case XYB -> JXL_COLOR_SPACE_XYB();
						case UNKNOWN -> JXL_COLOR_SPACE_UNKNOWN();
					});
					var jwp = c.whitePoint();
					int wp;
					if (jwp == JXLWhitePoint.D65) {
						wp = JXL_WHITE_POINT_D65();
					} else if (jwp == JXLWhitePoint.E) {
						wp = JXL_WHITE_POINT_E();
					} else if (jwp == JXLWhitePoint.DCI) {
						wp = JXL_WHITE_POINT_DCI();
					} else {
						wp = JXL_WHITE_POINT_CUSTOM();
					}
					JxlColorEncoding.white_point$set(cenc, wp);
					if (wp == JXL_WHITE_POINT_CUSTOM()) {
						var xy = JxlColorEncoding.white_point_xy$slice(cenc);
						xy.set(OfDouble.JAVA_DOUBLE, 0, jwp.x());
						xy.set(OfDouble.JAVA_DOUBLE, 8, jwp.y());
					}
					var jpri = c.primaries();
					int pri;
					if (jpri == JXLPrimaries.SRGB) {
						pri = JXL_PRIMARIES_SRGB();
					} else if (jpri == JXLPrimaries.BT2100) {
						pri = JXL_PRIMARIES_2100();
					} else if (jpri == JXLPrimaries.P3) {
						pri = JXL_PRIMARIES_P3();
					} else {
						pri = JXL_PRIMARIES_CUSTOM();
					}
					JxlColorEncoding.primaries$set(cenc, pri);
					if (pri == JXL_PRIMARIES_CUSTOM()) {
						var redxy = JxlColorEncoding.primaries_red_xy$slice(cenc);
						redxy.set(ValueLayout.JAVA_DOUBLE, 0, jpri.redX());
						redxy.set(ValueLayout.JAVA_DOUBLE, 8, jpri.redY());
						var greenxy = JxlColorEncoding.primaries_green_xy$slice(cenc);
						greenxy.set(ValueLayout.JAVA_DOUBLE, 0, jpri.greenX());
						greenxy.set(ValueLayout.JAVA_DOUBLE, 8, jpri.greenY());
						var bluexy = JxlColorEncoding.primaries_blue_xy$slice(cenc);
						bluexy.set(ValueLayout.JAVA_DOUBLE, 0, jpri.blueX());
						bluexy.set(ValueLayout.JAVA_DOUBLE, 8, jpri.blueY());
					}
					if (c.transferFunction() instanceof JXLPredefinedTransferFunction f) {
						JxlColorEncoding.transfer_function$set(cenc, switch (f) {
							case SMPTE_709 -> JXL_TRANSFER_FUNCTION_709();
							case LINEAR -> JXL_TRANSFER_FUNCTION_LINEAR();
							case SRGB -> JXL_TRANSFER_FUNCTION_SRGB();
							case PQ -> JXL_TRANSFER_FUNCTION_PQ();
							case DCI -> JXL_TRANSFER_FUNCTION_DCI();
							case HLG -> JXL_TRANSFER_FUNCTION_HLG();
						});
					} else if (c.transferFunction() == JXLTransferFunction.UNKNOWN) {
						JxlColorEncoding.transfer_function$set(cenc, JXL_TRANSFER_FUNCTION_UNKNOWN());
					} else if (c.transferFunction() instanceof JXLGammaTransferFunction g) {
						JxlColorEncoding.transfer_function$set(cenc, JXL_TRANSFER_FUNCTION_GAMMA());
						JxlColorEncoding.gamma$set(cenc, g.gamma());
					}
					JxlColorEncoding.rendering_intent$set(cenc, switch (c.intent()) {
						case ABSOLUTE -> JXL_RENDERING_INTENT_ABSOLUTE();
						case PERCEPTUAL -> JXL_RENDERING_INTENT_PERCEPTUAL();
						case RELATIVE -> JXL_RENDERING_INTENT_RELATIVE();
						case SATURATION -> JXL_RENDERING_INTENT_SATURATION();
					});
				}
				check(JxlEncoderSetColorEncoding(enc, cenc));
			}
			check(JxlEncoderSetParallelRunner(enc, FunctionAddresses.JxlThreadParallelRunner$ADDR, jxl_runner));

			var fmt = JxlPixelFormat.allocate(alloc);
			var hdr = JxlFrameHeader.allocate(alloc);
			var bitDepth = JxlBitDepth.allocate(alloc);
			var nameBuf = alloc.allocate(1072); // maximum as imposed by libjxl
			var extraChannelInfo = JxlExtraChannelInfo.allocate(alloc);
			var standaloneBlendInfo = JxlBlendInfo.allocate(alloc);
			for (var frame : frames) {
				var set = JxlEncoderFrameSettingsCreate(enc, MemorySegment.NULL);
				var layerInfo = JxlFrameHeader.layer_info$slice(hdr);
				var blendInfo = JxlLayerInfo.blend_info$slice(layerInfo);
				JxlEncoderInitFrameHeader(hdr);
				JxlEncoderInitBlendInfo(blendInfo);
				
				JxlFrameHeader.duration$set(hdr, frame.duration);
				JxlFrameHeader.timecode$set(hdr, frame.timecode);
				
				JxlLayerInfo.have_crop$set(layerInfo, frame.haveCrop ? 1 : 0);
				JxlLayerInfo.crop_x0$set(layerInfo, frame.cropX);
				JxlLayerInfo.crop_y0$set(layerInfo, frame.cropY);
				JxlLayerInfo.xsize$set(layerInfo, frame.haveCrop ? frame.xsize : xsize);
				JxlLayerInfo.ysize$set(layerInfo, frame.haveCrop ? frame.ysize : ysize);
				JxlLayerInfo.save_as_reference$set(layerInfo, frame.saveAsReference);
				
				JxlBlendInfo.blendmode$set(blendInfo, switch (frame.blendMode) {
					case REPLACE -> JXL_BLEND_REPLACE();
					case ADD -> JXL_BLEND_ADD();
					case BLEND -> JXL_BLEND_BLEND();
					case MULADD -> JXL_BLEND_MULADD();
					case MUL -> JXL_BLEND_MUL();
				});
				JxlBlendInfo.source$set(blendInfo, frame.source);
				JxlBlendInfo.alpha$set(blendInfo, frame.alpha);
				JxlBlendInfo.clamp$set(blendInfo, frame.clamp ? 1 : 0);
				
				check(JxlEncoderSetFrameHeader(set, hdr));
				if (frame.name != null) {
					var strseg = MemorySegment.ofArray(frame.name.getBytes(StandardCharsets.UTF_8));
					nameBuf.copyFrom(strseg);
					nameBuf.set(OfByte.JAVA_BYTE, strseg.byteSize(), (byte)0);
					check(JxlEncoderSetFrameName(set, nameBuf));
				}
				JxlBitDepth.type$set(bitDepth, switch (frame.bitDepthType) {
					case FROM_PIXEL_FORMAT -> JXL_BIT_DEPTH_FROM_PIXEL_FORMAT();
					case FROM_CODESTREAM -> JXL_BIT_DEPTH_FROM_CODESTREAM();
				});
				check(JxlEncoderSetFrameBitDepth(set, bitDepth));

				if (frame.distance <= 0) {
					check(JxlEncoderSetFrameLossless(set, 1));
				} else {
					check(JxlEncoderSetFrameDistance(set, frame.distance));
				}
				
				// JxlEncoderFrameSettingsSetOption
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_EFFORT(), frame.effort));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_DECODING_SPEED(), frame.decodingSpeed));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_RESAMPLING(), frame.resampling));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_EXTRA_CHANNEL_RESAMPLING(), frame.extraChannelResampling));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_ALREADY_DOWNSAMPLED(), frame.alreadyDownsampled ? 1 : 0));
				check(JxlEncoderFrameSettingsSetFloatOption(set, JXL_ENC_FRAME_SETTING_PHOTON_NOISE(), frame.photonNoise));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_DOTS(), nullableBoolint(frame.dots)));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_PATCHES(), nullableBoolint(frame.patches)-1));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_EPF(), frame.epf.ordinal()-1));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_GABORISH(), nullableBoolint(frame.gaborish)));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_MODULAR(), frame.modular));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_KEEP_INVISIBLE(), nullableBoolint(frame.keepInvisible)));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_GROUP_ORDER(), frame.groupOrder));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_GROUP_ORDER_CENTER_X(), frame.groupOrderCenterX));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_GROUP_ORDER_CENTER_Y(), frame.groupOrderCenterY));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_RESPONSIVE(), nullableBoolint(frame.responsive)));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_PROGRESSIVE_AC(), nullableBoolint(frame.progressiveAc)));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_QPROGRESSIVE_AC(), nullableBoolint(frame.qprogressiveAc)));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_PROGRESSIVE_DC(), frame.progressiveDc.ordinal()-1));
				check(JxlEncoderFrameSettingsSetFloatOption(set, JXL_ENC_FRAME_SETTING_CHANNEL_COLORS_GLOBAL_PERCENT(), frame.channelColorsGlobalPercent));
				check(JxlEncoderFrameSettingsSetFloatOption(set, JXL_ENC_FRAME_SETTING_CHANNEL_COLORS_GROUP_PERCENT(), frame.channelColorsGroupPercent));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_PALETTE_COLORS(), frame.paletteColors));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_LOSSY_PALETTE(), nullableBoolint(frame.lossyPalette)));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_COLOR_TRANSFORM(), frame.colorTransform.ordinal()-1));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_MODULAR_COLOR_SPACE(), frame.modularColorSpace));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_MODULAR_GROUP_SIZE(), frame.modularGroupSize.ordinal()-1));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_MODULAR_PREDICTOR(), frame.modularPredictor.ordinal()-1));
				check(JxlEncoderFrameSettingsSetFloatOption(set, JXL_ENC_FRAME_SETTING_MODULAR_MA_TREE_LEARNING_PERCENT(), frame.modularMaTreeLearningPercent));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_MODULAR_NB_PREV_CHANNELS(), frame.modularNbPrevChannels));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_JPEG_RECON_CFL(), nullableBoolint(frame.jpegReconCfl)));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_INDEX_BOX(), frame.indexBox ? 1 : 0));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_BROTLI_EFFORT(), frame.brotliEffort));
				check(JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_JPEG_COMPRESS_BOXES(), nullableBoolint(frame.jpegCompressBoxes)));
				// don't check, this is a new option and may be unavailable
				if (frame.buffering != JXLBuffering.AUTO)
					JxlEncoderFrameSettingsSetOption(set, JXL_ENC_FRAME_SETTING_BUFFERING(), frame.buffering.ordinal()-1);
				
				int i = 0;
				for (var ex : frame.extraChannels) {
					JxlEncoderInitExtraChannelInfo(switch (ex.type) {
						case ALPHA -> JXL_CHANNEL_ALPHA();
						case BLACK -> JXL_CHANNEL_BLACK();
						case CFA -> JXL_CHANNEL_CFA();
						case DEPTH -> JXL_CHANNEL_DEPTH();
						case OPTIONAL -> JXL_CHANNEL_OPTIONAL();
						case RESERVED0 -> JXL_CHANNEL_RESERVED0();
						case RESERVED1 -> JXL_CHANNEL_RESERVED1();
						case RESERVED2 -> JXL_CHANNEL_RESERVED2();
						case RESERVED3 -> JXL_CHANNEL_RESERVED3();
						case RESERVED4 -> JXL_CHANNEL_RESERVED4();
						case RESERVED5 -> JXL_CHANNEL_RESERVED5();
						case RESERVED6 -> JXL_CHANNEL_RESERVED6();
						case RESERVED7 -> JXL_CHANNEL_RESERVED7();
						case SELECTION_MASK -> JXL_CHANNEL_SELECTION_MASK();
						case SPOT_COLOR -> JXL_CHANNEL_SPOT_COLOR();
						case THERMAL -> JXL_CHANNEL_THERMAL();
						case UNKNOWN -> JXL_CHANNEL_UNKNOWN();
					}, extraChannelInfo);
					var nameUtf = ex.name == null ? null : alloc.allocateUtf8String(ex.name);
					JxlExtraChannelInfo.bits_per_sample$set(extraChannelInfo, ex.bitsPerSample);
					JxlExtraChannelInfo.exponent_bits_per_sample$set(extraChannelInfo, ex.exponentBitsPerSample);
					if (ex.dimShift != -1)
						JxlExtraChannelInfo.dim_shift$set(extraChannelInfo, ex.dimShift);
					JxlExtraChannelInfo.alpha_premultiplied$set(extraChannelInfo, ex.alphaPremultiplied ? 1 : 0);
					if (ex.spotColor != null)
						JxlExtraChannelInfo.spot_color$slice(extraChannelInfo).copyFrom(MemorySegment.ofArray(ex.spotColor));
					if (ex.cfaChannel != -1)
						JxlExtraChannelInfo.cfa_channel$set(extraChannelInfo, ex.cfaChannel);
					// is this needed? this api is so weird
					JxlExtraChannelInfo.name_length$set(extraChannelInfo, nameUtf == null ? 0 : (int)nameUtf.byteSize());
					check(JxlEncoderSetExtraChannelInfo(enc, i, extraChannelInfo));
					
					JxlBlendInfo.blendmode$set(standaloneBlendInfo, switch (ex.blendMode) {
						case REPLACE -> JXL_BLEND_REPLACE();
						case ADD -> JXL_BLEND_ADD();
						case BLEND -> JXL_BLEND_BLEND();
						case MULADD -> JXL_BLEND_MULADD();
						case MUL -> JXL_BLEND_MUL();
					});
					JxlBlendInfo.source$set(standaloneBlendInfo, ex.source);
					JxlBlendInfo.alpha$set(standaloneBlendInfo, ex.alpha);
					JxlBlendInfo.clamp$set(standaloneBlendInfo, ex.clamp ? 1 : 0);
					check(JxlEncoderSetExtraChannelBlendInfo(set, i, standaloneBlendInfo));
					if (nameUtf != null)
						check(JxlEncoderSetExtraChannelName(enc, i, nameUtf, nameUtf.byteSize()-1));
					i++;
				}
				
				if (frame.isJpeg) {
					check(JxlEncoderAddJPEGFrame(set, frame.data.data(), frame.data.data().byteSize()));
				} else {
					loadInto(frame.data, fmt);
					check(JxlEncoderAddImageFrame(set, fmt, frame.data.data(), frame.data.data().byteSize()));
				}

				i = 0;
				for (var extra : frame.extraChannelData) {
					loadInto(extra, fmt);
					check(JxlEncoderSetExtraChannelBuffer(set, fmt, extra.data(), extra.data().byteSize(), i));
					i++;
				}
			}
			JxlEncoderCloseInput(enc);
			var buf = alloc.allocate(8192);
			var avail = alloc.allocate(ValueLayout.JAVA_LONG, 8192);
			var ptr = alloc.allocate(ValueLayout.ADDRESS, buf);
			while (true) {
				int res = JxlEncoderProcessOutput(enc, ptr, avail);
				if (res == JXL_ENC_ERROR()) {
					throw new JXLException("JXL encoding failed"+NO_MORE_INFO);
				}
				long len = 8192-avail.get(ValueLayout.JAVA_LONG, 0);
				out.write(buf.asSlice(0, len).toArray(ValueLayout.JAVA_BYTE));
				avail.set(ValueLayout.JAVA_LONG, 0, 8192);
				ptr.set(ValueLayout.ADDRESS, 0, buf);
				if (res == JXL_ENC_SUCCESS()) {
					break;
				}
			}
		} finally {
			JxlEncoderDestroy(enc);
		}
	}
	
	private void loadInto(JXLFrameData data, MemorySegment fmt) {
		JxlPixelFormat.data_type$set(fmt, switch (data.dataType()) {
			case FLOAT32 -> JXL_TYPE_FLOAT();
			case UINT8 -> JXL_TYPE_UINT8();
			case UINT16 -> JXL_TYPE_UINT16();
			case FLOAT16 -> JXL_TYPE_FLOAT16();
		});
		JxlPixelFormat.num_channels$set(fmt, data.numChannels());
		JxlPixelFormat.endianness$set(fmt, switch (data.endianness()) {
			case NATIVE -> JXL_NATIVE_ENDIAN();
			case BIG -> JXL_BIG_ENDIAN();
			case LITTLE -> JXL_LITTLE_ENDIAN();
		});
		JxlPixelFormat.align$set(fmt, data.align());
	}

	public byte[] encode() throws IOException {
		var baos = new ByteArrayOutputStream();
		encodeToStream(baos);
		return baos.toByteArray();
	}
	
}
