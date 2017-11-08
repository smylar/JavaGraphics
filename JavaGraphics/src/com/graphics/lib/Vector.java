package com.graphics.lib;

public class Vector {

    protected double x = 0;
    protected double y = 0;
    protected double z = 0;
	
    //TODO refactor as immutable
	public Vector(){}
	
	public Vector(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }
    
    public void addX(double x) {
        this.x += x;
    }
    
    public void addY(double y) {
        this.y += y;
    }
    
    public void addZ(double z) {
        this.z += z;
    }

    /**
	 * A unit vector is one where it's magnitude is exactly 1
	 * 
	 * @return Unit Vector
	 */
	public Vector getUnitVector()
	{
		double squareadd = (x*x)+(y*y)+(z*z);
		double divider = Math.sqrt(squareadd);
		if (divider == 0){
			return new Vector();
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
	 * Generates a vector that is perpendicular to the plane occupied by the 2 vectors it is generated from
	 * <br/>
	 * N.B. Ordering is important v1 x v2 will generate a vector opposite to v2 x v1
	 * 
	 * @param v2
	 * @return Perpendicular Vector
	 */
	public Vector crossProduct(Vector v2)
	{
		Vector normal = new Vector();
		 normal.x = (this.y*v2.z) - (v2.y*this.z);
		 normal.y = -(this.x*v2.z ) + (v2.x*this.z);
		 normal.z = (this.x*v2.y) - (v2.x*this.y);
		 return normal;
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
	
	/**
	 * Combine two vectors to given their resultant vector
	 * 
	 * @param v2 Vector to combine with this one
	 */
	public void addVector(Vector v2)
	{
		this.x += v2.x;
		this.y += v2.y;
		this.z += v2.z;
	}
	
	@Override
	public String toString(){
		return this.x + "," + this.y + "," + this.z;
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
	
}
