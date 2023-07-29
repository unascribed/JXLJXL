package com.unascribed.jxljxl.panama;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class FunctionAddresses {
	public static final MemorySegment JxlThreadParallelRunner$ADDR = lookupFunction("JxlThreadParallelRunner");

	static MemorySegment lookupFunction(String name) {
		return RuntimeHelper.lookupGlobalVariable(name, ValueLayout.ADDRESS);
	}
}
