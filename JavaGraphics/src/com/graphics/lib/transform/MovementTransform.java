package com.graphics.lib.transform;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.interfaces.IVectorFinder;

public class MovementTransform extends Transform {

	private Vector vector;
	private double speed;
	private IVectorFinder vectorFinder;
	private double distanceMoved = 0;
	private double acceleration = 0;
	private double maxSpeed = 0;
	Predicate<MovementTransform> until;
	
	public MovementTransform(IVectorFinder vectorFinder, double speed)
	{
		this.vectorFinder = vectorFinder;
		this.speed = speed;
	}
	
	public MovementTransform(Vector vector, double speed)
	{
		this.vectorFinder = null;
		this.speed = speed;
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
	
	public double getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public double getSpeed() {
		return speed;
	}
	
	public Vector getVelocity(){
		return new Vector(vector.x * speed, vector.y * speed, vector.z * speed);
	}

	public void setSpeed(double speed) {
		//TODO extra work around negative speeds
		this.speed = (this.maxSpeed > 0 && speed > maxSpeed) ? maxSpeed : speed;
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
		return this.until != null && this.until.test(this);
	}
	
	@Override
	public void afterTransform()
	{
		this.distanceMoved += this.speed;
		if (this.maxSpeed == 0 || this.speed < this.maxSpeed){
			this.speed += this.acceleration;
		}
	}

	@Override
	public Consumer<Point> doTransformSpecific() {
		return (p) -> {
			p.x += vector.x * speed;
			p.y += vector.y * speed;
			p.z += vector.z * speed;
		};
	}

	@Override
	public void beforeTransform() {
		if (vectorFinder != null)
			this.vector = vectorFinder.getVector();
		
	}	

}
