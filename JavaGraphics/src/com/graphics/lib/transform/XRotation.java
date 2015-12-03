package com.graphics.lib.transform;

public class XRotation extends Matrix {

	@Override
	protected void generateMatrix(double angle, double trans1, double trans2) {
		matrix[0] = new double[]{1,0,0,0};
        matrix[1][0] = 0;
        matrix[1][1] = Math.cos(Math.toRadians(angle));
        matrix[1][2] = -(Math.sin(Math.toRadians(angle)));
        matrix[1][3] = trans1;
        matrix[2][0] = 0;
        matrix[2][1] = Math.sin(Math.toRadians(angle));
        matrix[2][2] = Math.cos(Math.toRadians(angle));
        matrix[2][3] = trans2;		
	}

}
