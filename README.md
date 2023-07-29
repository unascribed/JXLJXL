<img src="logo.svg" align="right" width="180px" height="180px">

# JXLJXL
**Warning**: JXLJXL is not complete. Currently, it only offers an encode API and has not been
thoroughly tested. It has encoded multiple files successfully, and v0.1 of it powers the JXL support
in [Visage](https://visage.surgeplay.com).

*The GitHub copy of this repository is a mirror. The authoritative copy is hosted on [Forgejo](https://git.sleeping.town/unascribed/JXLJXL)*

JXLJXL (jjxl or jxl^2 for short, the **J**ava **X**treme **L**ibrary for **J**PEG-**XL**) is a Java
library for low-level access to the libjxl encode/decode APIs. JXLJXL allows you to create JXL
files will all sorts of interesting topologies, with full support for multi-frame files, custom
boxes, extra channels, and names for both frames and channels.

The API is designed to feel nice to use within Java, having a fluent interface that resembles the
builder pattern. This design allows it to flex all the way from simple one-shot encoding of regular
files to building all manner of complex payloads.

<!--The decode API is similarly capable of returning individual frames and returning extra channels, but
can just as easily also be used to decode bog-standard fully-composited RGBA images.-->

The heart of the library is jxlpanama, a simple binding to libjxl via Project Panama, generated with
jextract. These bindings are… thorny, to say the least. Using a C adaptation of a C++ API from Java
isn't pretty. JXLJXL paves over the raw bindings, making advanced JXL use cases a breeze.

JXLJXL is designed for advanced use cases. If you don't need to handle encoding and have very simple
needs for decoding, the [jxlatte](https://github.com/Traneptora/jxlatte) project is a pure Java
implementation of a simple JXL decoder that I recommend using instead.

## Usage

JXLJXL is a modern Java 20 library that uses a preview feature — Project Panama, specifically the
Foreign Linker API, a modern and fast replacement for JNI. So, while including the dependency is as
easy as usual:

```gradle
repositories {
	maven {
		url 'https://repo.sleeping.town'
		content.includeGroup 'com.unascribed'
	}
}

dependencies {
	implementation 'com.unascribed:jxljxl:0.2'
}
```

…you will also need to add these options to your Java invocation, in order to enable the Panama API
and give JXLJXL permission to invoke native code:

`--enable-preview --enable-native-access=com.unascribed.jxljxl`

JXLJXL does not currently bundle a copy of libjxl — that needs to be available on the target system.
Bundled libraries will be added later.

## API

The primary entrypoint to the encoding API is `JXLEncoder`, which can be used something like so:

```java
JXLEncoder.create()
	.size(width, height)
	.bitsPerSample(8)
	.numChannels(3, 1)
	.colorEncoding(JXLPredefinedColorEncoding.SRGB)
	.newFrame()
		.distance(1.5f)
		.withModularEncoding()
			.lossyPalette(true)
			.done()
		.brotliEffort(3)
		.effort(1)
		.keepInvisible(false)
		.commit(bufferedImage)
	.encodeToStream(out);
```

All the documentation from libjxl, plus some extras, are included as javadocs in the library. Take
a look around!

All operations are buffered in memory until the `encode` call is made, so the order isn't
particularly important. JXLJXL handles calling everything in the right order and making sense of the
mess.
