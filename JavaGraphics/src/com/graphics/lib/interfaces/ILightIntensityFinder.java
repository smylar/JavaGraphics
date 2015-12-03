package com.graphics.lib.interfaces;

import com.graphics.lib.CanvasObject;
import com.graphics.lib.IntensityComponents;
import com.graphics.lib.Point;

@FunctionalInterface
public interface ILightIntensityFinder {
	public IntensityComponents getLightIntensity(CanvasObject obj, Point p, boolean isPartOfBacface);
}
