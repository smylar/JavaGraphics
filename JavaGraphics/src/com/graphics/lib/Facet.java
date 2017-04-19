package com.graphics.lib;

import java.awt.Color;
import com.graphics.lib.camera.Camera;

/**
 * A facet generally links 3 vertices of an object to form a triangle.
 * A series of these facets will make up how the whole object looks
 * 
 * @author Paul Brandon
 *
 */
public class Facet extends Triplet<WorldCoord> {
	private Color colour;
	private double baseIntensity = 0.15;
	private double maxIntensity = 1;
	private boolean isFrontFace = true;
	private String tag = null;
	
	public Facet(WorldCoord p1, WorldCoord p2, WorldCoord p3)
    {
	    super(p1, p2, p3);
    }
	
	public Facet(WorldCoord p1, WorldCoord p2, WorldCoord p3, String tag)
	{
		super(p1, p2, p3);
		this.tag = tag;
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
		if (maxIntensity < 0) 
		    this.maxIntensity = 0;
		else if (maxIntensity > 1) 
		    this.maxIntensity = 1;
		else 
		    this.maxIntensity = maxIntensity;
	}

	public double getBaseIntensity() {
		return baseIntensity;
	}

	public void setBaseIntensity(double baseIntensity) {
		if (baseIntensity < 0) 
		    this.baseIntensity = 0;
		else if (baseIntensity > 1) 
		    this.baseIntensity = 1;
		else 
		    this.baseIntensity = baseIntensity;
	}
	
	public void checkIntensity(IntensityComponents intensity){
		if (intensity.getRed() > maxIntensity) 
		    intensity.setRed(maxIntensity);
		else if (intensity.getRed() < baseIntensity) 
		    intensity.setRed(baseIntensity);
		
		if (intensity.getGreen() > maxIntensity) 
		    intensity.setGreen(maxIntensity);
		else if (intensity.getGreen() < baseIntensity) 
		    intensity.setGreen(baseIntensity);
		
		if (intensity.getBlue() > maxIntensity) 
		    intensity.setBlue(maxIntensity);
		else if (intensity.getBlue() < baseIntensity) 
		    intensity.setBlue(baseIntensity);
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
		return getAsList().contains(wc);
	}

	/**
	 * Gets the normal vector in relation to how the camera sees it
	 * 
	 * @return Normal Vector
	 */
	public Vector getTransformedNormal(Camera c){
		 Vector vector1 = new Vector(second().getTransformed(c).x - first().getTransformed(c).x, second().getTransformed(c).y - first().getTransformed(c).y, second().getTransformed(c).z - first().getTransformed(c).z);
		 Vector vector2 = new Vector(third().getTransformed(c).x - first().getTransformed(c).x, third().getTransformed(c).y - first().getTransformed(c).y, third().getTransformed(c).z - first().getTransformed(c).z);
		 
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
		Vector vector1 = new Vector(second().x - first().x, second().y - first().y, second().z - first().z);
		Vector vector2 = new Vector(third().x - first().x, third().y - first().y, third().z - first().z);
		 
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
		if (includeBackfaces && t == 0) 
		    return null;
		if (!includeBackfaces && t >= 0) 
		    return null;
		double tval = ((normal.x* first().x) + (normal.y* first().y) + (normal.z* first().z) - tmod) / t;
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
		double planex = normal.x * (p.x - first().x);
		double planey = normal.y * (p.y - first().y);
		double planez = normal.z * (p.z - first().z);
		
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
			isSameSide(p, this.first(), this.second(), this.third()) &&
			isSameSide(p, this.second(), this.third(), this.first()) &&
			isSameSide(p, this.third(), this.first(), this.second())
		){
			return true;
		}
		return false;
		
	}
	
	/**
	 * Checks if p and fthird() are on the same side of the line ffirst() to fsecond()
	 * 
	 * @param p - Point being tested
	 * @param ffirst() - Facet point 1
	 * @param fsecond() - Facet point 2
	 * @param fthird() - Facet point 3
	 * @return <code>True</code> if p and fthird() are the same side of the line, <code>False</code> otherwise
	 */
	public static boolean isSameSide(Point p, Point fpoint1, Point fpoint2, Point fpoint3){
		Vector vInteresect = p.vectorToPoint(fpoint1);
		Vector vector1 = fpoint2.vectorToPoint(fpoint1);
		Vector vector2 = fpoint3.vectorToPoint(fpoint1);
		Vector cp1 = vector1.crossProduct(vInteresect);
		Vector cp2 = vector1.crossProduct(vector2);
		if (cp1.dotProduct(cp2) >= 0)
		    return true;
		
		return false;
	}

	public boolean isEqualTo(Object obj) {
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
		if (first() == null) {
			if (other.first() != null)
				return false;
		} else if (!first().isEqualTo(other.first()))
			return false;
		if (second() == null) {
			if (other.second() != null)
				return false;
		} else if (!second().isEqualTo(other.second()))
			return false;
		if (third() == null) {
			if (other.third() != null)
				return false;
		} else if (!third().isEqualTo(other.third()))
			return false;
		return true;
	}
	
	
}
