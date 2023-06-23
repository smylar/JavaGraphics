package com.graphics.lib.lightsource;

import java.awt.Color;
import java.util.concurrent.SubmissionPublisher;

import com.graphics.lib.IntensityComponents;
import com.graphics.lib.Point;

import static com.graphics.lib.IntensityComponents.ColourComponent.*;

public class LightSource extends SubmissionPublisher<String> implements ILightSource {
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
		
	/* (non-Javadoc)
	 * @see com.graphics.lib.lightsource.ILightSource#isDeleted()
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see com.graphics.lib.lightsource.ILightSource#getPosition()
	 */
	@Override
	public Point getPosition() {
		return position;
	}

	public void setPosition(Point position) {
		this.position = position;
		this.flagChange(POSITIONCHANGE);
	}

	public void setIntensity(double intensity) {
		this.intensity = intensity;
		this.setActualColour();
		this.flagChange(INTENSITYCHANGE);
	}
	
	public double getIntensity() {
		return this.intensity;
	}

	/* (non-Javadoc)
	 * @see com.graphics.lib.lightsource.ILightSource#getColour()
	 */
	@Override
	public Color getColour() {
		return colour;
	}
	
	/* (non-Javadoc)
	 * @see com.graphics.lib.lightsource.ILightSource#getActualColour()
	 */
	@Override
	public Color getActualColour() {
		return isOn() ? actualColour : Color.black;
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
		this.on = !this.on;
		this.flagChange(ONOFFCHANGE);
	}
	
	/* (non-Javadoc)
	 * @see com.graphics.lib.lightsource.ILightSource#isOn()
	 */
	@Override
	public boolean isOn() {
		return on;
	}

	protected void flagChange(String change){
		submit(change);
	}
	
	
	@Override
	public IntensityComponents getIntensityComponents(Point p)
	{
		IntensityComponents components = new IntensityComponents();
		if (this.on){
			double intensity = this.getIntensity(p);
			components.set(RED, ((double)this.colour.getRed() / 255) * intensity);
			components.set(GREEN, ((double)this.colour.getGreen() / 255) * intensity);
			components.set(BLUE,((double)this.colour.getBlue() / 255) * intensity);
		}
		return components;
	}
	
	protected double getIntensity(Point p) {
		if (this.range < 1) {
		    return intensity;
		}
		//N.B. some lightsources are overriding getPosition(), leaving position here as 0
		//may need some refactoring, but for now must not use the local position field here
		double distanceAway = getPosition().distanceTo(p);
		if (distanceAway > range || distanceAway < 0) {
		    return 0;
		}
		return intensity - ((intensity/range) * distanceAway);
	}

	public boolean isEqualTo(Object obj) {
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
		} else if (!position.isEqualTo(other.position))
			return false;
		
		return Double.doubleToLongBits(range) == Double
				.doubleToLongBits(other.range);
	} 
}
