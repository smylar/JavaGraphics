package com.graphics.lib.texture;

import java.util.List;
import java.util.Map;

import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.interfaces.ICanvasObject;

/**
 * Simple (test) mapper, that just maps the first 4 points to the extent of the texture
 * 
 * @author paul
 *
 */
public class SimpleTextureMapper extends TextureMapper<ICanvasObject> {

	public SimpleTextureMapper() {
		super(ICanvasObject.class);
	}
	
	@Override
	public void mapImpl(ICanvasObject obj, Map<WorldCoord, Point> textureMap, Texture texture) {
		List<WorldCoord> vertexList = obj.getVertexList();
		if (vertexList.size() < 4) return;
		
		textureMap.put(vertexList.get(0), new Point(0, 0, 0));
		textureMap.put(vertexList.get(1), new Point(0, texture.getHeight(), 0));
		textureMap.put(vertexList.get(2), new Point(texture.getWidth(), texture.getHeight(), 0));
		textureMap.put(vertexList.get(3), new Point(texture.getWidth(), 0, 0));
	}
}
