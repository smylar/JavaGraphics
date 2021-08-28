package com.graphics.lib;

import java.util.HashSet;
import java.util.Set;

/**
 * Representation of a 3D point in space
 * 
 * @author Paul Brandon
 *
 */
public class Point {
    //TODO refactor
	public double x = 0;
	public double y = 0;
	public double z = 0;
	private Set<String> tags = new HashSet<>();
	
	public Point(Point p)
	{
		this.x = p.x;
		this.y = p.y;
		this.z = p.z;
	}
	
	public Point(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Does point have the specified tag
	 * 
	 * @see #setTag(String)
	 * @return Tag
	 */
	public boolean hasTag(String tag) {
		return tags.contains(tag);
	}
	
	public boolean hasTags(){
		return !tags.isEmpty();
	}
	
	public int tagCount(){
		return tags.size();
	}

	/**
	 * Setting a tag on a point implicitly means the point is special and not intrinsically part of the object model, 
	 * but need to be treated as if they are part of the model
	 * <br/>
	 * For example an object may have an orientation model added to it.
	 * <br/>
	 * The tagged points are still transformed with the rest, it just allows other systems to identify the points it needs to deal with and handle them separately,
	 * or for the object itself to ignore them in certain calculations e.g. Centre point calculation
	 */
	public void addTag(String tag) {
		this.tags.add(tag);
	}
	
	public void removeTag(String tag) {
		this.tags.remove(tag);
	}
	
	/**
	 * Calculates the (positive) unit distance from this point to another given point
	 * 
	 * @param p2 - Point to measure distance to
	 * @return The distance
	 */
	public double distanceTo(Point p2)
	{
		double dx = p2.x - x;
		double dy = p2.y - y;
		double dz = p2.z - z;
		
		double adj = Math.hypot(dx, dz);
		return Math.hypot(dy, adj);
	}
	
	public double screenDistanceTo(Point p2)
    {
        double dx = p2.x - x;
        double dy = p2.y - y;
        
        return Math.hypot(dx, dy);
    }
	
	/**
	 * Generate a vector that points from this point to another given point
	 * 
	 * @see Vector
	 * 
	 * @param p2 - Point to generate a Vector to
	 * @return The Vector
	 */
	public Vector vectorToPoint(Point p2)
	{
		return new Vector(p2.x - this.x, p2.y - this.y, p2.z - this.z);
	}
	
	public void add(Point p) {
		x += p.x;
		y += p.y;
		z += p.z;
	}
	
	public void subtract(Point p) {
		x -= p.x;
		y -= p.y;
		z -= p.z;
	}
	
	public void copyFrom(Point p) {
		x = p.x;
		y = p.y;
		z = p.z;
	}
	
	@Override
	public String toString() {
		return this.x + "," + this.y + "," + this.z;
	}
	

	public boolean isEqualTo(Object obj) {
		//checks if two points occupy the same coordinate
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Point))
			return false;
		Point other = (Point) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
			return false;
		return true;
	}
	
}
