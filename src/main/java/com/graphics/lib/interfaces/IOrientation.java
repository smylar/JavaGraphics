package com.graphics.lib.interfaces;

import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;

public interface IOrientation {
	Vector getForward();
	Vector getUp();
	Vector getRight();
	Vector getBack();
	Vector getDown();
	Vector getLeft();
	Point getAnchor();
	//public void setAnchor(Point p);
	ICanvasObject getRepresentation();
	IOrientation copy();
	Facet getPlane();
}
