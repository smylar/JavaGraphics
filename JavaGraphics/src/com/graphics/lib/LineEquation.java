package com.graphics.lib;

import com.graphics.lib.camera.Camera;

/**
 * Representation of a line using the equation y = mx + c
 * 
 * @author Paul Brandon
 *
 */
public final class LineEquation {
	private final Double x;
	private final Double y;
	private final Double c;
	private final Double m;
	private final Point start;
	private final Point end;
	private final WorldCoord worldStart;
	private final WorldCoord worldEnd;
	
	public LineEquation (WorldCoord worldStart, WorldCoord worldEnd, Camera cam)
	{
		this.worldEnd = worldEnd;
		this.worldStart = worldStart;
		this.start = worldStart.getTransformed(cam);
		this.end = worldEnd.getTransformed(cam);
		double dx = end.x - start.x;
		if (dx == 0){
			x = start.x;
			c = null;
			y = null;
			m = null;
		} else {
		    x = null;
			double dy = end.y - start.y;
			if (dy == 0)
			{
				y = end.y;
				m = null;
				c = null;
			}
			else
			{
			    y = null;
				m = dy/dx;
				c = start.y - (m*start.x);
			}
		}
	}
	
	public double getMaxX()
	{
		return start.x > end.x ? start.x : end.x;
	}
	
	public double getMaxY()
	{
		return start.y > end.y ? start.y : end.y;
	}
	
	public double getMinX()
	{
		return start.x < end.x ? start.x : end.x;
	}
	
	public double getMinY()
	{
		return start.y < end.y ? start.y : end.y;
	}
	
	public double getLength()
	{
		double dx = this.getMaxX() - this.getMinX();
		double dy = this.getMaxY() - this.getMinY();
		return Math.sqrt((dx*dx) + (dy*dy));
	}
	
	public Double getXAtY(double yValue)
	{
		if (this.x != null) return this.x;
		else if (this.y != null) return null;
		else return (yValue - this.c) / this.m;
	}
	
	public Double getYAtX(double xValue)
	{
		if (this.x != null) return null;
		else if (this.y != null) return this.y;
		else return (this.m * xValue) + this.c;
	}

	public Point getStart() {
		return start;
	}

	public Point getEnd() {
		return end;
	}

	public WorldCoord getWorldStart() {
		return worldStart;
	}

	public WorldCoord getWorldEnd() {
		return worldEnd;
	}	
	
}
