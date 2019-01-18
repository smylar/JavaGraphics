package com.graphics.lib.collectors;

import java.util.function.Consumer;

import com.graphics.lib.Point;

/**
 * Used to collect a set of points and generate a centre point 
 * that is the centre of a cube encompassing all points
 * 
 * May rename this as can be used for more than centre
 * @author paul
 *
 */
public class CentreFinder implements Consumer<Point> {
	
	private Double maxX = null;
	private Double maxY = null;
	private Double maxZ = null;
	private Double minX = null;
	private Double minY = null;
	private Double minZ = null;

	@Override
	public void accept(Point p) {
	    maxX = max(maxX, p.x);
	    minX = min(minX, p.x);
	    maxY = max(maxY, p.y);
        minY = min(minY, p.y);
        maxZ = max(maxZ, p.z);
        minZ = min(minZ, p.z);
	}
	
	public void combine(CentreFinder other) {
	    maxX = max(maxX, other.maxX);
        minX = min(minX, other.minX);
        maxY = max(maxY, other.maxY);
        minY = min(minY, other.minY);
        maxZ = max(maxZ, other.maxZ);
        minZ = min(minZ, other.minZ);
	}
	
	public Double getMaxX() {
        return maxX;
    }

    public Double getMaxY() {
        return maxY;
    }

    public Double getMaxZ() {
        return maxZ;
    }

    public Double getMinX() {
        return minX;
    }

    public Double getMinY() {
        return minY;
    }

    public Double getMinZ() {
        return minZ;
    }

    public Point result() {
		//NULLS ??
		return new Point(minX + ((maxX - minX)/2), minY + ((maxY - minY)/2), minZ + ((maxZ - minZ)/2));
	}
	
	private Double min(Double current, Double other) {
	    return (current == null || other < current) ? other : current;
	}
	
	private Double max(Double current, Double other) {
        return (current == null || other > current) ? other : current;
    }

}
