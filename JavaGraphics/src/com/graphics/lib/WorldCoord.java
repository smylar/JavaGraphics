package com.graphics.lib;

public class WorldCoord extends Point {

	private Point transformed;
	
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
