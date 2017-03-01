package com.graphics.lib;

import com.graphics.lib.interfaces.ILightIntensityFinder;

public enum LightIntensityFinderEnum {
	DEFAULT((ls, obj, p, v, bf) -> {
			IntensityComponents maxIntensity = new IntensityComponents();
			
			ls.stream().filter(l -> l.isOn()).forEach(l ->
			{
				IntensityComponents intComps = l.getIntensityComponents(p);
				if (intComps.hasNoIntensity()) return;
				
				double percent = 0;
				Vector lightVector = l.getPosition().vectorToPoint(p).getUnitVector();
				
				double answer = v.dotProduct(lightVector);
				
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
				
				double intensity = intComps.getRed() * percent;
				if (intensity > maxIntensity.getRed()) maxIntensity.setRed(intensity);
				intensity = intComps.getGreen() * percent;
				if (intensity > maxIntensity.getGreen()) maxIntensity.setGreen(intensity);
				intensity = intComps.getBlue() * percent;
				if (intensity > maxIntensity.getBlue()) maxIntensity.setBlue(intensity);
			});
			return maxIntensity;
	});
	
	private ILightIntensityFinder finder;
	
	private LightIntensityFinderEnum(ILightIntensityFinder finder) {
		this.finder = finder;
	}
	
	public ILightIntensityFinder get() {
		return this.finder;
	}
}
