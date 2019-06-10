package com.graphics.lib;

import java.awt.Color;
import java.util.List;

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
	
	public Facet(WorldCoord p1, WorldCoord p2, WorldCoord p3) {
	    super(p1, p2, p3);
	}
	
	public Facet(WorldCoord p1, WorldCoord p2, WorldCoord p3, String tag) {
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
	
	public IntensityComponents checkIntensity(IntensityComponents intensity) {
	    IntensityComponents.forEach(comp -> {
	        if (intensity.get(comp) > maxIntensity) 
                intensity.set(comp, maxIntensity);
            else if (intensity.get(comp) < baseIntensity) 
                intensity.set(comp, baseIntensity);
	    });
	    
	    return intensity;
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
	public Vector getTransformedNormal(Camera c) {
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
	public Vector getNormal() {
		Vector vector1 = new Vector(second().x - first().x, second().y - first().y, second().z - first().z);
		Vector vector2 = new Vector(third().x - first().x, third().y - first().y, third().z - first().z);
		 
		Vector normal = vector1.crossProduct(vector2);
		return normal.getUnitVector();
	}
	
	public Point getIntersectionPointWithFacetPlane(Point p, Vector vec) {
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
	public Point getIntersectionPointWithFacetPlane(Point p, Vector vec, boolean includeBackfaces) {
		Vector normal = this.getNormal();
		Vector v = vec.getUnitVector();
		double t = (normal.getX() * v.getX()) + (normal.getY() * v.getY()) + (normal.getZ() * v.getZ());
		double tmod = (normal.getX()*p.x) + (normal.getY()*p.y) + (normal.getZ()*p.z);
		if (includeBackfaces && t == 0) 
		    return null;
		if (!includeBackfaces && t >= 0) 
		    return null;
		double tval = ((normal.getX()* first().x) + (normal.getY()* first().y) + (normal.getZ()* first().z) - tmod) / t;
		Point intersect =  new Point(p.x + (v.getX()*tval), p.y + (v.getY()*tval), p.z + (v.getZ()*tval));
		
		Vector v2 = p.vectorToPoint(intersect).getUnitVector(); //check intersect isn't in opposite direction to vec
		
		return v2.dotProduct(v) <= 0 ? null : intersect;
	}
	
	/**
	 * Get the shortest distance in Units from the plane of this facet to a given point
	 * 
	 * @param p - Point to test
	 * @return
	 */
	public double getDistanceFromFacetPlane(Point p) {
		Vector normal = this.getNormal();
		double planex = normal.getX() * (p.x - first().x);
		double planey = normal.getY() * (p.y - first().y);
		double planez = normal.getZ() * (p.z - first().z);
		
		double dist = planex + planey + planez; //should not need to divide by normal length as already normalized (=1)
		return dist < 0 ? -dist : dist;
	}
	
	/**
	 * Check whether a point is within the footprint of this facet (i.e. z value unimportant)
	 * 
	 * @param p - Point to test
	 * @return <code>True</code> if p is within facet footprint, <code>False</code> otherwise
	 */
	public boolean isPointWithin(Point p) {
		return p != null &&
			isSameSide(p, this.first(), this.second(), this.third()) &&
			isSameSide(p, this.second(), this.third(), this.first()) &&
			isSameSide(p, this.third(), this.first(), this.second());
		
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
	public static boolean isSameSide(Point p, Point fpoint1, Point fpoint2, Point fpoint3) {
		Vector vInteresect = p.vectorToPoint(fpoint1);
		Vector vector1 = fpoint2.vectorToPoint(fpoint1);
		Vector vector2 = fpoint3.vectorToPoint(fpoint1);
		Vector cp1 = vector1.crossProduct(vInteresect);
		Vector cp2 = vector1.crossProduct(vector2);
		return cp1.dotProduct(cp2) >= 0;
	}

	public boolean isEqualTo(Facet other) {
        if (this == other)
            return true;
        if (other == null)
            return false;

        if (colour == null) {
            if (other.colour != null)
                return false;
        } else if (!colour.equals(other.colour))
            return false;

        List<WorldCoord> coords = this.getAsList();
        List<WorldCoord> otherCoords = other.getAsList();
        for (int i = 0 ; i < coords.size() ; i++) {
            //are ImmutableLists of size 3, so will not contains nulls
            if (!coords.get(i).isEqualTo(otherCoords.get(i))) {
                return false;
            }
        }
        return true;
	}
	
}
