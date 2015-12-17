package com.graphics.lib.shader;

import java.awt.Color;
import java.util.Set;

import com.graphics.lib.Facet;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.zbuffer.ScanLine;

public interface IShader {
	/**
	 * The initialisation method called before processing Scan Lines in a facet
	 * 
	 * @param obj	Canvas Object being processed
	 * @param f		Facet of canvas object being processed
	 */
	public void init(CanvasObject obj, Facet f);
	
	/**
	 * Gets the colour for a pixel (x,y) on the given scan line
	 * @see ScanLine
	 * 
	 * @param sl	The scanline object
	 * @param x		X coordinate of the pixel
	 * @param y		Y coordinate of the pixel
	 * @return		The colour of the pixel
	 */
	public Color getColour (ScanLine sl, int x, int y, double z);
	
	/**
	 * Shaders will usually need to be aware of light sources, this method will set them
	 * 
	 * @param ls
	 */
	public void setLightsources(Set<LightSource> ls);
	
	/**
	 * Shaders will usually need to be aware of light sources, this method will retrieve them
	 * 
	 * @return List of lightsources
	 */
	public Set<LightSource> getLightsources();
}
