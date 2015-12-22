package com.graphics.lib.lightsource;

import java.awt.Color;

import com.graphics.lib.IntensityComponents;
import com.graphics.lib.Point;

public class LightSource{

	private Point position;
	private double intensity = 1;
	private Color colour = new Color(255, 255, 255);
	private boolean on = true;
	private double range = 5000;
	private boolean deleted = false;
	
	public LightSource(double x, double y, double z) {
		this.position = new Point(x, y, z);
	}
		
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
		this.on = false;
	}

	public double getRange() {
		return range;
	}
	
	public void setRange(double range) {
		this.range = range;
	}

	public Point getPosition() {
		return position;
	}

	public void setPosition(Point position) {
		this.position = position;
	}

	public double getIntensity(Point p) {
		if (!this.on) return 0;
		if (this.range < 1) return this.intensity;
		double distanceAway = this.position.distanceTo(p);
		if (distanceAway > this.range || distanceAway < 0) return 0;
		return this.intensity - ((intensity/range) * distanceAway);
	}

	public void setIntensity(double intensity) {
		this.intensity = intensity;
	}
	
	public double getIntensity() {
		return this.intensity;
	}

	public Color getColour() {
		return colour;
	}

	public void setColour(Color colour) {
		this.colour = colour;
	}
	
	public void turnOn()
	{
		this.on = true;
	}
	
	public void turnOff()
	{
		this.on = false;
	}
	
	public void toggle()
	{
		this.on = this.on ? false : true;
	}
	
	public boolean isOn() {
		return on;
	}

	public IntensityComponents getIntensityComponents(Point p)
	{
		IntensityComponents components = new IntensityComponents();
		components.setRed(((double)this.colour.getRed() / 255) * this.getIntensity(p));
		components.setGreen(((double)this.colour.getGreen() / 255) * this.getIntensity(p));
		components.setBlue(((double)this.colour.getBlue() / 255) * this.getIntensity(p));
		return components;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((colour == null) ? 0 : colour.hashCode());
		long temp;
		temp = Double.doubleToLongBits(range);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LightSource other = (LightSource) obj;
		if (colour == null) {
			if (other.colour != null)
				return false;
		} else if (!colour.equals(other.colour))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		if (Double.doubleToLongBits(range) != Double
				.doubleToLongBits(other.range))
			return false;
		return true;
	}
	
	
 
}
