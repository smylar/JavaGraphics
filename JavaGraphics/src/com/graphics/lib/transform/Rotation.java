package com.graphics.lib.transform;

import java.util.function.Consumer;

import com.graphics.lib.Axis;
import com.graphics.lib.Point;

/**
 * Performs a rotation transformation, rotates points around the origin by a given angle in the given axis
 * 
 * @author paul
 *
 */
public class Rotation extends Transform{
	private double angleProgression = 0;
	private Matrix matrix;
	private Axis axis;
	private double totalRotation = 0;
	
	public Rotation(Axis axis, double angleProgression){
		try {
			this.matrix = axis.getMatrix().newInstance();
			this.axis = axis;
			this.matrix.generateMatrix(angleProgression, 0, 0);
			this.angleProgression = angleProgression;
		} catch (InstantiationException | IllegalAccessException e) {}
		
	}
	
	public void setAngleProgression(double angleProgression) {
		this.angleProgression = angleProgression;
	}
	
	public Axis getAxis() {
		return this.axis;
	}
	
	public double getTotalRotation() {
		return this.totalRotation;
	}

	@Override
	public boolean isCompleteSpecific() {
		return true;
	}

	@Override
	public void afterTransform() {
		this.totalRotation += this.angleProgression;
	}
	
	@Override
	public Consumer<Point> doTransformSpecific() {
		if (matrix != null) {			
			return matrix.getMapper();
		}
	 return (p) -> {return;};
	}
	
}
