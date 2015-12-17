package com.graphics.lib;

public class WorldCoord extends Point {

	private Point transformed;
	private double textureX = 0;
	private double textureY = 0;
	
	public WorldCoord() {
		this(0,0,0);
	}
	
	public WorldCoord(Point p) {
		this(p.x, p.y, p.z);
	}
	
	public WorldCoord(double x, double y, double z) {
		super(x, y, z);
		this.resetTransformed();
	}
	
	public Point getTransformed() {
		return transformed;
	}

	public void resetTransformed() {
		transformed = new Point (x, y, z);
		//transformed.setLightIntensity(this.getLightIntensity());
	}

	public double getTextureX() {
		return textureX;
	}

	public void setTextureX(double textureX) {
		this.textureX = textureX;
	}

	public double  getTextureY() {
		return textureY;
	}

	public void setTextureY(double textureY) {
		this.textureY = textureY;
	}
	
	
	/*@Override
	public void setLightIntensity(IntensityComponents lightIntensity) {
		super.setLightIntensity(lightIntensity);
		if (transformed != null) transformed.setLightIntensity(lightIntensity);
	}*/
	
	/*@Override
	public void setLightIntensity(ILightIntensityFinder liFinder, boolean isPartOfBacface, CanvasObject obj, Vector v) {
		super.setLightIntensity(liFinder, isPartOfBacface, obj, v);
		if (transformed != null) transformed.setLightIntensity(this.getLightIntensity());
	}*/
	
}
