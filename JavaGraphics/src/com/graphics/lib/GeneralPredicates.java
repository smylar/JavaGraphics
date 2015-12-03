package com.graphics.lib;

import java.util.function.Predicate;

import com.graphics.lib.camera.Camera;

public class GeneralPredicates {
	public static Predicate<Facet> isFrontface(Camera c)
	{
		return f -> {
			
			if (f.point1.getTransformed().z < 1 && f.point2.getTransformed().z < 1 && f.point3.getTransformed().z < 1) return false;
			
			double camVecZ = c.getOrientation().getForward().z;
			double facetVecZ = f.getTransformedNormal().z;
			
			if (camVecZ < 0) camVecZ = camVecZ * -1;
			
			return (camVecZ * facetVecZ) < 0;
		};
	}
	
	public static Predicate<Facet> isOverHorizon(Camera c, double horizon)
	{
		 return f -> c.getPosition().distanceTo(f.point1) > horizon
		 && c.getPosition().distanceTo(f.point2) > horizon
		 && c.getPosition().distanceTo(f.point3) > horizon;
	}
}