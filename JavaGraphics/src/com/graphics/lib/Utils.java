package com.graphics.lib;

import java.util.List;

import com.graphics.lib.interfaces.ILightIntensityFinder;
import com.graphics.lib.interfaces.IVertexNormalFinder;
import com.graphics.lib.interfaces.IZBuffer;
import com.graphics.lib.lightsource.LightSource;


public class Utils {
	//stuff in here should be separated to own factories

	public static enum VertexNormalFinderEnum{
		DEFAULT, CENTRE_TO_POINT
	}
	
	public static enum LightIntensityFinderEnum{
		DEFAULT
	}

	public static IZBuffer getDefaultZBuffer()
	{
		return new ZBuffer();
	}
	
	public static ILightIntensityFinder getLightIntensityFinder(List<LightSource> ls, LightIntensityFinderEnum type)
	{
		switch(type){
			case DEFAULT: return getDefaultLightIntensityFinder(ls);
			default: return getDefaultLightIntensityFinder(ls);
		}
	}
	
	private static ILightIntensityFinder getDefaultLightIntensityFinder(List<LightSource> ls)
	{
		return (obj, p, bf) -> {
			IntensityComponents maxIntensity = new IntensityComponents();
			
			ls.stream().filter(l -> l.isOn()).forEach(l ->
			{
				double percent = 0;
				Vector lightVector = l.getPosition().vectorToPoint(p).getUnitVector();
				
				double answer = p.getNormal().dotProduct(lightVector);
				
				double deg = Math.toDegrees(Math.acos(answer));
				if (deg > 90 && !bf)
				{			
					percent = (deg-90) / 90;
				}
				else if (bf)
				{
					//light on the rear of the facet for if we are processing backfaces - which implies the rear may be visible
					percent = (90-deg) / 90;
				}
				
				double intensity = l.getIntensityComponents(p).getRed() * percent;
				if (intensity > maxIntensity.getRed()) maxIntensity.setRed(intensity);
				intensity = l.getIntensityComponents(p).getGreen() * percent;
				if (intensity > maxIntensity.getGreen()) maxIntensity.setGreen(intensity);
				intensity = l.getIntensityComponents(p).getBlue() * percent;
				if (intensity > maxIntensity.getBlue()) maxIntensity.setBlue(intensity);
			});
			return maxIntensity;
		};
	}
	
	public static IVertexNormalFinder getVertexNormalFinder(VertexNormalFinderEnum type)
	{
		switch(type){
			case DEFAULT: return getDefaultVertexNormalFinder();
			case CENTRE_TO_POINT: return getCentreToPointVertexNormalFinder();
			default: return getDefaultVertexNormalFinder();
		}
	}
	
	
	private static IVertexNormalFinder getDefaultVertexNormalFinder()
	{
		return (obj, p, f) -> {
			if (obj.getVertexFacetMap() != null){
				List<Facet> facetList = obj.getVertexFacetMap().get(p);
				if (facetList != null && facetList.size() > 0)
				{	
					Vector normal = new Vector(0,0,0);
					for(Facet facet : facetList){
						normal.addVector(facet.getNormal());
					}
					return normal.getUnitVector(); //could possibly store for reuse between transforms
				}
			}
			
			return f.getNormal();
		};
	}
	
	private static IVertexNormalFinder getCentreToPointVertexNormalFinder()
	{
		return (obj, p, f) -> {
			Point centre = obj.getCentre();
			return new Vector(p.x - centre.x, p.y - centre.y, p.z - centre.z).getUnitVector();
		};
	}
}
