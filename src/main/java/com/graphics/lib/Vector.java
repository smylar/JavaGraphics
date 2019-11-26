package com.graphics.lib;

/**
 * Indicates direction and speed
 * 
 * @author paul.brandon
 *
 */
public final class Vector {

	public static final Vector ZERO_VECTOR = new Vector(0,0,0);
	
    private final double x;
    private final double y;
    private final double z;
	
	
	public Vector(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
    
    public static Builder builder() {
    	return new Builder();
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
		return Vector.builder().x((this.y*v2.z) - (v2.y*this.z))
						  	   .y(-(this.x*v2.z ) + (v2.x*this.z))
						  	   .z((this.x*v2.y) - (v2.x*this.y))
						  	   .build();
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
	
	@Override
	public String toString() {
		return this.x + "," + this.y + "," + this.z;
	}
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(z);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		Vector other = (Vector) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
			return false;
		return true;
	}



	public static final class Builder {
    	private double x = 0;
    	private double y = 0;
    	private double z = 0;
    	
    	public Builder x(double value) {
    		this.x = value;
    		return this;
    	}
    	
    	public Builder y(double value) {
    		this.y = value;
    		return this;
    	}
    	
    	public Builder z(double value) {
    		this.z = value;
    		return this;
    	}
    	
    	public Builder from(Vector vector) {
    		this.x = vector.getX();
    		this.y = vector.getY();
    		this.z = vector.getZ();
    		return this;
    	}

		public Vector build() {
    		return new Vector(x,y,z);
    	}
		
		public Builder combine(Builder other) {
			x += other.x; 
			y += other.y;
			z += other.z;
			return this;
		}
    }
	
}
