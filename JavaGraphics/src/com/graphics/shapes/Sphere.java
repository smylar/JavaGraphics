package com.graphics.shapes;

import com.graphics.lib.Point;
import com.graphics.lib.VertexNormalFinderEnum;
import com.graphics.lib.canvas.CanvasObjectFunctions;

public class Sphere extends Ovoid {
	
	public Sphere(double radius)
	{
		this(radius, 10);
	}
	
	public Sphere(double radius, int angleProgression)
	{
		super(radius, 1, angleProgression);
		this.getData().vnFinder = VertexNormalFinderEnum.CENTRE_TO_POINT;
		this.setFunctions(CanvasObjectFunctions.SPHERE.get());
	}
	
	@Override
	public boolean isPointInside(Point p)
	{
		double actualRadius = getCentre().distanceTo(this.getVertexList().get(0)); //may have changed in scaling
		return this.getCentre().distanceTo(p) < actualRadius;
	}
}
