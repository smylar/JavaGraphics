package com.graphics.lib.transform;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.interfaces.IVectorFinder;
import org.apache.commons.lang3.tuple.Pair;

public class MovementTransform extends Transform {

	private Vector vector;

	private Vector velocity = Vector.ZERO_VECTOR;
	private final IVectorFinder vectorFinder;
	private double distanceMoved = 0;
	private Acceleration acceleration = null;

	private double lastSpeed;

	Predicate<MovementTransform> until;
	
	public MovementTransform(IVectorFinder vectorFinder, double speed) //TODO should split this into fixed and finder types
	{
		this.vectorFinder = vectorFinder;
		this.lastSpeed = speed;
	}
	
	public MovementTransform(Vector vector, double speed)
	{
		this.vectorFinder = null;
		this.lastSpeed = speed;
		setVector(vector);
	}
	
	public MovementTransform moveUntil(Predicate<MovementTransform> until)
	{
		this.until = until;
		return this;
	}
	
	public Vector getVector() {
		return vector;
	}

	public MovementTransform setVector(Vector vector) {
		this.vector = vector.getUnitVector();
		this.velocity = vector.generateVelocity(lastSpeed);
		return this;
	}

	public double getSpeed() {
		return lastSpeed;
	}
	
	/**
	 * Get the vector combined with the speed (velocity)
	 * (Note each call generates a new Vector object)
	 */
	public Vector getVelocity(){
		return velocity;
	}

	public void setSpeed(double speed) {
		//TODO extra work around negative speeds
		lastSpeed = speed;
	}

	public void stop() {
		lastSpeed = 0;
		velocity = Vector.ZERO_VECTOR;
		acceleration = null;
	}
	
	public double getDistanceMoved() {
		return distanceMoved;
	}

	public Acceleration getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(Acceleration acceleration) {
		this.acceleration = acceleration;
	}

	@Override
	public boolean isCompleteSpecific() {
		return this.until != null && this.until.test(this);
	}
	
	@Override
	public void afterTransform()
	{
		distanceMoved += lastSpeed;
	}

	@Override
	public Consumer<Point> doTransformSpecific() {
		return p -> {
			p.x += velocity.x();
			p.y += velocity.y();
			p.z += velocity.z();
		};
	}

	@Override
	public void beforeTransform() {
		if (vectorFinder != null) {
			setVector(vectorFinder.getVector().getUnitVector());
		}

		if (acceleration != null) {
			Pair<Vector,Double> newVelocity = acceleration.modify(vector, lastSpeed);
			velocity = newVelocity.getLeft();
			lastSpeed = newVelocity.getRight();
			if (acceleration.changesDirection()) {
				vector = velocity.getUnitVector();
			}
		}
	}	

}
