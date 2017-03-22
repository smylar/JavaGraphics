package com.graphics.lib.lightsource;

import com.graphics.lib.IntensityComponents;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.interfaces.IPointFinder;
import com.graphics.lib.interfaces.IVectorFinder;

/**
 * A light source that illuminates in a given direction over a given area defined by a given angle
 * <br/>
 * In a difference to tied light source setup, objects of this class can take anonymous functions to define the location and direction
 * of the light source. This should effectively relate an object with this light source without either knowing about each other, or sending a notification.
 * <br/>
 * It should allow for greater flexibility in how that information is worked out 
 * 
 * @author Paul Brandon
 *
 */
public class DirectionalLightSource extends LightSource {

	private double lightConeAngle = 20;
	private IVectorFinder direction;
	private IPointFinder position;
	
	public DirectionalLightSource(){
		this(0,0,0);
	}
	
	public DirectionalLightSource(double x, double y, double z) {
		super(x, y, z);
		direction = () -> {return new Vector(0,0,1);};
		position = () -> {return new Point(x,y,z);};
	}

	public double getLightConeAngle() {
		return lightConeAngle;
	}

	public void setLightConeAngle(double lightConeAngle) {
		this.lightConeAngle = lightConeAngle;
	}

	public Vector getDirection() {
		return direction.getVector();
	}

	public void setDirection(IVectorFinder direction) {
		this.direction = direction;
	}
	
	@Override
	public Point getPosition() {
		return position.find();
	}
	
	@Override
	public void setPosition(Point p) {
		position = () -> {return p;};
	}

	public void setPosition(IPointFinder position) {
		this.position = position;
	}
	
	/**
	 * Get the amount of each colour component of the light illuminating the given point from this light source
	 * 
	 * @param p
	 * @return
	 */
	@Override
	public IntensityComponents getIntensityComponents(Point p)
	{
		Vector lightVector = this.getPosition().vectorToPoint(p).getUnitVector();
		
		double angleRad = this.getDirection().dotProduct(lightVector);
		if (Math.toDegrees(Math.acos(angleRad)) > this.getLightConeAngle()) return new IntensityComponents();

		return super.getIntensityComponents(p);
	}

}
