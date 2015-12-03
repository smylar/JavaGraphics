package com.graphics.shapes;

import com.graphics.lib.Utils;

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
}
