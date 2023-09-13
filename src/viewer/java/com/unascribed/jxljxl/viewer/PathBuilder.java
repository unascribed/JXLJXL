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

import java.awt.geom.Path2D;

public class PathBuilder {

	private final Path2D.Float path = new Path2D.Float();

	private double x() {
		if (path.getCurrentPoint() == null) return 0;
		return path.getCurrentPoint().getX();
	}
	
	private double y() {
		if (path.getCurrentPoint() == null) return 0;
		return path.getCurrentPoint().getY();
	}

	
	public PathBuilder M(double x, double y) {
		path.moveTo(x, y);
		return this;
	}
	public PathBuilder m(double dx, double dy) {
		path.moveTo(x()+dx, y()+dy);
		return this;
	}
	
	public PathBuilder L(double x, double y) {
		path.lineTo(x, y);
		return this;
	}
	public PathBuilder l(double dx, double dy) {
		path.lineTo(x()+dx, y()+dy);
		return this;
	}

	public PathBuilder H(double x) {
		path.lineTo(x, y());
		return this;
	}
	public PathBuilder h(double dx) {
		path.lineTo(x()+dx, y());
		return this;
	}

	public PathBuilder V(double y) {
		path.lineTo(x(), y);
		return this;
	}
	public PathBuilder v(double dy) {
		path.lineTo(x(), y()+dy);
		return this;
	}

	public PathBuilder C(double x1, double y1, double x2, double y2, double x, double y) {
		path.curveTo(x1, y1, x2, y2, x, y);
		return this;
	}
	public PathBuilder c(double dx1, double dy1, double dx2, double dy2, double dx, double dy) {
		path.curveTo(x()+dx1, y()+dy1, x()+dx2, y()+dy2, x()+dx, y()+dy);
		return this;
	}

	public PathBuilder Z() {
		path.closePath();
		return this;
	}
	public PathBuilder z() {
		path.closePath();
		return this;
	}
	
	public Path2D.Float build() {
		return path;
	}
	
	
}
