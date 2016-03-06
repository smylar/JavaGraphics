package com.graphics.shapes;

import com.graphics.lib.Point;
import com.graphics.lib.Utils;
import com.graphics.lib.Vector;

public class Sphere extends Ovoid {
	
	public Sphere(double radius)
	{
		this(radius, 10);
	}
	
	public Sphere(double radius, int angleProgression)
	{
		super(radius, 1, angleProgression);
		this.getData().vnFinder = Utils.VertexNormalFinderEnum.CENTRE_TO_POINT;
	}
	
	@Override
	public boolean isPointInside(Point p)
	{
		double actualRadius = getCentre().distanceTo(this.getVertexList().get(0)); //may have changed in scaling
		return this.getCentre().distanceTo(p) < actualRadius;
	}
	
	@Override
	public boolean vectorIntersects(Point start, Vector v){
		//as shape is a sphere we can use the rough calculation, which uses a sphere
		return this.vectorIntersectsRoughly(start, v);
	}
}
