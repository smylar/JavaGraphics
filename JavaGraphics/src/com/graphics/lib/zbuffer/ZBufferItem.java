package com.graphics.lib.zbuffer;

import java.awt.Color;
import java.util.TreeMap;

import com.graphics.lib.canvas.CanvasObject;

/**
 * Stores information about Z values and colour for a screen coordinate this item represents
 * 
 * @author Paul Brandon
 *
 */
public class ZBufferItem
{
	private TreeMap<Double, Color> items = new TreeMap<Double, Color>();
	private CanvasObject topMostObject = null;
	private int x = 0;
	private int y = 0;
	private boolean active = false;
	
	public ZBufferItem(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public CanvasObject getTopMostObject() {
		return topMostObject;
	}
	
	public boolean isActive() {
		return active;
	}

	/**
	 * Gets the colour of the pixel represented by this item
	 * <br/>
	 * The buffer can store multiple values in Z value order, this allows us to take account of transparent colourings etc.
	 * 
	 * @return
	 */
	public Color getColour() {
		//N.B. TreeMap will automatically order lowest to highest key (natural ordering)
		int red = 0;
		int green = 0;
		int blue = 0;
		int alpha = 0;
		for (Color c : items.values()){
			if (c.getAlpha() > alpha) alpha = c.getAlpha();
			red += ((double)c.getRed() / 255) * c.getAlpha();
			blue += ((double)c.getBlue() / 255) * c.getAlpha();
			green += ((double)c.getGreen() / 255) * c.getAlpha();
			if (alpha == 255) break;
		}
		
		if (red > 255) red = 255;
		if (green > 255) green = 255;
		if (blue > 255) blue = 255;
		
		return new Color(red, green, blue, alpha);
	}
	
	/**
	 * Add and Z value / colour combination to this item
	 * 
	 * @param z
	 * @param colour
	 */
	public synchronized void add(CanvasObject obj, double z, Color colour)
	{	
		if (items.isEmpty() || z < items.firstKey()){
			topMostObject = obj;
		}
		items.put(z, colour);
		active = true;
	}
	
	public void clear(){
		active = false;
		items.clear();
		topMostObject = null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ZBufferItem other = (ZBufferItem) obj;
		if (items == null) {
			if (other.items != null)
				return false;
		} else if (!items.equals(other.items))
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
			
}
