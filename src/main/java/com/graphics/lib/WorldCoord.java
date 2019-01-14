package com.graphics.lib;

import java.util.HashMap;
import java.util.Map;

import com.graphics.lib.camera.Camera;
import com.graphics.lib.texture.Texture;

/**
 * A world coordinate is the representation of a point within the context of the world, and not as the camera might see it.
 * <br/>
 * This class extends Point in that it allows a transformed point (camera coordinate) and a set of texture coordinates to be associated with it
 * 
 * @see Point
 * @see Texture
 * 
 * @author Paul Brandon
 *
 */
public class WorldCoord extends Point {

	private Map<Camera, Point> transformed = new HashMap<Camera, Point>();
	private String group = "";
	
	public WorldCoord() {
		this(0,0,0);
	}
	
	public WorldCoord(Point p) {
		this(p.x, p.y, p.z);
	}
	
	public WorldCoord(double x, double y, double z) {
		super(x, y, z);
	}
	
	public WorldCoord(double x, double y, double z, String group) {
		super(x, y, z);
		this.group = group;
	}
	
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public Point getTransformed(Camera c) {
	    return transformed.computeIfAbsent(c, key -> new Point(x,y,z));
	}

	public void resetTransformed(Camera c) {
			Point tr = getTransformed(c);
			tr.x = x;
			tr.y = y;
			tr.z = z;
	}
	
}
