package com.graphics.lib.interfaces;

import java.util.Collection;

import com.graphics.lib.IntensityComponents;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.lightsource.ILightSource;

@FunctionalInterface
public interface ILightIntensityFinder {
	public IntensityComponents getLightIntensity(Collection<ILightSource> ls, ICanvasObject obj, Point p, Vector normal, boolean isPartOfBacface);
}
