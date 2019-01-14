package com.graphics.shapes;

import com.graphics.lib.VertexNormalFinderEnum;
import com.graphics.lib.canvas.CanvasObjectFunctions;
import com.graphics.lib.canvas.FunctionHandler;

public final class Sphere extends Ovoid {
	
	public Sphere(double radius)
	{
		this(radius, 10);
	}
	
	public Sphere(double radius, int angleProgression)
	{
		super(radius, 1, angleProgression);
		this.setVertexNormalFinder(VertexNormalFinderEnum.CENTRE_TO_POINT.get());
		FunctionHandler.register(this, CanvasObjectFunctions.SPHERE);
	}
}
