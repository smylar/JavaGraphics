package com.graphics.lib.transform;

public class ZRotation extends Matrix{

	@Override
	protected void generateMatrix(double angle, double trans1, double trans2) {
		matrix[0][0] = Math.cos(Math.toRadians(angle));
	    matrix[0][1] = -(Math.sin(Math.toRadians(angle)));
	    matrix[0][2] = 0;
	    matrix[0][3] = trans1;
        matrix[1][0] = Math.sin(Math.toRadians(angle));
	    matrix[1][1] = Math.cos(Math.toRadians(angle));
	    matrix[1][2] = 0;
	    matrix[1][3] = trans2;
        matrix[2] = new double[]{0,0,1,0};
		
	}

}
