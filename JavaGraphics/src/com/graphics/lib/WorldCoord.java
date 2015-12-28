package com.graphics.lib;

import java.util.HashMap;
import java.util.Map;

import com.graphics.lib.texture.Texture;

/**
 * A world coordinate is the representation of a point within the context of the world, and not as the camera might see it.
 * <br/>
 * This class extends Point in that allows a transformed point (camera coordinate) and a set of texture coordinates to be associated with it
 * 
 * @see Point
 * @see Texture
 * 
 * @author Paul Brandon
 *
 */
public class WorldCoord extends Point {

	private Point transformed;
	private Map<Texture, Double> textureX;
	private Map<Texture, Double> textureY;
	
	public WorldCoord() {
		this(0,0,0);
	}
	
	public WorldCoord(Point p) {
		this(p.x, p.y, p.z);
	}
	
	public WorldCoord(double x, double y, double z) {
		super(x, y, z);
		transformed = new Point(x,y,z);
	}
	
	public Point getTransformed() {
		return transformed;
	}

	public void resetTransformed() {
		transformed.x = x;
		transformed.y = y;
		transformed.z = z;
	}

	public double getTextureX(Texture t) {
		return (textureX != null && textureX.containsKey(t)) ? textureX.get(t) : 0;
	}

	public void setTextureX(Texture t, double textureX) {
		if (this.textureX == null) this.textureX = new HashMap<Texture, Double>();
		this.textureX.put(t, textureX);
	}

	public double  getTextureY(Texture t) {
		return (textureY != null && textureY.containsKey(t)) ? textureY.get(t) : 0;
	}

	public void setTextureY(Texture t, double textureY) {
		if (this.textureY == null) this.textureY = new HashMap<Texture, Double>();
		this.textureY.put(t, textureY);
	}
	
	
}
