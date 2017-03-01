package com.graphics.lib.texture;

import java.awt.Color;
import java.util.Optional;

/**
 * A texture is a pattern that overlays a facet or set of facets
 * <br/>
 * Typically a facet's vertices are also given texture coordinates (a point in the pattern that the vertex occupies),
 * the shader will then work out the texture coordinates for each pixel it is calculating and lookup that pixels colour 
 * in the associated texture object.
 * <br/>
 * The source of the texture could be a bitmap, or some other method of your devising
 * 
 * @see BmpTexture
 * 
 * @author Paul Brandon
 *
 */
public interface Texture {
	/**
	 * Get the colour of the pattern at a given texture coordinate
	 * 
	 * @param x - X value of texture coordinate
	 * @param y - Y value of texture coordinate
	 * @return Colour of the pixel
	 */
	public Optional<Color> getColour(int x, int y);
	
	public void setColour(int x, int y, Color colour);
	
	/**
	 * Get the height of the full texture
	 * @return The height in pixels
	 */
	public int getHeight();
	
	/**
	 * Get the width of the full texture
	 * @return The width in pixels
	 */
	public int getWidth();
	
	/**
	 * Allows for setting of a flag which dictates if lighting effects should be applied to this texture
	 * 
	 * @return
	 */
	public boolean applyLighting();
	
	public int getOrder();
}
