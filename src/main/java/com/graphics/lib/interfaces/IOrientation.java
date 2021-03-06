package com.graphics.lib.interfaces;

import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;

public interface IOrientation {
	public Vector getForward();
	public Vector getUp();
	public Vector getRight();
	public Vector getBack();
	public Vector getDown();
	public Vector getLeft();
	public Point getAnchor();
	//public void setAnchor(Point p);
	public ICanvasObject getRepresentation();
	public IOrientation copy();
	public Facet getPlane();
}
