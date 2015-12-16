package com.graphics.lib.interfaces;

import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.canvas.CanvasObject;

public interface IOrientation {
	public Vector getForward();
	public Vector getUp();
	public Vector getRight();
	public Vector getBack();
	public Vector getDown();
	public Vector getLeft();
	public Point getAnchor();
	//public void setAnchor(Point p);
	public CanvasObject getRepresentation();
	public IOrientation copy();
}
