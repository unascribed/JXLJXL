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

/**
 * Specifies a custom color encoding.
 */
public record JXLCustomColorEncoding(JXLColorSpace colorSpace, JXLWhitePoint whitePoint, JXLPrimaries primaries, JXLTransferFunction transferFunction, JXLRenderingIntent intent)
	implements JXLColorEncoding {

	/**
	 * Whitepoints for color encoding. When decoding, the numerical xy whitepoint value can be read
	 * from the JXLColorEncoding white_point field regardless of the enum value. When encoding, enum
	 * values except JXL_WHITE_POINT_CUSTOM override the numerical fields. Some enum values match a
	 * subset of CICP (Rec. ITU-T H.273 | ISO/IEC 23091-2:2019(E)), however the white point and RGB
	 * primaries are separate enums here.
	 */
	public record JXLWhitePoint(double x, double y) {
		// these values are not actually used. delegates to the predefined values in libjxl
		// filled in just in case a library user tries to read them
		/** CIE Standard Illuminant D65: 0.3127, 0.3290  */
		public static final JXLWhitePoint D65 = new JXLWhitePoint(0.3127, 0.3290);
		/** CIE Standard Illuminant E (equal-energy): 1/3, 1/3 */
		public static final JXLWhitePoint E = new JXLWhitePoint(1/3D, 1/3D);
		/** DCI-P3 from SMPTE RP 431-2: 0.314, 0.351 */
		public static final JXLWhitePoint DCI = new JXLWhitePoint(0.314, 0.351);
	}
	
	/**
	 * Primaries for color encoding. When decoding, the primaries can be read from the
	 * JxlColorEncoding primaries_red_xy, primaries_green_xy and primaries_blue_xy fields regardless
	 * of the enum value. When encoding, the enum values except JXL_PRIMARIES_CUSTOM override the
	 * numerical fields. Some enum values match a subset of CICP (Rec. ITU-T H.273 | ISO/IEC
	 * 23091-2:2019(E)), however the white point and RGB primaries are separate enums here.
	 */
	public record JXLPrimaries(double redX, double redY, double greenX, double greenY, double blueX, double blueY) {
		// these values are not actually used. delegates to the predefined values in libjxl
		/** The CIE xy values of the red, green and blue primaries are: 0.639998686, 0.330010138; 0.300003784, 0.600003357; 0.150002046, 0.059997204  */
		public static final JXLPrimaries SRGB = new JXLPrimaries(0.639998686, 0.330010138, 0.300003784, 0.600003357, 0.150002046, 0.059997204);
		/** As specified in Rec. ITU-R BT.2100-1 */
		public static final JXLPrimaries BT2100 = new JXLPrimaries(0.708, 0.292, 0.170, 0.797, 0.131, 0.046);
		/** As specified in SMPTE RP 431-2 */
		public static final JXLPrimaries P3 = new JXLPrimaries(0.680, 0.320, 0.265, 0.690, 0.150, 0.060);
	}

	/**
	 * @see JXLPredefinedTransferFunction
	 * @see JXLGammaTransferFunction
	 */
	public sealed interface JXLTransferFunction permits JXLPredefinedTransferFunction, JXLGammaTransferFunction, JXLUnknownTransferFunction {
		/**
		 * None of the other options describe the transfer function.
		 */
		JXLTransferFunction UNKNOWN = JXLUnknownTransferFunction.INSTANCE;
	}
	
	private enum JXLUnknownTransferFunction implements JXLTransferFunction {
		INSTANCE;
	}
	
	public enum JXLPredefinedTransferFunction implements JXLTransferFunction {
		/** As specified in SMPTE RP 431-2 */
		SMPTE_709,
		/** The gamma exponent is 1 */
		LINEAR,
		/** As specified in IEC 61966-2-1 sRGB */
		SRGB,
		/** As specified in SMPTE ST 2084 */
		PQ,
		/** As specified in SMPTE ST 428-1 */
		DCI,
		/** As specified in Rec. ITU-R BT.2100-1 (HLG) */
		HLG,
	}
	
	/**
	 * Transfer function follows power law given by the gamma value. Not a CICP value.
	 */
	public record JXLGammaTransferFunction(double gamma) implements JXLTransferFunction {}
	
	public enum JXLRenderingIntent {
		/** vendor-specific */
		PERCEPTUAL,
		/** media-relative */
		RELATIVE,
		/** vendor-specific */
		SATURATION,
		/** ICC-absolute */
		ABSOLUTE,
	}
}
