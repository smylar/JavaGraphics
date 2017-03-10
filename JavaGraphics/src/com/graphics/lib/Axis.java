package com.graphics.lib;

import com.graphics.lib.transform.Matrix;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.transform.XRotation;
import com.graphics.lib.transform.YRotation;
import com.graphics.lib.transform.ZRotation;

public enum Axis {
	X(XRotation.class),
	Y(YRotation.class),
	Z(ZRotation.class);
	
	private Class<? extends Matrix> matrix;
	
	private Axis(Class<? extends Matrix> matrix) {
		this.matrix = matrix;
	}
	
	public Class<? extends Matrix> getMatrix() {
		return matrix;
	}
	
	public Rotation getRotation(double amount) {
		return new Rotation(this, amount);
	}
}
