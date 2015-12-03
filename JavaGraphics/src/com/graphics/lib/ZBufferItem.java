package com.graphics.lib;

import java.awt.Color;
import java.util.TreeMap;

public class ZBufferItem
{
	private TreeMap<Integer, Color> items = new TreeMap<Integer, Color>();
	private int x = 0;
	private int y = 0;
	
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

	Color getColour() {
		//N.B. TreeMap will automatically order lowest to highest key
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
	
	public void add(int z, Color colour)
	{
		items.put(z, colour);			
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
