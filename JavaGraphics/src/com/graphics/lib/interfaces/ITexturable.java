package com.graphics.lib.interfaces;

import java.util.Optional;
import java.util.Set;

import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.texture.ITextureMapper;
import com.graphics.lib.texture.Texture;

public interface ITexturable extends ICanvasObject {

	ITexturable addTexture(Texture texture);

	ITexturable mapTexture(ITextureMapper mapper);

	Set<Texture> getTextures();

	Optional<Point> getTextureCoord(Texture texture, WorldCoord vertex);
	
	Set<Texture> getTextures(WorldCoord vertex);

}