package com.graphics.lib;

import java.awt.Dimension;
import java.util.List;
import java.util.function.Predicate;

import com.graphics.lib.camera.Camera;
import com.graphics.lib.lightsource.ILightSource;

public class GeneralPredicates {
    
    private GeneralPredicates() {}
    
	public static Predicate<Facet> isFrontface(Camera c)
	{
		return f -> {
			List<WorldCoord> points = f.getAsList();
			if (points.get(0).getTransformed(c).z < 1 && points.get(1).getTransformed(c).z < 1 && points.get(2).getTransformed(c).z < 1) return false;
			
			double camVecZ = c.getOrientation().getForward().getZ();
			double facetVecZ = f.getTransformedNormal(c).getZ();
			
			camVecZ = Math.abs(camVecZ);
			
			return (camVecZ * facetVecZ) < 0;
		};
	}
	
	public static Predicate<Facet> isLit(ILightSource l)
	{
		return f -> {		
			List<WorldCoord> points = f.getAsList();
			
			if (points.stream().allMatch(p -> l.getIntensityComponents(p).hasNoIntensity())) return false; 
			
			Vector lightVector = l.getPosition().vectorToPoint(points.get(0)).getUnitVector();
			
			return f.getNormal().dotProduct(lightVector) < 0;
		};
	}
	
	public static Predicate<Facet> isFrontface(Point p)
	{
		return f -> {	
			List<WorldCoord> points = f.getAsList();
			Vector vector = p.vectorToPoint(points.get(0)).getUnitVector();
			
			return f.getNormal().dotProduct(vector) < 0;
		};
	}
	
	public static Predicate<Facet> isOverHorizon(Camera c, double horizon)
	{
		 return f -> {
			 List<WorldCoord> points = f.getAsList();
			 
			 return points.stream().allMatch(p -> p.distanceTo(c.getPosition()) > horizon);
		};
	}
	
	public static Predicate<WorldCoord> untagged()
	{
		return w -> !w.hasTags();
	}
	
	public static Predicate<Point> isOnScreen(final Dimension screen) {
        return p -> p.z > 1 &&
                    p.x > 0 && p.x < screen.getWidth() &&
                    p.y > 0 && p.y < screen.getHeight();
     }
}