package com.graphics.lib;

import com.graphics.lib.interfaces.ILightIntensityFinder;
import com.graphics.lib.lightsource.ILightSource;

public enum LightIntensityFinderEnum {
	DEFAULT((ls, obj, p, v, f) -> {
			IntensityComponents maxIntensity = new IntensityComponents(f.getBaseIntensity());

			ls.stream().filter(ILightSource::isOn).forEach(l ->
			{
				IntensityComponents intComps = l.getIntensityComponents(p);
				if (intComps.hasNoIntensity()) return;
				
				double percent = 0;

				double deg = v.angleBetween(l.getPosition().vectorToPoint(p));
				
				if (deg > 90 && f.isFrontFace())
				{			
					percent = (deg-90) / 90;
				}
				else if (!f.isFrontFace())
				{
					//light on the rear of the facet for if we are processing backfaces - which implies the rear may be visible
					percent = (90-deg) / 90;
				}
				
				final double pc = percent;
				
				IntensityComponents.forEach(comp -> {
				    double intensity = intComps.get(comp) * pc;
	                if (intensity > maxIntensity.get(comp)) 
	                    maxIntensity.set(comp, intensity);
		        });
				
			});
			return f.checkIntensity(maxIntensity);
	});
	
	private final ILightIntensityFinder finder;
	
	LightIntensityFinderEnum(ILightIntensityFinder finder) {
		this.finder = finder;
	}
	
	public ILightIntensityFinder get() {
		return this.finder;
	}
}
