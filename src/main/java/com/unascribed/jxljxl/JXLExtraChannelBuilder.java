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

import java.lang.foreign.MemorySegment;

import com.unascribed.jxljxl.JXLFrameBuilder.JXLBlendMode;
import com.unascribed.jxljxl.JXLFrameBuilder.JXLByteOrder;
import com.unascribed.jxljxl.JXLFrameBuilder.JXLFrameData;

public class JXLExtraChannelBuilder {
	
	public enum JXLExtraChannelType {
		@Deprecated /** @deprecated Should not be used directly @see {@link JXLExtraChannelBuilder#typeAlpha} */
		ALPHA,
		DEPTH,
		@Deprecated /** @deprecated Should not be used directly @see {@link JXLExtraChannelBuilder#typeSpotColor} */
		SPOT_COLOR,
		SELECTION_MASK,
		BLACK,
		@Deprecated /** @deprecated Should not be used directly @see {@link JXLExtraChannelBuilder#typeCfa} */
		CFA,
		THERMAL,
		@Deprecated RESERVED0, @Deprecated RESERVED1, @Deprecated RESERVED2, @Deprecated RESERVED3,
		@Deprecated RESERVED4, @Deprecated RESERVED5, @Deprecated RESERVED6, @Deprecated RESERVED7,
		UNKNOWN,
		OPTIONAL,
		;
		
		public int toNative() {
			return switch (this) {
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
			};
		}
		
		public static JXLExtraChannelType fromNative(int i) {
			if (i == JXL_CHANNEL_ALPHA()) return ALPHA;
			if (i == JXL_CHANNEL_BLACK()) return BLACK;
			if (i == JXL_CHANNEL_CFA()) return CFA;
			if (i == JXL_CHANNEL_DEPTH()) return DEPTH;
			if (i == JXL_CHANNEL_OPTIONAL()) return OPTIONAL;
			if (i == JXL_CHANNEL_RESERVED0()) return RESERVED0;
			if (i == JXL_CHANNEL_RESERVED1()) return RESERVED1;
			if (i == JXL_CHANNEL_RESERVED2()) return RESERVED2;
			if (i == JXL_CHANNEL_RESERVED3()) return RESERVED3;
			if (i == JXL_CHANNEL_RESERVED4()) return RESERVED4;
			if (i == JXL_CHANNEL_RESERVED5()) return RESERVED5;
			if (i == JXL_CHANNEL_RESERVED6()) return RESERVED6;
			if (i == JXL_CHANNEL_RESERVED7()) return RESERVED7;
			if (i == JXL_CHANNEL_SELECTION_MASK()) return SELECTION_MASK;
			if (i == JXL_CHANNEL_SPOT_COLOR()) return SPOT_COLOR;
			if (i == JXL_CHANNEL_THERMAL()) return THERMAL;
			return UNKNOWN;
		}
	}
	
	private final JXLFrameBuilder owner;
	
	JXLExtraChannelType type = JXLExtraChannelType.UNKNOWN;
	int bitsPerSample = -1;
	int exponentBitsPerSample = 0;
	int dimShift = -1;
	String name = null;
	boolean alphaPremultiplied = false;
	float[] spotColor = null;
	int cfaChannel = -1;
	
	// JxlBlendInfo
	JXLBlendMode blendMode = JXLBlendMode.REPLACE;
	int source;
	int alpha;
	boolean clamp = false;
	
	JXLExtraChannelBuilder name(String name) {
		this.name = name;
		return this;
	}
	
	/**
	 * Set the generic type of this extra channel. Some types require extra arguments, and must be
	 * configured through other methods.
	 * @see #typeAlpha
	 * @see #typeSpotColor
	 * @see #typeCfa
	 */
	public JXLExtraChannelBuilder type(JXLExtraChannelType type) {
		this.type = type;
		return this;
	}
	
	/**
	 * Set the type of this extra channel to ALPHA, and configure whether or not it is
	 * premultiplied.
	 */
	public JXLExtraChannelBuilder typeAlpha(boolean premultiplied) {
		this.type = JXLExtraChannelType.ALPHA;
		this.alphaPremultiplied = premultiplied;
		return this;
	}
	
	/**
	 * Set the type of this extra channel to SPOT_COLOR, and configure the spot color.
	 */
	public JXLExtraChannelBuilder typeSpotColor(float r, float g, float b, float a) {
		this.type = JXLExtraChannelType.SPOT_COLOR;
		this.spotColor = new float[] {r, g, b, a};
		return this;
	}
	
	/**
	 * Set the type of this extra channel to CFA, and configure the CFA channel. libjxl does not
	 * document what this is for.
	 */
	// TODO update above doc if upstream resolves their own todo
	public JXLExtraChannelBuilder typeCfa(int cfaChannel) {
		this.type = JXLExtraChannelType.CFA;
		this.cfaChannel = cfaChannel;
		return this;
	}
	
	/**
	 * Total bits per sample for this channel.
	 */
	public JXLExtraChannelBuilder bitsPerSample(int bitsPerSample) {
		this.bitsPerSample = bitsPerSample;
		return this;
	}
	
	/**
	 * Floating point exponent bits per channel, or 0 if they are unsigned integer.
	 */
	public JXLExtraChannelBuilder withFloatingSamples(int exponentBits) {
		if (exponentBits == 0) throw new IllegalArgumentException("exponentBits must not be 0");
		this.exponentBitsPerSample = exponentBits;
		return this;
	}
	
	/**
	 * The exponent the channel is downsampled by on each axis. libjxl does not document this any
	 * further.
	 */
	// TODO update above doc if upstream resolves their own todo
	public JXLExtraChannelBuilder dimShift(int dimShift) {
		this.dimShift = dimShift;
		return this;
	}

	/**
	 * Set the blend mode for this extra channel, which affects how it is composited with other
	 * frames when coalescing is enabled in the decoder.
	 */
	public JXLExtraChannelBuilder blendMode(JXLBlendMode blendMode) {
		this.blendMode = blendMode;
		return this;
	}

	/**
	 * Reference frame ID to use as the "bottom" layer (0-3).
	 */
	public JXLExtraChannelBuilder blendSource(int source) {
		this.source = source;
		return this;
	}

	/**
	 * Which extra channel to use as the "alpha" channel for blend modes BLEND and MULADD.
	 */
	public JXLExtraChannelBuilder blendAlpha(int alpha) {
		this.alpha = alpha;
		return this;
	}

	/**
	 * Clamp values to [0,1] for the purpose of blending.
	 */
	public JXLExtraChannelBuilder blendClamp(boolean clamp) {
		this.clamp = clamp;
		return this;
	}
	
	JXLExtraChannelBuilder(JXLFrameBuilder owner) {
		this.owner = owner;
	}

	/**
	 * Commit this extra channel to the frame with the given data.
	 */
	public JXLFrameBuilder commit(JXLDataType dataType, JXLByteOrder endianness, int align, int[] data) {
		return commit(dataType, endianness, align, owner.swapped(data));
	}

	/**
	 * Commit this extra channel to the frame with the given data.
	 */
	public JXLFrameBuilder commit(JXLDataType dataType, JXLByteOrder endianness, int align, short[] data) {
		return commit(dataType, endianness, align, MemorySegment.ofArray(data));
	}

	/**
	 * Commit this extra channel to the frame with the given data.
	 */
	public JXLFrameBuilder commit(JXLDataType dataType, JXLByteOrder endianness, int align, byte[] data) {
		return commit(dataType, endianness, align, MemorySegment.ofArray(data));
	}

	/**
	 * Commit this extra channel to the frame with the given data.
	 */
	public JXLFrameBuilder commit(JXLDataType dataType, JXLByteOrder endianness, int align, MemorySegment data) {
		if (bitsPerSample == -1) throw new IllegalArgumentException("bitsPerSample must be set");
		int index = owner.extraChannels.size();
		if (index >= owner.owner.numExtraChannels)
			throw new IllegalArgumentException("Extra channel index "+index+" is too large for this JXLEncoder");
		owner.extraChannels.add(this);
		owner.checkDataSize(1, dataType, align, data);
		
		owner.extraChannelData.add(new JXLFrameData(0, dataType, endianness, align, owner.ensureNative(data)));
		return owner;
	}
	
}
