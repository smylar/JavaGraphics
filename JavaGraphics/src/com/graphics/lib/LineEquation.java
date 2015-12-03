package com.graphics.lib;

public class LineEquation {
	private Double x;
	private Double y;
	private Double c;
	private Double m;
	private Point start;
	private Point end;
	
	public LineEquation (Point start, Point end)
	{
		this.start = start;
		this.end = end;
		double dx = end.x - start.x;
		if (dx == 0){
			x = start.x;
		}else{
			double dy = end.y - start.y;
			if (dy == 0)
			{
				y = end.y;
			}
			else
			{
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
	
}
