package com.graphics.lib.lightsource;

import java.awt.Color;

import com.graphics.lib.IntensityComponents;
import com.graphics.lib.Point;

public class BasicLightSource implements ILightSource {

	private Point position;
	private Color colour = new Color(255, 255, 255);
	
	public BasicLightSource(double x, double y, double z) {
		this.position = new Point(x, y, z);
	}
	
	@Override
	public boolean isDeleted() {
		return false;
	}

	@Override
	public Point getPosition() {
		return position;
	}
	
	public void setPosition(Point position) {
		this.position = position;
	}

	@Override
	public Color getColour() {
		return colour;
	}

	@Override
	public Color getActualColour() {
		return colour;
	}

	@Override
	public IntensityComponents getIntensityComponents(Point p) {
		IntensityComponents comps = new IntensityComponents();
		comps.setBlue(1);
		comps.setGreen(1);
		comps.setRed(1);
		return comps;
	}

	@Override
	public boolean isOn() {
		return true;
	}

}
