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
