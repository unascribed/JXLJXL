package com.unascribed.jxljxl.viewer;

import java.util.function.Consumer;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

import com.unascribed.jxljxl.viewer.MenuBuilder.ChildMenuBuilder;

public class MenuBuilder extends BaseMenuBuilder<MenuBuilder> {

	public static class ChildMenuBuilder<P> extends BaseMenuBuilder<ChildMenuBuilder<P>> {
		
		private final P parent;
		public ChildMenuBuilder(P owner) {
			this.parent = owner;
		}

		public P done() {
			return parent;
		}
	}

	public JMenu build() {
		return menu;
	}
	
	public JMenuBar buildAsMenuBar() {
		var bar = new JMenuBar();
		bar.setFont(JXLJXLViewer.font);
		int c = menu.getMenuComponentCount();
		for (int i = 0; i < c; i++) {
			var it = menu.getMenuComponent(0); // adding the menus to the bar removes them from their current parent
			if (it instanceof JMenu jm) {
				bar.add(jm);
			} else {
				bar.add(it);
			}
		}
		return bar;
	}
	
}

class BaseMenuBuilder<T extends BaseMenuBuilder<T>> {
	
	protected final JMenu menu = new JMenu(); {
		menu.setFont(JXLJXLViewer.font);
		menu.setBorder(padding());
	}

	private EmptyBorder padding() {
		return new EmptyBorder(4, 8, 4, 8);
	}
	
	public ChildMenuBuilder<T> menu(String name) {
		var bldr = new ChildMenuBuilder(this);
		menu.add(bldr.menu);
		bldr.menu.setText(name);
		return bldr;
	}
	
	public T item(String text, Runnable callback) {
		return item(text, null, callback);
	}
	
	public T item(String text, KeyStroke accel, Runnable callback) {
		var item = new JMenuItem(text);
		if (accel != null) item.setAccelerator(accel);
		item.setBorder(padding());
		item.addActionListener((e) -> callback.run());
		item.setFont(JXLJXLViewer.font);
		menu.add(item);
		return (T)this;
	}
	
	public T toggleItem(String text, Consumer<Boolean> callback) {
		return toggleItem(text, null, callback);
	}
	
	public T toggleItem(String text, KeyStroke accel, Consumer<Boolean> callback) {
		var item = new JCheckBoxMenuItem(text);
		if (accel != null) item.setAccelerator(accel);
		item.setBorder(padding());
		item.addActionListener((e) -> callback.accept(item.isSelected()));
		item.setFont(JXLJXLViewer.font);
		menu.add(item);
		return (T)this;
	}
	
	public T separator() {
		menu.addSeparator();
		return (T)this;
	}
	
}
