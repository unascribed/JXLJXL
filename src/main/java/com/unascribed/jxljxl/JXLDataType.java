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

public enum JXLDataType {
	/**
	 * Use 32-bit single-precision floating point values, with range 0.0-1.0 (within gamut, may go
	 * outside this range for wide color gamut). Floating point output, either {@link #FLOAT32} or
	 * {@link #FLOAT16}, is recommended for HDR and wide gamut images when color profile conversion
	 * is required.
	 */
	FLOAT32(4),
	/**
	 * Use type uint8_t (byte in Java). May clip wide color gamut data.
	 */
	UINT8(1),
	/**
	 * Use type uint16_t (short in Java). May clip wide color gamut data.
	 */
	UINT16(2),
	/**
	 * Use 16-bit IEEE 754 half-precision floating point values
	 */
	FLOAT16(2),
	;
	public final int bytes;

	JXLDataType(int bytes) {
		this.bytes = bytes;
	}
}
