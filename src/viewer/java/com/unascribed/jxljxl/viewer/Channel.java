package com.unascribed.jxljxl.viewer;

public record Channel(String name, int idx, boolean rgb) {

	@Override
	public String toString() {
		return name;
	}
	
}
