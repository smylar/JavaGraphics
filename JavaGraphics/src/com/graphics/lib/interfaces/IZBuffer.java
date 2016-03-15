package com.graphics.lib.interfaces;

import java.util.HashMap;
import java.util.Map;

import com.graphics.lib.camera.Camera;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.shader.IShader;
import com.graphics.lib.zbuffer.ZBufferItem;

/**
 * Interface for a Z Buffer.
 * <br/>
 * A Z Buffer contains the information required to draw the entire scene on the screen.
 * Generally we calculate the x, y and position and colour of every pixel in an object,
 * as more than one object might occupy the same screen x and y coordinates, the one with the smallest z value remains in the buffer
 * 
 * @author Paul Brandon
 *
 */
public interface IZBuffer {
	/**
	 * Add a facet to the Z Buffer
	 * 
	 * @param parent - The CanvasObject the facet belongs to
	 * @param shader - The shader object that will handle colouring of the facet
	 * @param c		- The camera being processed
	 */
	public void Add(CanvasObject obj, IShader shader, Camera c, double horizon);
	
	/**
	 * Get the Z buffer, the form is (in an attempt for some performance when finding a specific entry):
	 * <br/>
	 * <code>Map{xValue, Map{yValue, item}}</code>
	 * @return The Z Buffer map
	 */
	public Map<Integer, HashMap<Integer, ZBufferItem>> getBuffer();
	
	
	/**
	 * Set the width of the buffer in pixels
	 * @param width
	 * @param height
	 */
	public void setDimensions(int width, int height);
	
	/**
	 * Get the item at the given x, y coordinates
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public ZBufferItem getItemAt(int x, int y);
	
	public void clear();
}
