package com.graphics.lib.transform;

public class YRotation extends Matrix{

	@Override
	protected void generateMatrix(double angle, double trans1, double trans2) {
		matrix[0][0] = Math.cos(Math.toRadians(angle));
        matrix[0][1] = 0;
        matrix[0][2] = Math.sin(Math.toRadians(angle));
        matrix[0][3] = trans1;
        matrix[1] = new double[]{0,1,0,0};
        matrix[2][0] = -(Math.sin(Math.toRadians(angle)));
        matrix[2][1] = 0;
        matrix[2][2] = Math.cos(Math.toRadians(angle));
        matrix[2][3] = trans2;	
	}

}
