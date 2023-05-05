package com.graphics.lib.interfaces;

import java.util.Optional;
import java.util.Set;

import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.texture.Texture;

/**
 * Allows a texture to be applied to an object
 * 
 * @author paul
 *
 */
public interface ITexturable extends ITrait {

	/**
	 * Add a texture to this object
	 * 
	 * @param texture
	 * @return
	 */
	ITexturable addTexture(Texture texture);

	/**
	 * Get the textures added to this object
	 * 
	 * @return
	 */
	Set<Texture> getTextures();

	/**
	 * Find the texture coordinate for the given texture for the given object vertex
	 * 
	 * @param texture
	 * @param vertex
	 * @return
	 */
	Optional<Point> getTextureCoord(Texture texture, WorldCoord vertex);
	
	/**
	 * Get the textures that apply to the given object vertex
	 * 
	 * @param vertex
	 * @return
	 */
	Set<Texture> getTextures(WorldCoord vertex);

}