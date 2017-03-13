package com.graphics.lib.canvas;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ITexturable;
import com.graphics.lib.texture.ITextureMapper;
import com.graphics.lib.texture.Texture;

public class TexturableCanvasObject<T extends ICanvasObject> extends CanvasObjectWrapper<T> implements ITexturable {
	//note currently can't apply textures on a per facet (or set of facets) basis, though may be able to achieve that with the mapper used
	private Map<Texture, Map<WorldCoord, Point>> textureMap = new HashMap<>();
	
	public TexturableCanvasObject()
	{
		super();
	}
	
	public TexturableCanvasObject(T obj)
	{
		super(obj);
	}

	@Override
	public ITexturable addTexture(Texture texture) {
		textureMap.put(texture, new HashMap<>());
		return this;
	}
	
	@Override
	public ITexturable mapTexture(ITextureMapper mapper) {
		textureMap.entrySet().forEach(e -> mapper.map(this.getVertexList(), e.getValue(), e.getKey()));
		return this;
	}
	
	@Override
	public Set<Texture> getTextures() {
		return textureMap.keySet();
	}
	
	@Override
	public Optional<Point> getTextureCoord(Texture texture, WorldCoord vertex) {
		if (textureMap.containsKey(texture)) {
			return Optional.ofNullable(textureMap.get(texture).get(vertex));
		}
		
		return Optional.empty();
	}

	@Override
	public Set<Texture> getTextures(WorldCoord vertex) {
		Set<Texture> textures = new HashSet<>();
		textureMap.forEach((t, coords) -> {
			if (coords.containsKey(vertex)) {
				textures.add(t);
			}
		});
		return textures;
	}
}
