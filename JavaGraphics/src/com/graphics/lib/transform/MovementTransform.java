package com.graphics.lib.transform;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.interfaces.IVectorFinder;

public class MovementTransform extends Transform {

	private Vector vector;
	private double velocity;
	private IVectorFinder vectorFinder;
	private double distanceMoved = 0;
	private double acceleration = 0;
	Predicate<MovementTransform> until;
	
	public MovementTransform(IVectorFinder vectorFinder, double velocity)
	{
		this.vectorFinder = vectorFinder;
		this.velocity = velocity;
	}
	
	public MovementTransform(Vector vector, double velocity)
	{
		this.vectorFinder = null;
		this.velocity = velocity;
		this.vector = vector;
	}
	
	public void moveUntil(Predicate<MovementTransform> until)
	{
		this.until = until;
	}
	
	public Vector getVector() {
		return vector;
	}

	public void setVector(Vector vector) {
		this.vector = vector.getUnitVector();
	}

	public double getVelocity() {
		return velocity;
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}
	
	public double getDistanceMoved() {
		return distanceMoved;
	}

	public double getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(double acceleration) {
		this.acceleration = acceleration;
	}

	@Override
	public boolean isCompleteSpecific() {
		return this.velocity <= 0 || (this.until != null && this.until.test(this));
	}
	
	@Override
	public void afterTransform()
	{
		this.distanceMoved += this.velocity;
		this.velocity += this.acceleration;
	}

	@Override
	public Consumer<Point> doTransformSpecific() {
		return (p) -> {
			p.x += vector.x * velocity;
			p.y += vector.y * velocity;
			p.z += vector.z * velocity;
		};
	}

	@Override
	public void beforeTransform() {
		if (vectorFinder != null)
			this.vector = vectorFinder.getVector();
		
	}	

}
