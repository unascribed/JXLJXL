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

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SegmentScope;
import java.lang.foreign.ValueLayout;
import java.nio.Buffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class JXLFrameBuilder {
	
	public enum JXLEPFMode {
		/** Encoder chooses */
		AUTO,
		OFF,
		WEAK,
		MEDIUM,
		STRONG,
	}
	
	public enum JXLProgressiveDC {
		/** Encoder chooses */
		AUTO,
		/** No lower-resolution DC images */
		DISABLE,
		/** One extra 64x64 pass */
		ENABLE_64,
		/** Extra 512x512 and 64x64 passes */
		ENABLE_512_AND_64,
	}
	
	public enum JXLBlendMode {
		REPLACE,
		ADD,
		BLEND,
		MULADD,
		MUL,
	}
	
	public enum JXLEncodeStrategy {
		/** Encoder chooses */
		AUTO,
		/** Variable DCT mode (like classic JPEG), best for photographic images. */
		VARDCT,
		/** Modular mode, best for lossless images and graphics. */
		MODULAR,
	}
	
	public enum JXLBitDepthType {
		/**
		 * This is the default setting, where the encoder expects the input pixels to use the full
		 * range of the pixel format data type (e.g. for UINT16, the input range is 0 .. 65535 and
		 * the value 65535 is mapped to 1.0 when converting to float), and the decoder uses the full
		 * range to output pixels. If the bit depth in the basic info is different from this, the
		 * encoder expects the values to be rescaled accordingly (e.g. multiplied by 65535/4095 for
		 * a 12-bit image using UINT16 input data type).
		 */
		FROM_PIXEL_FORMAT,
		/**
		 * If this setting is selected, the encoder expects the input pixels to be in the range
		 * defined by the bits_per_sample value of the basic info (e.g. for 12-bit images using
		 * UINT16 input data types, the allowed range is 0 .. 4095 and the value 4095 is mapped to
		 * 1.0 when converting to float), and the decoder outputs pixels in this range.
		 */
		FROM_CODESTREAM,
		;
	}
	
	public enum JXLColorTransform {
		/** Encoder chooses */
		AUTO,
		/** Performs the forward XYB transform. */
		XYB,
		/** No transform. (RGB) */
		NONE,
		/** No transform. Used to indicate that the encoded data losslessly represents YCbCr values. */
		YCbCr,
	}
	
	public enum JXLModularGroupSize {
		/** Encoder chooses */
		AUTO,
		_128,
		_256,
		_512,
		_1024,
	}
	
	public enum JXLModularPredictor {
		/** Encoder chooses */
		AUTO,
		ZERO, LEFT, TOP, AVG0, SELECT, GRADIENT, WEIGHTED, TOPRIGHT, TOPLEFT, LEFTLEFT, AVG1,
		AVG2, AVG3, TOPTOP_PREDICTIVE_AVERAGE,
		/** Gradient + weighted */
		GRADIENT_WEIGHTED,
		/** Combination of all others */
		ALL
	}
	
	public enum JXLBuffering {
		/** Encoder chooses */
		AUTO,
		/** Buffers everything, basically the same as non-streamed code path (mainly for testing)  */
		EVERYTHING,
		/** Buffer internal data (the tokens) */
		TOKENS,
		/** Buffer the outuput */
		OUTPUT,
		/** Minimize buffer usage: streamed input and chunked output, writing TOC last (will not work with progressive) */
		MINIMAL
	}
	
	public enum JXLByteOrder {
		/**
		 * Use the endianness of the system, either little endian or big endian, without forcing
		 * either specific endianness. Do not use if pixel data should be exported to a well defined
		 * format.
		 */
		NATIVE,
		/**
		 * Force big-endian. In Java, you almost always want this.
		 */
		BIG,
		/**
		 * Force little-endian.
		 */
		LITTLE,
	}
	
	final JXLEncoder owner;
	
	// JxlFrameHeader
	int duration;
	int timecode;
	// JxlLayerInfo
	boolean haveCrop = false;
	int cropX;
	int cropY;
	int xsize;
	int ysize;
	int saveAsReference = 0;
	// JxlBlendInfo
	JXLBlendMode blendMode = JXLBlendMode.REPLACE;
	int source;
	int alpha;
	boolean clamp = false;
	
	
	// JxlEncoderFrameSettingsSetOption
	int effort = 7;
	int decodingSpeed = 0;
	int resampling = -1;
	int extraChannelResampling = -1;
	boolean alreadyDownsampled = false;
	float photonNoise = 0;
	Boolean dots = null;
	Boolean patches = null;
	JXLEPFMode epf = JXLEPFMode.AUTO;
	Boolean gaborish = null;
	int modular = -1;
	Boolean keepInvisible = null;
	int groupOrder = -1;
	int groupOrderCenterX = -1;
	int groupOrderCenterY = -1;
	Boolean responsive = null;
	Boolean progressiveAc = null;
	Boolean qprogressiveAc = null;
	JXLProgressiveDC progressiveDc = JXLProgressiveDC.AUTO;
	float channelColorsGlobalPercent = -1;
	float channelColorsGroupPercent = -1;
	int paletteColors = -1;
	Boolean lossyPalette = null;
	JXLColorTransform colorTransform = JXLColorTransform.AUTO;
	int modularColorSpace = -1; // TODO provide a way to set this from its components (permutation, transforms)
	JXLModularGroupSize modularGroupSize = JXLModularGroupSize.AUTO;
	JXLModularPredictor modularPredictor = JXLModularPredictor.AUTO;
	float modularMaTreeLearningPercent = -1;
	int modularNbPrevChannels = -1;
	Boolean jpegReconCfl = null;
	boolean indexBox = false;
	int brotliEffort = -1;
	Boolean jpegCompressBoxes = null;
	JXLBuffering buffering = JXLBuffering.AUTO;
	boolean isJpeg = false;
	
	record JXLFrameData(
		int numChannels,
		JXLDataType dataType,
		JXLByteOrder endianness,
		int align,
		MemorySegment data) {}
	
	// AddImageFrame
	JXLFrameData data;
	
	// SetExtraChannelBuffer
	final List<JXLExtraChannelBuilder> extraChannels = new ArrayList<>();
	final List<JXLFrameData> extraChannelData = new ArrayList<>();
	
	// other
	float distance = 1;
	JXLBitDepthType bitDepthType = JXLBitDepthType.FROM_PIXEL_FORMAT;
	String name = null;
	
	
	JXLFrameBuilder(JXLEncoder owner) {
		this.owner = owner;
	}
	
	// simple setters
	
	JXLFrameBuilder name(String name) {
		this.name = name;
		return this;
	}

	JXLFrameBuilder duration(int duration) {
		this.duration = duration;
		return this;
	}

	/**
	 * SMPTE timecode of the current frame in form 0xHHMMSSFF, or 0. The bits are interpreted from
	 * most-significant to least-significant as hour, minute, second, and frame. If timecode is
	 * nonzero, it is strictly larger than that of a previous frame with nonzero duration. This
	 * value is only used if withTimecodes has been set.
	 */
	public JXLFrameBuilder timecode(int packedSmpteTimecode) {
		this.timecode = packedSmpteTimecode;
		return this;
	}

	/**
	 * After blending, save the frame as reference frame with this ID (0-3). Special case: if the
	 * frame duration is nonzero, ID 0 means "will not be referenced in the future". This value is
	 * not used for the last frame. When encoding, ID 3 is reserved to frames that are generated
	 * internally by the encoder, and should not be used by applications.
	 */
	public JXLFrameBuilder saveAsReference(int saveAsReference) {
		this.saveAsReference = saveAsReference;
		return this;
	}

	/**
	 * Set the blend mode for this frame, which affects how it is composited with other frames when
	 * coalescing is enabled in the decoder.
	 */
	public JXLFrameBuilder blendMode(JXLBlendMode blendMode) {
		this.blendMode = blendMode;
		return this;
	}

	/**
	 * Reference frame ID to use as the "bottom" layer (0-3).
	 */
	public JXLFrameBuilder blendSource(int source) {
		this.source = source;
		return this;
	}

	/**
	 * Which extra channel to use as the "alpha" channel for blend modes BLEND and MULADD.
	 */
	public JXLFrameBuilder blendAlpha(int alpha) {
		this.alpha = alpha;
		return this;
	}

	/**
	 * Clamp values to [0,1] for the purpose of blending.
	 */
	public JXLFrameBuilder blendClamp(boolean clamp) {
		this.clamp = clamp;
		return this;
	}

	/**
	 * Sets encoder effort/speed level without affecting decoding speed. Valid values are, from
	 * faster to slower speed:
	 * <ol>
	 * <li>⚡️ Lightning</li>
	 * <li>⛈️ Thunder</li>
	 * <li>🦅 Falcon</li>
	 * <li>🐆 Cheetah</li>
	 * <li>🐇 Hare</li>
	 * <li>🦇 Wombat</li>
	 * <li>🐿️ Squirrel (default)</li>
	 * <li>🐈️ Kitten</li>
	 * <li>🐢 Tortoise</li>
	 * <li>🏔️ Glacier (best for lossless, very slow, requires expert options)</li>
	 * </ol>
	 */
	public JXLFrameBuilder effort(int effort) {
		this.effort = effort;
		return this;
	}

	/**
	 * Sets the decoding speed tier for the provided options. Minimum is 0 (slowest to decode, best
	 * quality/density), and maximum is 4 (fastest to decode, at the cost of some quality/density).
	 * Default is 0.
	 */
	public JXLFrameBuilder decodingSpeed(int decodingSpeed) {
		this.decodingSpeed = decodingSpeed;
		return this;
	}

	/**
	 * Sets resampling option. If enabled, the image is downsampled before compression, and
	 * upsampled to original size in the decoder. Integer option, use -1 for the default behavior
	 * (resampling only applied for low quality), 1 for no downsampling (1x1), 2 for 2x2
	 * downsampling, 4 for 4x4 downsampling, 8 for 8x8 downsampling.
	 */
	public JXLFrameBuilder resampling(int resampling) {
		this.resampling = resampling;
		return this;
	}

	/**
	 * Similar to {@link #resampling(int) resampling}, but for extra channels. Integer option, use -1 for the
	 * default behavior (depends on encoder implementation), 1 for no downsampling (1x1), 2 for 2x2
	 * downsampling, 4 for 4x4 downsampling, 8 for 8x8 downsampling.
	 */
	public JXLFrameBuilder extraChannelResampling(int extraChannelResampling) {
		this.extraChannelResampling = extraChannelResampling;
		return this;
	}

	/**
	 * Indicates the frame data added with {@link #commit} is already downsampled by the
	 * downsampling factor set with {@link #resampling(int) resampling}. The input frame must then
	 * be given in the downsampled resolution, not the full image resolution. The downsampled
	 * resolution is given by {@code ceil(xsize / resampling), ceil(ysize / resampling)} with xsize
	 * and ysize the dimensions given in the basic info, and resampling the factor set with
	 * {@link #resampling(int) resampling}. Default is false.
	 */
	public JXLFrameBuilder alreadyDownsampled(boolean alreadyDownsampled) {
		this.alreadyDownsampled = alreadyDownsampled;
		return this;
	}

	/**
	 * Adds noise to the image emulating photographic film noise, the higher the given number, the
	 * grainier the image will be. As an example, a value of 100 gives low noise whereas a value of
	 * 3200 gives a lot of noise. The default value is 0.
	 */
	public JXLFrameBuilder photonNoise(float photonNoise) {
		this.photonNoise = photonNoise;
		return this;
	}

	/**
	 * Enables or disables dots generation.
	 */
	public JXLFrameBuilder dots(boolean dots) {
		this.dots = dots;
		return this;
	}

	/**
	 * Enables or disables patches generation.
	 */
	public JXLFrameBuilder patches(boolean patches) {
		this.patches = patches;
		return this;
	}

	/**
	 * Edge preserving filter level.
	 */
	public JXLFrameBuilder epf(JXLEPFMode epf) {
		this.epf = epf;
		return this;
	}

	/**
	 * Enables or disables the gaborish filter.
	 */
	public JXLFrameBuilder gaborish(boolean gaborish) {
		this.gaborish = gaborish;
		return this;
	}

	/**
	 * Enables or disables preserving color of invisible pixels. If not set, will be automatically
	 * enabled for lossless images.
	 */
	public JXLFrameBuilder keepInvisible(boolean keepInvisible) {
		this.keepInvisible = keepInvisible;
		return this;
	}

	/**
	 * Sets the distance level for lossy compression: target max butteraugli distance, lower =
	 * higher quality. 0.0 = mathematically lossless. 1.0 = visually lossless.
	 * <p>
	 * Recommended range: 0.5 .. 3.0. Allowed range: 0 .. 15. Default value: 1.0. JXLJXL will
	 * automatically enable true lossless mode if this is set to 0.0.
	 */
	public JXLFrameBuilder distance(float distance) {
		this.distance = distance;
		return this;
	}
	
	/**
	 * Sets the bit depth of the input buffer.
	 * <p>
	 * For float pixel formats, only the default FROM_PIXEL_FORMAT setting is allowed, while for
	 * unsigned pixel formats, FROM_CODESTREAM setting is also allowed.
	 * <p>
	 * For example, to encode a 12-bit image, you would set bitsPerSample to 12, while the input
	 * frame buffer can be in the following formats:
	 * <ul>
	 * <li>if pixel format is in UINT16 with default bit depth setting (i.e. JFROM_PIXEL_FORMAT),
	 * input sample values are rescaled to 16-bit, i.e. multiplied by 65535/4095;</li>
	 * <li>if pixel format is in _UINT16 with FROM_CODESTREAM bit depth setting, input sample values
	 * are provided unscaled;</li>
	 * <li>if pixel format is in FLOAT32 or FLOAT16, input sample values are rescaled to 0..1, i.e.
	 * multiplied by 1.f/4095.f. While it is allowed, it is obviously not recommended to use a
	 * pixel_format with lower precision than what is specified.</li>
	 * </ul>
	 */
	public JXLFrameBuilder bitDepthType(JXLBitDepthType bitDepthType) {
		this.bitDepthType = bitDepthType;
		return this;
	}
	
	/**
	 * Color transform for internal encoding.
	 */
	public JXLFrameBuilder colorTransform(JXLColorTransform colorTransform) {
		this.colorTransform = colorTransform;
		return this;
	}
	
	/**
	 * Enable or disable CFL (chroma-from-luma) for lossless JPEG recompression.
	 */
	public JXLFrameBuilder jpegReconCfl(boolean jpegReconCfl) {
		this.jpegReconCfl = jpegReconCfl;
		return this;
	}
	
	/**
	 * Prepare the frame for indexing in the frame index box. false = ignore this frame (same as not
	 * setting a value), true = index this frame within the Frame Index Box. If any frames are
	 * indexed, the first frame needs to be indexed, too. If the first frame is not indexed, and a
	 * later frame is attempted to be indexed, an encoding error will occur. If non-keyframes, i.e.,
	 * frames with cropping, blending or patches are attempted to be indexed, an error will occur.
	 */
	public JXLFrameBuilder frameIndexBox(boolean frameIndexBox) {
		this.indexBox = frameIndexBox;
		return this;
	}
	
	/**
	 * Sets brotli encode effort for use in JPEG recompression and compressed metadata boxes (brob).
	 * Can be -1 (default) or 0 (fastest) to 11 (slowest). Default is based on the general encode
	 * effort in case of JPEG recompression, and 4 for brob boxes.
	 */
	public JXLFrameBuilder brotliEffort(int brotliEffort) {
		this.brotliEffort = brotliEffort;
		return this;
	}
	
	/**
	 * Enables or disables brotli compression of metadata boxes derived from a JPEG frame when using
	 * {@link #commitJpeg}. This has no effect on boxes added using {@code addBox}. -1 =
	 * default, 0 = disable compression, 1 = enable compression.
	 */
	public JXLFrameBuilder jpegCompressBoxes(boolean jpegCompressBoxes) {
		this.jpegCompressBoxes = jpegCompressBoxes;
		return this;
	}
	
	/**
	 * Control what kind of buffering is used, when using chunked image frames. When the image
	 * dimensions is smaller than 2048 x 2048 all the options are the same. Using
	 * {@link JXLBuffering#TOKENS TOKENS}, {@link JXLBuffering#OUTPUT OUTPUT}, or
	 * {@link JXLBuffering#MINIMAL MINIMAL} can result increasingly in less compression density.
	 */
	public JXLFrameBuilder buffering(JXLBuffering buffering) {
		this.buffering = buffering;
		return this;
	}
	
	private sealed interface JXLModularConfiguratorBase<T extends JXLModularConfiguratorBase<T>> permits JXLDualConfigurator, JXLModularConfigurator {

		/**
		 * Enables or disables progressive encoding for modular mode.
		 */
		T responsive(boolean responsive);

		/**
		 * Use Global channel palette if the amount of colors is smaller than this percentage of range.
		 * Use 0-100 to set an explicit percentage, -1 to use the encoder default. Used for modular
		 * encoding.
		 */
		T channelColorsGlobalPercent(float channelColorsGlobalPercent);

		/**
		 * Use Local (per-group) channel palette if the amount of colors is smaller than this percentage
		 * of range. Use 0-100 to set an explicit percentage, -1 to use the encoder default. Used for
		 * modular encoding.
		 */
		T channelColorsGroupPercent(float channelColorsGroupPercent);

		/**
		 * Use color palette if amount of colors is smaller than or equal to this amount, or -1 to use
		 * the encoder default. Used for modular encoding.
		 */
		T paletteColors(int paletteColors);

		/**
		 * Enables or disables delta palette. Used in modular mode.
		 */
		T lossyPalette(boolean lossyPalette);

		/**
		 * Reversible color transform for modular encoding: -1=default, 0-41=RCT index, e.g. index 0 =
		 * none, index 6 = YCoCg. If this option is set to a non-default value, the RCT will be globally
		 * applied to the whole frame. The default behavior is to try several RCTs locally per modular
		 * group, depending on the speed and distance setting.
		 */
		T colorSpace(int modularColorSpace);

		/**
		 * Group size for modular encoding.
		 */
		T groupSize(JXLModularGroupSize modularGroupSize);

		/**
		 * Predictor for modular encoding.
		 */
		T predictor(JXLModularPredictor modularPredictor);

		/**
		 * Fraction of pixels used to learn MA trees as a percentage. -1 = default, 0 = no MA and fast
		 * decode, 50 = default value, 100 = all, values above 100 are also permitted. Higher values use
		 * more encoder memory.
		 */
		T maTreeLearningPercent(float modularMaTreeLearningPercent);

		/**
		 * Number of extra (previous-channel) MA tree properties to use. -1 = default, 0-11 = valid
		 * values. Recommended values are in the range 0 to 3, or 0 to amount of channels minus 1
		 * (including all extra channels, and excluding color channels when using VarDCT mode). Higher
		 * value gives slower encoding and slower decoding.
		 */
		T nbPrevChannels(int modularNbPrevChannels);
		
		/**
		 * Finish configuring encoding settings and return the parent.
		 */
		JXLFrameBuilder done();
		
	}
	public sealed interface JXLModularConfigurator extends JXLModularConfiguratorBase<JXLModularConfigurator> permits ModularStrategyConfigurator {}
	
	private sealed interface JXLVarDCTConfiguratorBase<T extends JXLVarDCTConfiguratorBase<T>> permits JXLVarDCTConfigurator, JXLDualConfigurator {

		/**
		 * Set the progressive mode for the AC coefficients of VarDCT, using spectral progression from
		 * the DCT coefficients.
		 */
		T progressiveAc(boolean progressiveAc);

		/**
		 * Set the progressive mode for the AC coefficients of VarDCT, using quantization of the least
		 * significant bits.
		 */
		T qProgressiveAC(boolean qprogressiveAc);

		/**
		 * Set the progressive mode using lower-resolution DC images for VarDCT.
		 */
		T progressiveDC(JXLProgressiveDC progressiveDc);
		
		/**
		 * Finish configuring encoding settings and return the parent.
		 */
		JXLFrameBuilder done();

	}
	public sealed interface JXLVarDCTConfigurator extends JXLVarDCTConfiguratorBase<JXLVarDCTConfigurator> permits VarDCTStrategyConfigurator {}
	
	public sealed interface JXLDualConfigurator extends JXLVarDCTConfiguratorBase<JXLDualConfigurator>, JXLModularConfiguratorBase<JXLDualConfigurator> permits DualStrategyConfigurator {}
	
	private final class VarDCTStrategyConfigurator extends StrategyConfigurator<JXLVarDCTConfigurator> implements JXLVarDCTConfigurator {}
	private final class ModularStrategyConfigurator extends StrategyConfigurator<JXLModularConfigurator> implements JXLModularConfigurator {}
	private final class DualStrategyConfigurator extends StrategyConfigurator<JXLDualConfigurator> implements JXLDualConfigurator {}

	@SuppressWarnings("unchecked")
	private sealed class StrategyConfigurator<T> permits VarDCTStrategyConfigurator, ModularStrategyConfigurator, DualStrategyConfigurator {
		public T responsive(boolean responsive) {
			checkModularOption();
			JXLFrameBuilder.this.responsive = responsive;
			return (T)this;
		}
		
		public T channelColorsGlobalPercent(float channelColorsGlobalPercent) {
			checkModularOption();
			JXLFrameBuilder.this.channelColorsGlobalPercent = channelColorsGlobalPercent;
			return (T)this;
		}
		
		public T channelColorsGroupPercent(float channelColorsGroupPercent) {
			checkModularOption();
			JXLFrameBuilder.this.channelColorsGroupPercent = channelColorsGroupPercent;
			return (T)this;
		}
		
		public T paletteColors(int paletteColors) {
			checkModularOption();
			JXLFrameBuilder.this.paletteColors = paletteColors;
			return (T)this;
		}
		
		public T lossyPalette(boolean lossyPalette) {
			checkModularOption();
			JXLFrameBuilder.this.lossyPalette = lossyPalette;
			return (T)this;
		}
		
		public T colorSpace(int modularColorSpace) {
			checkModularOption();
			JXLFrameBuilder.this.modularColorSpace = modularColorSpace;
			return (T)this;
		}
		
		public T groupSize(JXLModularGroupSize modularGroupSize) {
			checkModularOption();
			JXLFrameBuilder.this.modularGroupSize = modularGroupSize;
			return (T)this;
		}
		
		public T predictor(JXLModularPredictor modularPredictor) {
			checkModularOption();
			JXLFrameBuilder.this.modularPredictor = modularPredictor;
			return (T)this;
		}
		
		public T maTreeLearningPercent(float modularMaTreeLearningPercent) {
			checkModularOption();
			JXLFrameBuilder.this.modularMaTreeLearningPercent = modularMaTreeLearningPercent;
			return (T)this;
		}
		
		public T nbPrevChannels(int modularNbPrevChannels) {
			checkModularOption();
			JXLFrameBuilder.this.modularNbPrevChannels = modularNbPrevChannels;
			return (T)this;
		}

		public T progressiveAc(boolean progressiveAc) {
			checkVarDCTOption();
			JXLFrameBuilder.this.progressiveAc = progressiveAc;
			return (T)this;
		}

		public T qProgressiveAC(boolean qprogressiveAc) {
			checkVarDCTOption();
			JXLFrameBuilder.this.qprogressiveAc = qprogressiveAc;
			return (T)this;
		}

		public T progressiveDC(JXLProgressiveDC progressiveDc) {
			checkVarDCTOption();
			JXLFrameBuilder.this.progressiveDc = progressiveDc;
			return (T)this;
		}

		public JXLFrameBuilder done() {
			return JXLFrameBuilder.this;
		}
	}
	
	// setters with logic

	/**
	 * Force modular encoding and return a builder for configuring modular-specific options.
	 */
	public JXLModularConfigurator withModularEncoding() {
		return new ModularStrategyConfigurator();
	}
	
	/**
	 * Force VarDCT encoding and return a builder for configuring VarDCT-specific options.
	 */
	public JXLVarDCTConfigurator withVarDCTEncoding() {
		return new VarDCTStrategyConfigurator();
	}
	
	/**
	 * Allow the decoder to choose modular or VarDCT encoding, and return a builder for configuring
	 * options for both strategies.
	 */
	public JXLDualConfigurator withAutoEncoding() {
		return new DualStrategyConfigurator();
	}
	
	/**
	 * Determines the order in which 256x256 regions are stored in the codestream for progressive
	 * rendering. This method explicitly selects scanline-order.
	 */
	public JXLFrameBuilder scanlineGroupOrder() {
		this.groupOrder = 0;
		return this;
	}
	
	/**
	 * Determines the order in which 256x256 regions are stored in the codestream for progressive
	 * rendering. This method explicitly selects center-first order, with the given center. Use -1
	 * to automatically use the middle of the image.
	 * <p>
	 * This can be used to do progressive focal point decoding, where the focal point of an image
	 * becomes more detailed before the less important parts.
	 */
	public JXLFrameBuilder centeredGroupOrder(int groupOrderCenterX, int groupOrderCenterY) {
		this.groupOrder = 1;
		this.groupOrderCenterX = groupOrderCenterX;
		this.groupOrderCenterY = groupOrderCenterY;
		return this;
	}
	
	/**
	 * Set an offset and size for this frame, rather than being the size of the entire image as
	 * configured in the encoder.
	 */
	public JXLFrameBuilder crop(int x, int y, int xsize, int ysize) {
		this.haveCrop = true;
		this.cropX = x;
		this.cropY = y;
		this.xsize = xsize;
		this.ysize = ysize;
		return this;
	}

	/**
	 * SMPTE timecode of the current frame. Must be strictly larger than that of a previous frame
	 * with nonzero duration. Only used if withTimecodes has been set.
	 */
	public JXLFrameBuilder timecode(int hours, int minutes, int seconds, int frames) {
		if (hours > 0xFF) throw new IllegalArgumentException("Hours cannot be greater than 255");
		if (minutes > 0xFF) throw new IllegalArgumentException("Minutes cannot be greater than 255");
		if (seconds > 0xFF) throw new IllegalArgumentException("Seconds cannot be greater than 255");
		if (frames > 0xFF) throw new IllegalArgumentException("Frames cannot be greater than 255");
		if (hours < 0) throw new IllegalArgumentException("Hours cannot be less than 0");
		if (minutes < 0) throw new IllegalArgumentException("Minutes cannot be less than 0");
		if (seconds < 0) throw new IllegalArgumentException("Seconds cannot be less than 0");
		if (frames < 0) throw new IllegalArgumentException("Frames cannot be less than 0");
		this.timecode = ((hours&0xFF)<<24)|((minutes&0xFF)<<16)|((seconds&0xFF)<<8)|(frames&0xFF);
		return this;
	}

	/**
	 * Configure encoding quality in terms of a JPEG-style 0-100 rather than Butteraugli distance.
	 * 100 = mathematically lossless. 90 = visually lossless.
	 * <p>
	 * Recommended range: 68 .. 96. Allowed range: 0 .. 100. Internally mapped to
	 * {@link #distance(float) distance}.
	 */
	public JXLFrameBuilder quality(float quality) {
		// https://github.com/libjxl/libjxl/blob/main/tools/cjxl_main.cc#L629
		distance(quality >= 100 ? 0.0f
				: quality >= 30
					? 0.1f + (100 - quality) * 0.09f
					: 53.0f / 3000.0f * quality * quality -
						23.0f / 20.0f * quality + 25.0f);
		return this;
	}
	
	/**
	 * Define a new extra channel with the next available index.
	 * @throws IllegalArgumentException if numExtraCHannels is not large enough
	 */
	public JXLExtraChannelBuilder newExtraChannel() {
		if (extraChannels.size() >= owner.numExtraChannels)
			throw new IllegalArgumentException("Out of extra channels");
		return new JXLExtraChannelBuilder(this);
	}
	/**
	 * Define a new named extra channel with the next available index.
	 * @throws IllegalArgumentException if numExtraCHannels is not large enough
	 */
	public JXLExtraChannelBuilder newExtraChannel(String name) {
		return newExtraChannel().name(name);
	}
	
	
	/**
	 * Commit this frame to the encoder with the given frame data. It is assumed to be 8-bit-per-sample
	 * in ARGB order, which is emitted by most Java APIs. (BGRA in little-endian order.)
	 */
	public JXLEncoder commit(int channels, int align, int[] data) {
		return commit(channels, JXLDataType.UINT8, JXLByteOrder.BIG, align, swapped(data));
	}
	
	/**
	 * Commit this frame to the encoder with the given frame data.
	 */
	public JXLEncoder commit(int channels, JXLDataType dataType, JXLByteOrder endianness, int align, byte[] data) {
		return commit(channels, dataType, endianness, align, MemorySegment.ofArray(data));
	}
	
	/**
	 * Commit this frame to the encoder with the frame data from the given AWT BufferedImage.
	 */
	public JXLEncoder commit(BufferedImage img) {
		if (img.isAlphaPremultiplied() != owner.alphaPremultiplied) {
			if (owner.alphaPremultiplied) {
				throw new IllegalArgumentException("Cannot add a straight alpha image to a premultiplied alpha JXL");
			} else {
				throw new IllegalArgumentException("Cannot add a premultiplied alpha image to a straight alpha JXL");
			}
		}
		if (haveCrop) {
			int[] data = new int[xsize*ysize];
			img.getRGB(cropX, cropY, xsize, ysize, data, 0, xsize);
			return commit(4, 0, data);
		}
		return switch (img.getType()) {
			case BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_INT_ARGB_PRE ->
				commit(4, 0, ((DataBufferInt)img.getRaster().getDataBuffer()).getData());
			default -> {
				int[] data = new int[img.getWidth()*img.getHeight()];
				img.getRGB(0, 0, img.getWidth(), img.getHeight(), data, 0, img.getWidth());
				yield commit(4, 0, data);
			}
		};
	}
	
	private static final ValueLayout.OfInt SWAPPED_INT = ValueLayout.JAVA_INT.withOrder(ByteOrder.BIG_ENDIAN);
	
	MemorySegment swapped(int[] arr) {
		var cpy = SegmentAllocator.nativeAllocator(SegmentScope.auto()).allocate(arr.length*4);
		for (int i = 0; i < arr.length; i++) {
			cpy.set(SWAPPED_INT, i*4, Integer.rotateLeft(arr[i], 8));
		}
		return cpy;
	}

	/**
	 * Commit this frame to the encoder with the given frame data.
	 */
	public JXLEncoder commit(int channels, JXLDataType dataType, JXLByteOrder endianness, int align, Buffer data) {
		return commit(channels, dataType, endianness, align, MemorySegment.ofBuffer(data));
	}
	
	/**
	 * Commit this frame to the encoder with the given frame data.
	 */
	public JXLEncoder commit(int channels, JXLDataType dataType, JXLByteOrder endianness, int align, MemorySegment data) {
		checkDataSize(channels, dataType, align, data);
		
		this.data = new JXLFrameData(channels, dataType, endianness, align, ensureNative(data));
		owner.frames.add(this);
		return owner;
	}

	/**
	 * Commit this frame to the encoder as a raw JPEG bitstream.
	 */
	public JXLEncoder commitJpeg(byte[] data) {
		return commitJpeg(MemorySegment.ofArray(data));
	}
	
	/**
	 * Commit this frame to the encoder as a raw JPEG bitstream.
	 */
	public JXLEncoder commitJpeg(MemorySegment data) {
		if (!extraChannelData.isEmpty()) throw new IllegalArgumentException("Attempting to commit a frame as JPEG when it has already had raw data added");
		this.isJpeg = true;
		this.data = new JXLFrameData(0, null, null, 0, ensureNative(data));
		owner.frames.add(this);
		return owner;
	}
	
	void checkDataSize(int channels, JXLDataType dataType, int align, MemorySegment data) {
		int w = haveCrop ? xsize : owner.xsize;
		int h = haveCrop ? ysize : owner.ysize;
		if (alreadyDownsampled) {
			w = divRoundUp(w, resampling);
			h = divRoundUp(h, resampling);
		}
		if (align == 0) align = 1;
		int expectedSize = channels*(divRoundUp(w*dataType.bytes, align)*align)*h;
		if (data.byteSize() < expectedSize) throw new IllegalArgumentException("Not enough data for specified frame size and data format (need "+expectedSize+" bytes, got "+data.byteSize()+" bytes). Did you forget to set a crop?");
	}
	
	private static int divRoundUp(int p, int q) {
		return (p+(q-1))/q;
	}
	
	MemorySegment ensureNative(MemorySegment data) {
		if (data.array().isPresent()) {
			var cpy = SegmentAllocator.nativeAllocator(SegmentScope.auto()).allocate(data.byteSize());
			cpy.copyFrom(data);
			return cpy;
		}
		return data;
	}
	
	private void checkModularOption() {
		if (modular == 0) throw new IllegalStateException("Attempting to set a Modular-only option in VarDCT mode");
	}
	
	private void checkVarDCTOption() {
		if (modular == 1) throw new IllegalStateException("Attempting to set a VarDCT-only option in Modular mode");
	}
	
}
