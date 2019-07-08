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
	
	public double getZValue(final double xVal, final double yVal)
    {
        double dx = xVal - start.x;
        double dy = yVal - start.y;
        double len = Math.sqrt((dx*dx)+(dy*dy));
        
        double percentLength = len / getLength();
        
        return interpolateZ(start.z, end.z, percentLength); 
    }
    
    public static double interpolateZ(final double startZ, final double endZ, final double percentLength)
    {
        return 1d / ((1d/startZ) + (percentLength * ((1d/endZ) - (1d/startZ))));
    }

	//generated
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((c == null) ? 0 : c.hashCode());
        result = prime * result + ((end == null) ? 0 : end.hashCode());
        result = prime * result + ((m == null) ? 0 : m.hashCode());
        result = prime * result + ((start == null) ? 0 : start.hashCode());
        result = prime * result + ((x == null) ? 0 : x.hashCode());
        result = prime * result + ((y == null) ? 0 : y.hashCode());
        return result;
    }

    //generated
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LineEquation other = (LineEquation) obj;
        if (c == null) {
            if (other.c != null)
                return false;
        } else if (!c.equals(other.c))
            return false;
        if (end == null) {
            if (other.end != null)
                return false;
        } else if (!end.equals(other.end))
            return false;
        if (m == null) {
            if (other.m != null)
                return false;
        } else if (!m.equals(other.m))
            return false;
        if (start == null) {
            if (other.start != null)
                return false;
        } else if (!start.equals(other.start))
            return false;
        if (x == null) {
            if (other.x != null)
                return false;
        } else if (!x.equals(other.x))
            return false;
        if (y == null) {
            if (other.y != null)
                return false;
        } else if (!y.equals(other.y))
            return false;
        return true;
    }	
	
	
}
