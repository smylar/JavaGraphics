package com.graphics.lib;

import java.awt.Color;

import com.graphics.lib.texture.Texture;

public class Facet {
	public WorldCoord point1;
	public WorldCoord point2;
	public WorldCoord point3;
	private Color colour;
	private double baseIntensity = 0.15;
	private boolean isFrontFace = true;
	private Texture texture;
	
	public Facet(WorldCoord p1, WorldCoord p2, WorldCoord p3)
	{
		this.point1 = p1;
		this.point2 = p2;
		this.point3 = p3;
	}

	public double getBaseIntensity() {
		return baseIntensity;
	}

	public void setBaseIntensity(double baseIntensity) {
		if (baseIntensity < 0) this.baseIntensity = 0;
		else if (baseIntensity > 1) this.baseIntensity = 1;
		else this.baseIntensity = baseIntensity;
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

	public Texture getTexture() {
		return texture;
	}

	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	public Vector getTransformedNormal()
	 {
		 Vector vector1 = new Vector(point2.getTransformed().x - point1.getTransformed().x, point2.getTransformed().y - point1.getTransformed().y, point1.getTransformed().z - point1.getTransformed().z);
		 Vector vector2 = new Vector(point3.getTransformed().x - point1.getTransformed().x, point3.getTransformed().y - point1.getTransformed().y, point3.getTransformed().z - point1.getTransformed().z);
		 
		 Vector normal = vector1.crossProduct(vector2);
		 return normal.getUnitVector();
	 }
	 
	public Vector getNormal()
	{
		Vector vector1 = new Vector(point2.x - point1.x, point2.y - point1.y, point2.z - point1.z);
		Vector vector2 = new Vector(point3.x - point1.x, point3.y - point1.y, point3.z - point1.z);
		 
		Vector normal = vector1.crossProduct(vector2);
		return normal.getUnitVector();
	}
	
	/**
	 * Get the point at which a given point will intersect with the plane of this facet
	 * 
	 * @param p - Start point
	 * @param vec - Vector to travel along
	 * @return - Point of intersection, or null if it cannot intersect
	 */
	public Point getIntersectionPointWithFacetPlane(Point p, Vector vec){
		Vector normal = this.getNormal();
		Vector v = vec.getUnitVector();
		double t = (normal.x * v.x) + (normal.y * v.y) + (normal.z * v.z);
		double tmod = (normal.x*p.x) + (normal.y*p.y) + (normal.z*p.z);
		if (t == 0) return null;
		double tval = ((normal.x* point1.x) + (normal.y* point1.y) + (normal.z* point1.z) - tmod) / t;
		return new Point(p.x + (v.x*tval), p.y + (v.y*tval), p.z + (v.z*tval));
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
		
		return planex + planey + planez; //should not need to divide by normal length as already normalized (=1)
	}
	
	/**
	 * Check whether a point is within the footprint of this facet (i.e. z value unimportant)
	 * 
	 * @param p - Point to test
	 * @return <code>True</code> if p is within facet footprint, <code>False</code> otherwise
	 */
	public boolean isPointWithin(Point p){
		if (
			this.isSameSide(p, this.point1, this.point2, this.point3) &&
			this.isSameSide(p, this.point2, this.point3, this.point1) &&
			this.isSameSide(p, this.point3, this.point1, this.point2)
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
	private boolean isSameSide(Point p, Point fPoint1, Point fPoint2, Point fPoint3){
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
