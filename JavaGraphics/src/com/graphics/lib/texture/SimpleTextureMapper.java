package com.graphics.lib.texture;

import java.util.List;
import java.util.Map;

import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;

/**
 * Simple (test) mapper, that just maps the first 4 points to the extent of the texture
 * 
 * @author paul
 *
 */
public class SimpleTextureMapper implements ITextureMapper {

	@Override
	public void map(List<WorldCoord> vertexList, Map<WorldCoord, Point> textureMap, Texture texture) {
		if (vertexList.size() < 4) return;
		
		textureMap.put(vertexList.get(0), new Point(0, 0, 0));
		textureMap.put(vertexList.get(1), new Point(0, texture.getHeight(), 0));
		textureMap.put(vertexList.get(2), new Point(texture.getWidth(), texture.getHeight(), 0));
		textureMap.put(vertexList.get(3), new Point(texture.getWidth(), 0, 0));
	}

}
