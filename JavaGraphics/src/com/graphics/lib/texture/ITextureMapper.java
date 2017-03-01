package com.graphics.lib.texture;

import java.util.List;
import java.util.Map;

import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;

@FunctionalInterface
public interface ITextureMapper {

	void map(List<WorldCoord> vertexList, Map<WorldCoord, Point> textureMap, Texture texture);

}
