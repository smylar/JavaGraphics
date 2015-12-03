package com.graphics.lib;

public class Vector {
	public double x = 0;
	public double y = 0;
	public double z = 0;
	
	public Vector(){}
	
	public Vector(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector getUnitVector()
	{
		double squareadd = (x*x)+(y*y)+(z*z);
		double divider = Math.sqrt(squareadd);
		if (divider == 0){
			return new Vector();
		}
		
		return new Vector(this.x/divider, this.y/divider, this.z/divider);
	}
	
	public double dotProduct (Vector v2)
	{
		return (this.x * v2.x) + (this.y * v2.y) + (this.z * v2.z);
	}
	
	public Vector crossProduct(Vector v2)
	{
		Vector normal = new Vector();
		 normal.x = (this.y*v2.z) - (v2.y*this.z);
		 normal.y = (-(this.x*v2.z ) + (v2.x*this.z));
		 normal.z = ((this.x*v2.y) - (v2.x*this.y));
		 return normal;
	}
	
	public Vector getReverseVector()
	{
		return new Vector(-this.x, -this.y, -this.z);
	}
	
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
	public int hashCode() {
		//TODO will need to change these, as vector is altered it will change the hash code which is not good
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
	
}
