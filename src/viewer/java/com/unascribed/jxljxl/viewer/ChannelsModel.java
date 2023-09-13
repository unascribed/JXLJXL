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

package com.unascribed.jxljxl.viewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import com.unascribed.jxljxl.viewer.JXLJXLViewer.ExtraChannel;

public class ChannelsModel implements ListModel<Channel> {

	private static final Channel[] BUILTIN = {
		new Channel("[Composite]", -1, true),
		new Channel("Red", 0, true),
		new Channel("Green", 0, true),
		new Channel("Blue", 0, true)
	};
	
	private final List<ExtraChannel> extra;
	
	public ChannelsModel(List<ExtraChannel> extra) {
		this.extra = new ArrayList<>(extra);
	}

	@Override
	public int getSize() {
		return extra.size()+4;
	}

	@Override
	public Channel getElementAt(int i) {
		if (i < BUILTIN.length) return BUILTIN[i];
		i -= BUILTIN.length;
		var ex = extra.get(i);
		var typeName = formatTitleCase(ex.type().name());
		return new Channel(ex.name() == null ? typeName : "\""+ex.name()+"\" ("+typeName+")", i, false);
	}

	@Override
	public void addListDataListener(ListDataListener l) {

	}

	@Override
	public void removeListDataListener(ListDataListener l) {

	}
	
	private static String formatTitleCase(String in) {
		String[] pieces = new String[] { in };
		if (in.contains(" ")) {
			pieces = in.toLowerCase().split(" ");
		} else if (in.contains("_")) {
			pieces = in.toLowerCase().split("_");
		}

		StringBuilder result = new StringBuilder();
		for (String s : pieces) {
			if (s == null)
				continue;
			String t = s.trim().toLowerCase(Locale.ROOT);
			if (t.isEmpty())
				continue;
			result.append(Character.toUpperCase(t.charAt(0)));
			if (t.length() > 1)
				result.append(t.substring(1));
		}
		return result.toString();
	}

}
