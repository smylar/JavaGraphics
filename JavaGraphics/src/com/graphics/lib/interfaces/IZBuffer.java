package com.graphics.lib.interfaces;

import java.awt.image.BufferedImage;

import com.graphics.lib.camera.Camera;
import com.graphics.lib.shader.ShaderFactory;
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
	public void add(ICanvasObject obj, ShaderFactory shader, Camera c, double horizon);
	
	/**
	 * Get the Z buffer, the form is (in an attempt for some performance when finding a specific entry):
	 * <br/>
	 * <code>Map{xValue, Map{yValue, item}}</code>
	 * @return The Z Buffer map
	 */
	public BufferedImage getBuffer();
	
	/**
	 * Refresh the image buffer with the current data
	 */
	public void refreshBuffer();
	
	
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
