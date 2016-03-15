package com.graphics.lib.lightsource;

import java.awt.Color;
import java.util.Observable;

import com.graphics.lib.IntensityComponents;
import com.graphics.lib.Point;

public class LightSource extends Observable{
	public static final String ONOFFCHANGE = "ONOFF";
	public static final String POSITIONCHANGE = "POS";
	public static final String INTENSITYCHANGE = "INT";
	public static final String COLOURCHANGE = "COLOUR";
	
	private Point position;
	private double intensity = 1;
	private Color colour = new Color(255, 255, 255);
	private Color actualColour = new Color(255, 255, 255);
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
		this.flagChange(POSITIONCHANGE);
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
		this.setActualColour();
		this.flagChange(INTENSITYCHANGE);
	}
	
	public double getIntensity() {
		return this.intensity;
	}

	public Color getColour() {
		return colour;
	}
	
	public Color getActualColour(){
		return (isOn() ? actualColour : Color.black);
	}
	
	protected void setActualColour() {
		actualColour = new Color(
					(int)Math.round(colour.getRed() * intensity),
					(int)Math.round(colour.getGreen() * intensity),
					(int)Math.round(colour.getBlue() * intensity),
					colour.getAlpha()
					);
	}

	public void setColour(Color colour) {
		this.colour = colour;
		this.setActualColour();
		this.flagChange(COLOURCHANGE);
	}
	
	public void turnOn()
	{
		this.on = true;
		this.flagChange(ONOFFCHANGE);
	}
	
	public void turnOff()
	{
		this.on = false;
		this.flagChange(ONOFFCHANGE);
	}
	
	public void toggle()
	{
		this.on = this.on ? false : true;
		this.flagChange(ONOFFCHANGE);
	}
	
	public boolean isOn() {
		return on;
	}

	protected void flagChange(String change){
		this.setChanged();
		this.notifyObservers(change);
	}
	
	/**
	 * Get the amount of each colour component of the light illuminating the given point from this light source
	 * 
	 * @param p
	 * @return
	 */
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
