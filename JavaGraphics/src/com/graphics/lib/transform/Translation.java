package com.graphics.lib.transform;

import java.util.function.Consumer;

import com.graphics.lib.Point;

public class Translation extends Transform {

	public double transX = 0;
	public double transY = 0;
	public double transZ = 0;
	
	public Translation()
	{}
	
	public Translation(Point p)
	{
		this.transX = p.x;
		this.transY = p.y;
		this.transZ = p.z;
	}
	
	public Translation(double transX, double transY, double transZ)
	{
		this.transX = transX;
		this.transY = transY;
		this.transZ = transZ;
	}

	@Override
	public boolean isCompleteSpecific() {
		return true;
	}

	@Override
	public Consumer<Point> doTransformSpecific() {
		return (p) -> {
			p.x += transX;
			p.y += transY;
			p.z += transZ;
		};
	}

}
