package com.graphics.lib.transform;

import java.util.function.Consumer;

import com.graphics.lib.Point;

public class Translation extends Transform {

	private double transX = 0;
	private double transY = 0;
	private double transZ = 0;
	
	public Translation(){}
	
	public Translation(double transX, double transY, double transZ)
	{
		this.transX = transX;
		this.transY = transY;
		this.transZ = transZ;
	}

	/**
     * @return the transX
     */
    public double getTransX() {
        return transX;
    }

    /**
     * @param transX the transX to set
     */
    public void setTransX(double transX) {
        this.transX = transX;
    }

    /**
     * @return the transY
     */
    public double getTransY() {
        return transY;
    }

    /**
     * @param transY the transY to set
     */
    public void setTransY(double transY) {
        this.transY = transY;
    }

    /**
     * @return the transZ
     */
    public double getTransZ() {
        return transZ;
    }

    /**
     * @param transZ the transZ to set
     */
    public void setTransZ(double transZ) {
        this.transZ = transZ;
    }

    @Override
	public boolean isCompleteSpecific() {
		return true;
	}

	@Override
	public Consumer<Point> doTransformSpecific() {
		return p -> {
			p.x += transX;
			p.y += transY;
			p.z += transZ;
		};
	}

}
