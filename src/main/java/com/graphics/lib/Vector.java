package com.graphics.lib;

/**
 * Indicates direction and speed
 * 
 * @author paul.brandon
 *
 */
public record Vector(double x, double y, double z) {

	public static final Vector ZERO_VECTOR = new Vector(0,0,0);
	
	public Vector(Vector from) {
		this(from.x, from.y, from.z);
	}
	

    /**
	 * A unit vector is one where it's magnitude is exactly 1
	 * 
	 * @return Unit Vector
	 */
	public Vector getUnitVector()
	{
		double divider = getSpeed();
		if (divider == 0){
			return ZERO_VECTOR;
		}
		
		return new Vector(this.x/divider, this.y/divider, this.z/divider);
	}

	public Vector generateVelocity(double speed) {
		Vector unit = getUnitVector();
		return new Vector(unit.x() * speed, unit.y() * speed, unit.z() * speed);
	}
	
	/**
	 * Get speed of this vector compared to the unit vector
	 * 
	 * @return
	 */
	public double getSpeed()
	{
		double squareadd = (x*x)+(y*y)+(z*z);
		return Math.sqrt(squareadd);
	}
	
	/**
	 * The dot product of 2 vectors equals the cosine of the angle between them
	 * <br/>
	 * N.B. Must be unit vectors
	 * @param v2
	 * @return Cosine of angle between vectors
	 */
	public double dotProduct (Vector v2)
	{
		return (this.x * v2.x) + (this.y * v2.y) + (this.z * v2.z);
	}
	
	/**
	 * Return angle between 2 vectors in degrees
	 * 
	 * @param v2
	 * @return
	 */
	public double angleBetween (Vector v2)
    {
	    return Math.toDegrees(angleBetweenRad(v2));
    }
	
	/**
     * Return angle between 2 vectors in radians
     * 
     * @param v2
     * @return
     */
    public double angleBetweenRad (Vector v2)
    {
        return Math.acos(getUnitVector().dotProduct(v2.getUnitVector()));
    }
	
	/**
	 * Generates a vector that is perpendicular to the plane occupied by the 2 vectors it is generated from
	 * <br/>
	 * N.B. Ordering is important v1 x v2 will generate a vector opposite to v2 x v1
	 * 
	 * @param v2
	 * @return Perpendicular Vector
	 */
	public Vector crossProduct(Vector v2)
	{
		return new Vector((this.y*v2.z) - (v2.y*this.z),
				         -(this.x*v2.z ) + (v2.x*this.z),
				         (this.x*v2.y) - (v2.x*this.y));
	}
	
	/**
	 * Get a vector the exact opposite of this one
	 * 
	 * @return Reverse Vector
	 */
	public Vector getReverseVector()
	{
		return new Vector(-this.x, -this.y, -this.z);
	}
	
	public Vector combine(Vector other) {
		return new Vector(
			x + other.x, 
			y + other.y,
			z + other.z
			);
	}
	
}
