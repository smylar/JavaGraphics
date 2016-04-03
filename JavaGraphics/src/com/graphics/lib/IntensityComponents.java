package com.graphics.lib;

import java.awt.Color;

public class IntensityComponents {
	private double red = 0;
	private double green = 0;
	private double blue = 0;
	
	public double getRed() {
		return red;
	}
	public void setRed(double red) {
		if (red > 1) this.red = 1;
		else if (red < 0) this.red = 0;
		else this.red = red;
	}
	public double getGreen() {
		return green;
	}
	public void setGreen(double green) {
		if (green > 1) this.green = 1;
		else if (green < 0) this.green = 0;
		else this.green = green;
	}
	public double getBlue() {
		return blue;
	}
	public void setBlue(double blue) {
		if (blue > 1) this.blue = 1;
		else if (blue < 0) this.blue = 0;
		else this.blue = blue;
	}
	
	public boolean hasNoIntensity(){
		return red == 0 && blue == 0 && green == 0;
	}
	
	public Color applyIntensities(Color colour){
		return new Color((int)((double)colour.getRed() * red), 
				(int)((double)colour.getGreen() * green), 
				(int)((double)colour.getBlue() * blue),
				colour.getAlpha());
	}
}
