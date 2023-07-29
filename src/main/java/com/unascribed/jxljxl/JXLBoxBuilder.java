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

import java.nio.charset.StandardCharsets;

public class JXLBoxBuilder {

	public enum JXLWellKnownBoxType {
		/**
		 * A box with EXIF metadata, can be added by libjxl users, or is automatically added when
		 * needed for JPEG reconstruction. The contents of this box must be prepended by a 4-byte
		 * tiff header offset, which may be 4 zero bytes in case the tiff header follows
		 * immediately. The EXIF metadata must be in sync with what is encoded in the JPEG XL
		 * codestream, specifically the image orientation. While this is not recommended in
		 * practice, in case of conflicting metadata, the JPEG XL codestream takes precedence.
		 */
		EXIF("Exif"),
		/**
		 * A box with XML data, in particular XMP metadata, can be added by libjxl users, or is
		 * automatically added when needed for JPEG reconstruction
		 */
		XML("xml "),
		/**
		 * A JUMBF superbox, which can contain boxes with different types of metadata inside. This
		 * box type can be added by the encoder transparently, and other libraries to create and
		 * handle JUMBF content exist.
		 */
		JUMBF("jumb"),
		/**
		 * A box that encapsulates another box that has been compressed with Brotli. Automatically
		 * emitted by the encoder if compression is requested, and transparently unwrapped by the
		 * decoder.
		 */
		BROTLI("brob"),
		
		@Deprecated /** @deprecated Not permitted for use by application code. */
		FILETYPE("ftyp"),
		@Deprecated /** @deprecated Not permitted for use by application code. */
		JXL_SIGNATURE("JXL "),
		@Deprecated /** @deprecated Not permitted for use by application code. */
		JXL_CODESTREAM("jxlc"),
		@Deprecated /** @deprecated Not permitted for use by application code. */
		JXL_LEVEL("jxll"),
		@Deprecated /** @deprecated Not permitted for use by application code. */
		JXL_FRAMEINDEX("jxli"),
		@Deprecated /** @deprecated Not permitted for use by application code. */
		JXL_PARTIALCODESTREAM("jxlp"),
		;
		final byte[] fourcc;

		JXLWellKnownBoxType(String fourcc) {
			this.fourcc = fourcc.getBytes(StandardCharsets.ISO_8859_1);
		}
		
	}
	
	private final JXLEncoder owner;
	
	byte[] type;
	boolean compressed;
	
	byte[] contents;
	
	JXLBoxBuilder(JXLEncoder owner) {
		this.owner = owner;
	}
	
	JXLBoxBuilder type(String fourcc) {
		byte[] bys = fourcc.getBytes(StandardCharsets.ISO_8859_1);
		if (bys.length != 4) throw new IllegalArgumentException(fourcc+" is not a valid FourCC");
		this.type = bys;
		return this;
	}
	
	JXLBoxBuilder type(JXLWellKnownBoxType type) {
		this.type = type.fourcc;
		return this;
	}
	
	/**
	 * Request that this box be compressed with Brotli.
	 */
	public JXLBoxBuilder compressed() {
		this.compressed = true;
		return this;
	}
	
	/**
	 * Fill this box with data from the given byte array and commit it to its parent encoder,
	 * returning the parent to allow continuing to build the JXL file.
	 */
	public JXLEncoder commit(byte[] contents) {
		this.contents = contents;
		return commit();
	}
	
	private JXLEncoder commit() {
		owner.boxes.add(this);
		return owner;
	}
	
}
