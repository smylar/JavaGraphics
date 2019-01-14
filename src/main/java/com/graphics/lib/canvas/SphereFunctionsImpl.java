package com.graphics.lib.canvas;

import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.interfaces.ICanvasObject;

public class SphereFunctionsImpl extends CanvasObjectFunctionsImpl {
	
	
	@Override
	public boolean vectorIntersects(ICanvasObject obj, Point start, Vector v){
		return this.vectorIntersectsRoughly(obj, start, v);
	}
	
	@Override
	public boolean isPointInside(ICanvasObject obj, Point p)
	{
		double actualRadius = obj.getCentre().distanceTo(obj.getVertexList().get(0)); //may have changed in scaling
		return obj.getCentre().distanceTo(p) < actualRadius;
	}
}
