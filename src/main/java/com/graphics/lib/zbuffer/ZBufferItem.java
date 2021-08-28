package com.graphics.lib.zbuffer;

import java.awt.Color;
import java.util.TreeMap;

import com.graphics.lib.interfaces.ICanvasObject;

/**
 * Stores information about Z values and colour for a screen coordinate this item represents
 * 
 * @author Paul Brandon
 *
 */
public final class ZBufferItem
{
	private TreeMap<Double, Color> items = new TreeMap<>();
	private ICanvasObject topMostObject = null;

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

	public ICanvasObject getTopMostObject() {
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

		Color first = items.firstEntry().getValue();
		if (items.size() == 1 || first.getAlpha() == 255) {
		    return first;
		}
		
		//N.B. TreeMap will automatically order lowest to highest key (natural ordering)
		int red = 0;
		int green = 0;
		int blue = 0;
		int alpha = 0;
		
		for (Color c : items.values()){
			if (c.getAlpha() > alpha) {
			    alpha = c.getAlpha();
			}
			red += ((double)c.getRed() / 255) * c.getAlpha();
			blue += ((double)c.getBlue() / 255) * c.getAlpha();
			green += ((double)c.getGreen() / 255) * c.getAlpha();
			
			if (alpha == 255) {
			    break;
			}
		}
		
		red = red > 255 ? 255 : red;
		green = green > 255 ? 255 : green; 
		blue = blue > 255 ? 255 : blue;
		
		return new Color(red, green, blue, alpha);
	}
	
	/**
	 * Add and Z value / colour combination to this item
	 * 
	 * @param z
	 * @param colour
	 */
	public synchronized void add(ICanvasObject obj, double z, final Color colour)
	{	
		if (items.isEmpty() || z < items.firstKey()) {
			topMostObject = obj;
			items.put(z, colour);
			active = true;
		} else if (items.firstEntry().getValue().getAlpha() < 255) {
			items.put(z, colour);
		}
	}
	
	public void clear() {
		active = false;
		items.clear();
		topMostObject = null;
	}
			
}
