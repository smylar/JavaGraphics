package com.graphics.lib;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.graphics.lib.camera.Camera;

/**
 * A facet generally links 3 vertices of an object to form a triangle.
 * A series of these facets will make up how the whole object looks
 * 
 * @author Paul Brandon
 *
 */
public class Facet {
	private WorldCoord point1;
	private WorldCoord point2;
	private WorldCoord point3;
	private List<WorldCoord> list = new ArrayList<WorldCoord>();
	private Color colour;
	private double baseIntensity = 0.15;
	private double maxIntensity = 1;
	private boolean isFrontFace = true;
	private String tag = null;
	
	public Facet(WorldCoord p1, WorldCoord p2, WorldCoord p3)
	{
		this.point1 = p1;
		this.point2 = p2;
		this.point3 = p3;
		list.add(point1);
		list.add(point2);
		list.add(point3);
	}
	
	public Facet(WorldCoord p1, WorldCoord p2, WorldCoord p3, String tag)
	{
		this(p1, p2, p3);
		this.tag = tag;
	}
	
	public List<WorldCoord> getAsList(){
		return list;
	}
	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public double getMaxIntensity() {
		return maxIntensity;
	}
	
	public void setMaxIntensity(double maxIntensity) {
		if (maxIntensity < 0) this.maxIntensity = 0;
		else if (maxIntensity > 1) this.maxIntensity = 1;
		else this.maxIntensity = maxIntensity;
	}

	public double getBaseIntensity() {
		return baseIntensity;
	}

	public void setBaseIntensity(double baseIntensity) {
		if (baseIntensity < 0) this.baseIntensity = 0;
		else if (baseIntensity > 1) this.baseIntensity = 1;
		else this.baseIntensity = baseIntensity;
	}
	
	public void checkIntensity(IntensityComponents intensity){
		if (intensity.getRed() > maxIntensity) intensity.setRed(maxIntensity);
		else if (intensity.getRed() < baseIntensity) intensity.setRed(baseIntensity);
		
		if (intensity.getGreen() > maxIntensity) intensity.setGreen(maxIntensity);
		else if (intensity.getGreen() < baseIntensity) intensity.setGreen(baseIntensity);
		
		if (intensity.getBlue() > maxIntensity) intensity.setBlue(maxIntensity);
		else if (intensity.getBlue() < baseIntensity) intensity.setBlue(baseIntensity);
	}
	
	public Color getColour() {
		return colour;
	}

	public void setColour(Color colour) {
		this.colour = colour;
	}
	
	public boolean isFrontFace() {
		return isFrontFace;
	}

	public void setFrontFace(boolean isFrontFace) {
		this.isFrontFace = isFrontFace;
	}
	
	public boolean contains(WorldCoord wc){
		return point1 == wc || point2 == wc || point3 == wc;
	}

	/**
	 * Gets the normal vector in relation to how the camera sees it
	 * 
	 * @return Normal Vector
	 */
	public Vector getTransformedNormal(Camera c){
		 Vector vector1 = new Vector(point2.getTransformed(c).x - point1.getTransformed(c).x, point2.getTransformed(c).y - point1.getTransformed(c).y, point2.getTransformed(c).z - point1.getTransformed(c).z);
		 Vector vector2 = new Vector(point3.getTransformed(c).x - point1.getTransformed(c).x, point3.getTransformed(c).y - point1.getTransformed(c).y, point3.getTransformed(c).z - point1.getTransformed(c).z);
		 
		 Vector normal = vector1.crossProduct(vector2);
		 return normal.getUnitVector();
	}
	 
	/**
	 * Gets a Vector that is perpendicular to the plane formed by the points of the facet with a magnitude of 1 (the normal vector)
	 * 
	 * @return Normal Vector
	 */
	public Vector getNormal()
	{
		Vector vector1 = new Vector(point2.x - point1.x, point2.y - point1.y, point2.z - point1.z);
		Vector vector2 = new Vector(point3.x - point1.x, point3.y - point1.y, point3.z - point1.z);
		 
		Vector normal = vector1.crossProduct(vector2);
		return normal.getUnitVector();
	}
	
	public Point getIntersectionPointWithFacetPlane(Point p, Vector vec){
		return this.getIntersectionPointWithFacetPlane(p, vec, false);
	}
	/**
	 * Get the point at which a given point will intersect with the plane of this facet
	 * 
	 * @param p - Start point
	 * @param vec - Vector to travel along
	 * @param includeBackfaces - Indicates if intersection should be generated if facet is back facing (relative to vec)
	 * @return - Point of intersection, or null if it cannot intersect
	 */
	public Point getIntersectionPointWithFacetPlane(Point p, Vector vec, boolean includeBackfaces){
		Vector normal = this.getNormal();
		Vector v = vec.getUnitVector();
		double t = (normal.x * v.x) + (normal.y * v.y) + (normal.z * v.z);
		double tmod = (normal.x*p.x) + (normal.y*p.y) + (normal.z*p.z);
		if (includeBackfaces && t == 0) return null;
		if (!includeBackfaces && t >= 0) return null;
		double tval = ((normal.x* point1.x) + (normal.y* point1.y) + (normal.z* point1.z) - tmod) / t;
		Point intersect =  new Point(p.x + (v.x*tval), p.y + (v.y*tval), p.z + (v.z*tval));
		
		Vector v2 = p.vectorToPoint(intersect).getUnitVector(); //check intersect isn't in opposite direction to vec
		
		return v2.dotProduct(v) <= 0 ? null : intersect;
	}
	
	/**
	 * Get the shortest distance in Units from the plane of this facet to a given point
	 * 
	 * @param p - Point to test
	 * @return
	 */
	public double getDistanceFromFacetPlane(Point p){
		Vector normal = this.getNormal();
		double planex = normal.x * (p.x - point1.x);
		double planey = normal.y * (p.y - point1.y);
		double planez = normal.z * (p.z - point1.z);
		
		double dist = planex + planey + planez; //should not need to divide by normal length as already normalized (=1)
		return dist < 0 ? -dist : dist;
	}
	
	/**
	 * Check whether a point is within the footprint of this facet (i.e. z value unimportant)
	 * 
	 * @param p - Point to test
	 * @return <code>True</code> if p is within facet footprint, <code>False</code> otherwise
	 */
	public boolean isPointWithin(Point p){
		if (p != null &&
			isSameSide(p, this.point1, this.point2, this.point3) &&
			isSameSide(p, this.point2, this.point3, this.point1) &&
			isSameSide(p, this.point3, this.point1, this.point2)
		){
			return true;
		}
		return false;
		
	}
	
	/**
	 * Checks if p and fPoint3 are on the same side of the line fPoint1 to fPoint2
	 * 
	 * @param p - Point being tested
	 * @param fPoint1 - Facet point 1
	 * @param fPoint2 - Facet point 2
	 * @param fPoint3 - Facet point 3
	 * @return <code>True</code> if p and fPoint3 are the same side of the line, <code>False</code> otherwise
	 */
	public static boolean isSameSide(Point p, Point fPoint1, Point fPoint2, Point fPoint3){
		Vector vInteresect = p.vectorToPoint(fPoint1);
		Vector vector1 = fPoint2.vectorToPoint(fPoint1);
		Vector vector2 = fPoint3.vectorToPoint(fPoint1);
		Vector cp1 = vector1.crossProduct(vInteresect);
		Vector cp2 = vector1.crossProduct(vector2);
		if (cp1.dotProduct(cp2) >= 0) return true;
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((colour == null) ? 0 : colour.hashCode());
		result = prime * result + ((point1 == null) ? 0 : point1.hashCode());
		result = prime * result + ((point2 == null) ? 0 : point2.hashCode());
		result = prime * result + ((point3 == null) ? 0 : point3.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Facet other = (Facet) obj;
		if (colour == null) {
			if (other.colour != null)
				return false;
		} else if (!colour.equals(other.colour))
			return false;
		if (point1 == null) {
			if (other.point1 != null)
				return false;
		} else if (!point1.equals(other.point1))
			return false;
		if (point2 == null) {
			if (other.point2 != null)
				return false;
		} else if (!point2.equals(other.point2))
			return false;
		if (point3 == null) {
			if (other.point3 != null)
				return false;
		} else if (!point3.equals(other.point3))
			return false;
		return true;
	}
	
	
}
