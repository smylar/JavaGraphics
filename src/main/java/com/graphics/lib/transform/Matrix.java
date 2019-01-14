package com.graphics.lib.transform;

import java.util.function.Consumer;

import com.graphics.lib.Point;

public abstract class Matrix {
	protected double[][] matrix = new double[3][4];
	
	public Consumer<Point> getMapper()
	{
		return (p) -> {
		double x = (p.x * matrix[0][0])  + (p.y * matrix[0][1]) + (p.z * matrix[0][2]) + matrix[0][3];
		double y = (p.x * matrix[1][0])  + (p.y * matrix[1][1]) + (p.z * matrix[1][2]) + matrix[1][3];
		double z = (p.x * matrix[2][0])  + (p.y * matrix[2][1]) + (p.z * matrix[2][2]) + matrix[2][3];
		p.x = x;	
		p.y = y;               
		p.z = z;
		};
	
	}

	protected abstract void generateMatrix(double angle, double trans1, double trans2);
}
