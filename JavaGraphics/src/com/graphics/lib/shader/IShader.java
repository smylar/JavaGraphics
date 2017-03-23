package com.graphics.lib.shader;

import java.awt.Color;
import com.graphics.lib.Facet;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.zbuffer.ScanLine;

/**
 * A shader is used by the Z Buffer to generate the colour of each pixel
 * <br/>
 * If facets are processed in parallel, this assumes the z buffer creates a new instance of the shader to process a facet 
 * 
 * @author Paul Brandon
 *
 */
public interface IShader {
	/**
	 * The initialisation method called before processing Scan Lines in a facet
	 * 
	 * @param obj	Canvas Object being processed
	 * @param f		Facet of canvas object being processed
	 * @param c		Camera being processed
	 */
	public void init(ICanvasObject obj, Facet f, Camera c);
	
	/**
	 * Gets the colour for a pixel (x,y) on the given scan line, 
	 * which is associated with the facet given in init()
	 * @see ScanLine
	 * 
	 * @param sl	The scanline object
	 * @param x		X coordinate of the pixel
	 * @param y		Y coordinate of the pixel
	 * @return		The colour of the pixel
	 */
	public Color getColour (ScanLine sl, int x, int y);
}
