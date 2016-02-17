package com.graphics.lib.transform;

import java.util.function.Consumer;

import com.graphics.lib.Point;


public class Rotation<T extends Matrix> extends Transform{
	
	public double angleProgression = 0;
	private Class<T> matrix;
	private double totalRotation = 0;
	
	public static Rotation<?> getRotation(char direction, double amount){
		if (direction== 'x'){
			return new Rotation<XRotation>(XRotation.class, amount);
		}else if (direction == 'y'){
			return new Rotation<YRotation>(YRotation.class, amount);
		}else if (direction == 'z'){
			return new Rotation<ZRotation>(ZRotation.class, amount);
		}
		return null;
	}
	
	public Rotation(Class<T> matrix, double angleProgression){
		this.matrix = matrix;
		this.angleProgression = angleProgression;
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
		try {
			Matrix m = matrix.newInstance();
			m.generateMatrix(this.angleProgression, 0, 0);			
			return m.getMapper();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	 return (p) -> {return;};
	}
	
}
