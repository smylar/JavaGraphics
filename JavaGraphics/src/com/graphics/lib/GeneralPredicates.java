package com.graphics.lib;

import java.util.function.Predicate;

import com.graphics.lib.camera.Camera;
import com.graphics.lib.lightsource.DirectionalLightSource;
import com.graphics.lib.lightsource.LightSource;

public class GeneralPredicates {
	public static Predicate<Facet> isFrontface(Camera c)
	{
		return f -> {
			
			if (f.point1.getTransformed(c).z < 1 && f.point2.getTransformed(c).z < 1 && f.point3.getTransformed(c).z < 1) return false;
			
			double camVecZ = c.getOrientation().getForward().z;
			double facetVecZ = f.getTransformedNormal(c).z;
			
			if (camVecZ < 0) camVecZ = camVecZ * -1;
			
			return (camVecZ * facetVecZ) < 0;
		};
	}
	
	public static Predicate<Facet> isFrontface(LightSource l)
	{
		return f -> {		
			Vector lightVector = l.getPosition().vectorToPoint(f.point1).getUnitVector();
			
			if (l instanceof DirectionalLightSource){
				//N.B. this will result in whole facet being declared out of the light even if just point1 is not in the light
				double angleRad = ((DirectionalLightSource) l).getDirection().dotProduct(lightVector);
				if (Math.toDegrees(Math.acos(angleRad)) > ((DirectionalLightSource) l).getLightConeAngle()) return false;
			}
			
			double answer = f.getNormal().dotProduct(lightVector);
			
			double deg = Math.toDegrees(Math.acos(answer));
			
			return (deg > 90);
		};
	}
	
	public static Predicate<Facet> isOverHorizon(Camera c, double horizon)
	{
		 return f -> c.getPosition().distanceTo(f.point1) > horizon
		 && c.getPosition().distanceTo(f.point2) > horizon
		 && c.getPosition().distanceTo(f.point3) > horizon;
	}
}