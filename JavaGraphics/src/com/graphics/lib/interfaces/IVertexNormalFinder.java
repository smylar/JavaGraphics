package com.graphics.lib.interfaces;

import com.graphics.lib.CanvasObject;
import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;

@FunctionalInterface
public interface IVertexNormalFinder {
	public Vector getVertexNormal(CanvasObject obj, Point p, Facet f);
}
