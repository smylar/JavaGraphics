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
	private Map<Texture, Double> textureX = new HashMap<Texture, Double>();
	private Map<Texture, Double> textureY = new HashMap<Texture, Double>();
	
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
	}

	public double getTextureX(Texture t) {
		return (textureX.containsKey(t)) ? textureX.get(t) : 0;
	}

	public void setTextureX(Texture t, double textureX) {
		this.textureX.put(t, textureX);
	}

	public double  getTextureY(Texture t) {
		return (textureY.containsKey(t)) ? textureY.get(t) : 0;
	}

	public void setTextureY(Texture t, double textureY) {
		this.textureY.put(t, textureY);
	}
	
	
}
