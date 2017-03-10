package com.graphics.lib.canvas;

import java.util.Optional;

import com.graphics.lib.Point;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.shapes.Torus;

public class TorusFunctionsImpl extends CanvasObjectFunctionsImpl {
	@Override
	public boolean isPointInside(ICanvasObject obj, Point p)
	{	
		Optional<Torus> torus = obj.getObjectAs(Torus.class);
		if(torus.isPresent()) {
			double distFromCentre = torus.get().getCentre().distanceTo(p);
			if (distFromCentre > torus.get().getActualRadius() || torus.get().getCentre().distanceTo(p) < torus.get().getActualHoleRadius()) return false;
			
			double distFromHolePlane = torus.get().getHolePlane().getDistanceFromFacetPlane(p);
			if (distFromHolePlane > torus.get().getActualTubeRadius()) return false;
			
			//now just to check within the circular area of the tube
			double pX = Math.sqrt(Math.pow(distFromCentre, 2) - Math.pow(distFromHolePlane, 2));
			double circleX = torus.get().getActualRadius() - torus.get().getActualTubeRadius();
			//pY would be distFromHolePlane and circleY = 0
			
			return Math.pow(pX - circleX, 2) + Math.pow(distFromHolePlane, 2) < Math.pow(torus.get().getActualTubeRadius(), 2);
		}
		return super.isPointInside(obj, p);
	}
}
