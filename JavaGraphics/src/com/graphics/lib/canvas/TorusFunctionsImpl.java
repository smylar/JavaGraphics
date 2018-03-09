package com.graphics.lib.canvas;

import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import com.graphics.lib.Point;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.shapes.Torus;

/**
 * Functions for {@link Torus} shapes
 * 
 * @author paul.brandon
 *
 */
public class TorusFunctionsImpl extends CanvasObjectFunctionsImpl {
	@Override
	public boolean isPointInside(ICanvasObject obj, Point p)
	{	
		return obj.getObjectAs(Torus.class)
		          .map(t -> isPointInsideImpl(t, p))
		          .orElse(super.isPointInside(obj, p));
	}
	
	private boolean isPointInsideImpl(Torus t, Point p) {
	    
	    return Optional.of(t.getCentre().distanceTo(p))
        	            .filter(distFromCentre -> distFromCentre <= t.getActualRadius() && distFromCentre >= t.getActualHoleRadius())
        	            .map(distFromCentre -> Pair.of(distFromCentre, t.getHolePlane().getDistanceFromFacetPlane(p)))
        	            .filter(pair -> pair.getRight() <= t.getActualTubeRadius()) //check distance from hole plane is less than the tube radius
        	            .map(pair -> {
        	                double pX = Math.sqrt(Math.pow(pair.getLeft(), 2) - Math.pow(pair.getRight(), 2));
        	                double circleX = t.getActualRadius() - t.getActualTubeRadius();
        	                
        	                return Math.pow(pX - circleX, 2) + Math.pow(pair.getRight(), 2) < Math.pow(t.getActualTubeRadius(), 2);
        	            })
        	            .orElse(false);	    
	}
}
