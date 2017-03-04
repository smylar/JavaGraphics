package com.graphics.lib.canvas;

import com.graphics.lib.Point;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.shapes.Torus;

public class TorusFunctionsImpl extends CanvasObjectFunctionsImpl {
	@Override
	public boolean isPointInside(ICanvasObject obj, Point p)
	{	
		if (obj instanceof Torus) {
			Torus torus = (Torus)obj;
			double distFromCentre = torus.getCentre().distanceTo(p);
			if (distFromCentre > torus.getActualRadius() || torus.getCentre().distanceTo(p) < torus.getActualHoleRadius()) return false;
			
			double distFromHolePlane = torus.getHolePlane().getDistanceFromFacetPlane(p);
			if (distFromHolePlane > torus.getActualTubeRadius()) return false;
			
			//now just to check within the circular area of the tube
			double pX = Math.sqrt(Math.pow(distFromCentre, 2) - Math.pow(distFromHolePlane, 2));
			double circleX = torus.getActualRadius() - torus.getActualTubeRadius();
			//pY would be distFromHolePlane and circleY = 0
			
			return Math.pow(pX - circleX, 2) + Math.pow(distFromHolePlane, 2) < Math.pow(torus.getActualTubeRadius(), 2);
		}
		return super.isPointInside(obj, p);
	}
}
