package com.graphics.lib;

/**
 * Representation of a 3D point in space
 * 
 * @author Paul Brandon
 *
 */
public class Point {
	
	private static int nextId = 0;
	
	private int objectId = nextId++;
	public double x = 0;
	public double y = 0;
	public double z = 0;
	private String tag = "";
	
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
	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	/*public Vector getNormal() {
		return normal;
	}*/

	/*public void setNormal(IVertexNormalFinder vnFinder, Facet f, CanvasObject obj) {
		this.normal = vnFinder.getVertexNormal(obj, this, f);
	}*/
	
	/*public void setNormal(Vector normal) {
		this.normal = normal;
	}*/
	
	/*public IntensityComponents getLightIntensity() {
		if (this.lightIntensity == null) return new IntensityComponents();
		return this.lightIntensity;
	}*/

	/*public void setLightIntensity(IntensityComponents lightIntensity) {
		this.lightIntensity = lightIntensity;
	}*/
	
	/*public void setLightIntensity(ILightIntensityFinder liFinder, boolean isPartOfBackface, CanvasObject obj, Vector v) {
		this.lightIntensity = liFinder.getLightIntensity(obj, this, v, isPartOfBackface);
	}*/
	
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
		
		double adj = Math.sqrt((dx * dx) + (dz * dz));
		return Math.sqrt((dy * dy) + (adj * adj));
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
	
	@Override
	public String toString(){
		return this.x + "," + this.y + "," + this.z;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.getClass().getName().hashCode();
		result = prime * result + objectId;
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
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
		Point other = (Point) obj;
		/*if (normal == null) {
			if (other.normal != null)
				return false;
		} else if (!normal.equals(other.normal))
			return false;*/
		if (tag == null) {
			if (other.tag != null)
				return false;
		} else if (!tag.equals(other.tag))
			return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
			return false;
		return true;
	}
	
}
