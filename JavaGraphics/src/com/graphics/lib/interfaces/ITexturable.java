package com.graphics.lib.interfaces;

import java.util.Optional;
import java.util.Set;

import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.texture.Texture;
import com.graphics.lib.texture.TextureMapper;

public interface ITexturable extends ICanvasObject {

	ITexturable addTexture(Texture texture);

	ITexturable mapTexture(TextureMapper<?> mapper);

	Set<Texture> getTextures();

	Optional<Point> getTextureCoord(Texture texture, WorldCoord vertex);
	
	Set<Texture> getTextures(WorldCoord vertex);

}