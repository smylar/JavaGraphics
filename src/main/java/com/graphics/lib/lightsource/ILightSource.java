package com.graphics.lib.lightsource;

import java.awt.Color;

import com.graphics.lib.IntensityComponents;
import com.graphics.lib.Point;

public interface ILightSource {

	boolean isDeleted();

	Point getPosition();

	void setPosition(Point position);

	Color getColour();

	/**
	 * Get the actual/apparent colour after applying any modifiers e.g. Intensity, the fact the light source is off etc
	 * 
	 * @return
	 */
	default Color getActualColour() { return getColour();}
	
	/**
	 * Get the amount of each colour component of the light illuminating the given point from this light source
	 * 
	 * @param p
	 * @return
	 */
	IntensityComponents getIntensityComponents(Point p);

	boolean isOn();

}