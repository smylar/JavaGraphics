package com.graphics.lib.lightsource;

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
	
	public DirectionalLightSource() {
		super(0, 0, 0);
		direction = () -> {return new Vector(0,0,1);};
		position = () -> {return new Point(0,0,0);};
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

}
