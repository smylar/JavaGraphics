package com.graphics.lib.lightsource;

import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.interfaces.IDirectionFinder;
import com.graphics.lib.interfaces.IPointFinder;

public class DirectionalLightSource extends LightSource {

	private double lightConeAngle = 20;
	private IDirectionFinder direction;
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
		return direction.getDirection();
	}

	public void setDirection(IDirectionFinder direction) {
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
