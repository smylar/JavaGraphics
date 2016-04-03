package com.graphics.lib.shader;

import java.awt.Color;
import java.util.Set;

import com.graphics.lib.Facet;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.lightsource.ILightSource;
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
	public void init(CanvasObject obj, Facet f, Camera c);
	
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
	
	/**
	 * Shaders will usually need to be aware of light sources, this method will set them
	 * 
	 * @param ls
	 */
	public void setLightsources(Set<ILightSource> ls);
	
	/**
	 * Shaders will usually need to be aware of light sources, this method will retrieve them
	 * 
	 * @return List of lightsources
	 */
	public Set<ILightSource> getLightsources();
}
