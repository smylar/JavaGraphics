package com.graphics.lib.transform;

import java.util.function.Consumer;

import com.graphics.lib.Point;

public class ScaleTransform extends Transform{

	private double scaling = 0;
	
	public ScaleTransform (double scaling){
		this.scaling = scaling;
	}
	
	public void setScaling(double scaling) {
		this.scaling = scaling;
	}

	@Override
	public boolean isCompleteSpecific() {
		return true;
	}

	@Override
	public Consumer<Point> doTransformSpecific() {
		return (p) -> {
			p.x = p.x * scaling;
			p.y = p.y * scaling;
			p.z = p.z * scaling;
		};
	}

}
