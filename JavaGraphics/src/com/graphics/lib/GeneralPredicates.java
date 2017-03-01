package com.graphics.lib;

import java.util.List;
import java.util.function.Predicate;

import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.lightsource.ILightSource;

public class GeneralPredicates {
	public static Predicate<Facet> isFrontface(Camera c)
	{
		return f -> {
			List<WorldCoord> points = f.getAsList();
			if (points.get(0).getTransformed(c).z < 1 && points.get(1).getTransformed(c).z < 1 && points.get(2).getTransformed(c).z < 1) return false;
			
			double camVecZ = c.getOrientation().getForward().z;
			double facetVecZ = f.getTransformedNormal(c).z;
			
			if (camVecZ < 0) camVecZ = camVecZ * -1;
			
			return (camVecZ * facetVecZ) < 0;
		};
	}
	
	public static Predicate<Facet> isLit(ILightSource l)
	{
		return f -> {		
			List<WorldCoord> points = f.getAsList();
			
			//IntensityComponents intComps = l.getIntensityComponents(points.get(0));
			//if (intComps.hasNoIntensity()) return false;
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
	
	public static Predicate<WorldCoord> untagged(ICanvasObject obj)
	{
		return w -> !w.hasTags() || (w.tagCount() == 1 && w.hasTag(obj.getObjectTag()));
	}
}